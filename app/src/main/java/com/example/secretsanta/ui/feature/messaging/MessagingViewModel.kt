package com.example.secretsanta.ui.feature.messaging

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.domain.repository.MessagingRepository
import com.example.secretsanta.domain.repository.SecretSantaRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val lastMessage: String? = null,
    val lastMessageAt: Long? = null
)

data class MessagingState(
    val isLoading: Boolean = true,
    val conversations: List<ConversationUi> = emptyList(),
    val info: String? = null,
    val error: String? = null
)

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val messagingRepository: MessagingRepository,
    private val secretSantaRepository: SecretSantaRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(MessagingState())
    val state = _state.asStateFlow()

    private val titleByConversationId = MutableStateFlow<Map<String, Pair<String, String>>>(emptyMap())
    // map[conversationId] = (title, subtitle)

    init {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        val email = firebaseAuth.currentUser?.email.orEmpty()

        if (uid.isBlank() || email.isBlank()) {
            _state.value = MessagingState(isLoading = false, error = "Not authenticated")
        } else {
            // 1) écouter les Secret Santas (pour créer lazy les conversations)
            secretSantaRepository.getAllSecretSantasForUser(uid, email)
                .onEach { santas ->
                    ensureConversations(uid, email, santas)
                }
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
                .launchIn(viewModelScope)

            // 2) écouter les conversations firestore
            messagingRepository.observeConversations(uid)
            // 2) écouter les conversations firestore + filtrer par Secret Santas existants
            combine(
                messagingRepository.observeConversations(uid),
                titleByConversationId,
                secretSantaRepository.getAllSecretSantasForUser(uid, email)
            ) { convos, titles, santas ->

                // Liste des IDs de Secret Santas qui existent encore
                val existingSantaIds = santas.map { it.id }.toSet()

                // Filter + map
                convos
                    .filter { c -> c.secretSantaId in existingSantaIds }  // ✅ Garde seulement si le SS existe
                    .map { c ->
                        val (title, subtitle) = titles[c.id] ?: ("Conversation" to "")
                        ConversationUi(
                            id = c.id,
                            title = title,
                            subtitle = subtitle,
                            lastMessage = c.lastMessage,
                            lastMessageAt = c.lastMessageAt
                        )
                    }
                    .sortedByDescending { it.lastMessageAt ?: 0L }
            }
                .onEach { ui ->
                    _state.value = _state.value.copy(isLoading = false, conversations = ui, error = null)
                }
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
                .launchIn(viewModelScope)
        }
    }

    private suspend fun ensureConversations(
        currentUid: String,
        currentEmail: String,
        santas: List<SecretSanta>
    ) {
        Log.d("MessagingVM", "ensureConversations called: total=${santas.size}")

        val eligible = santas.filter { it.drawDone } // disponible après tirage :contentReference[oaicite:7]{index=7}

        val titles = mutableMapOf<String, Pair<String, String>>()

        var missingUserIds = false

        for (santa in eligible) {
            Log.d("MessagingVM", "---- Santa ${santa.id} drawDone=${santa.drawDone} participants=${santa.participants.size} assignments=${santa.assignments.size}")

            val myParticipant = santa.participants.firstOrNull {
                (it.userId == currentUid) || it.email.equals(currentEmail, ignoreCase = true)
            } ?: continue

            val myPid = myParticipant.id
            Log.d("MessagingVM", "SecretSanta=${santa.id} name=${santa.name} myPid=$myPid myUid=$currentUid myEmail=$currentEmail")


            // Giftee = assignments[myPid] (participantId -> participantId) :contentReference[oaicite:8]{index=8}
            val gifteePid = santa.assignments[myPid] ?: continue
            val giftee = santa.participants.firstOrNull { it.id == gifteePid }
            Log.d("MessagingVM", "gifteePid=$gifteePid gifteeEmail=${giftee?.email} gifteeUserId=${giftee?.userId} gifteeName=${giftee?.name}")

            // Santa = celui dont value == myPid
            val santaPid = santa.assignments.entries.firstOrNull { it.value == myPid }?.key
            val santaParticipant = santaPid?.let { pid -> santa.participants.firstOrNull { it.id == pid } }

            // Créer conversation avec giftee
            val gifteeUid = giftee?.userId
                ?: giftee?.email?.let { messagingRepository.findUserIdByEmail(it) }
            Log.d("MessagingVM", "gifteeUidResolved=$gifteeUid (from userId=${giftee?.userId})")

            if (!gifteeUid.isNullOrBlank()) {
                val conversationId = conversationId(santa.id, currentUid, gifteeUid)
                Log.d("MessagingVM", "Creating conversationId=$conversationId uids=[$currentUid,$gifteeUid]")

                val res = messagingRepository.ensureConversationExists(conversationId, santa.id, currentUid, gifteeUid)
                Log.d("MessagingVM", "ensureConversationExists($conversationId) => $res")

                val gifteeLabel = giftee?.name ?: "Participant"
                titles[conversationId] = (gifteeLabel + " • " + santa.name) to santa.name
            } else if (giftee != null) {
                missingUserIds = true
            }


            // Créer conversation avec mon Santa (anonyme côté UI)
            val santaUid = santaParticipant?.userId
                ?: santaParticipant?.email?.let { messagingRepository.findUserIdByEmail(it) }

            if (!santaUid.isNullOrBlank()) {
                val conversationId = conversationId(santa.id, currentUid, santaUid)
                messagingRepository.ensureConversationExists(conversationId, santa.id, currentUid, santaUid)
                titles[conversationId] = ("Secret Santa • " + santa.name) to santa.name
            } else if (santaParticipant != null) {
                missingUserIds = true
            }
        }
        Log.d("MessagingVM", "eligible(drawDone)=${eligible.size}")

        titleByConversationId.value = titles

        _state.value = _state.value.copy(
            info = if (missingUserIds) {
                "Certaines conversations seront disponibles quand tous les participants auront un compte."
            } else null
        )
    }

    private fun conversationId(secretSantaId: String, uidA: String, uidB: String): String {
        val (minUid, maxUid) = if (uidA <= uidB) uidA to uidB else uidB to uidA
        return "${secretSantaId}_${minUid}_${maxUid}"
    }
}

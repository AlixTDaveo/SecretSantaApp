package com.example.secretsanta.domain.usecase.invitation

import com.example.secretsanta.core.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import javax.inject.Inject

class SendInvitationUseCase @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(
        secretSantaId: String,
        email: String,
        invitedBy: String
    ): Resource<Unit> {
        return try {
            val invitationId = UUID.randomUUID().toString()

            val invitation = mapOf(
                "secretSantaId" to secretSantaId,
                "email" to email.lowercase(),
                "invitedBy" to invitedBy,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("invitations")
                .document(invitationId)
                .set(invitation)

            // TODO: Envoyer un email via Firebase Cloud Functions
            // ou afficher un lien d'invitation à partager

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur d'invitation")
        }
    }
}
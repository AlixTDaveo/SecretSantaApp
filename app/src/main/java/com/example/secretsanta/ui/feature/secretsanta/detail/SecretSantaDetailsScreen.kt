package com.example.secretsanta.ui.feature.secretsanta.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.R
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.navigation.Screen
import com.example.secretsanta.ui.theme.ChristmasColors
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Message


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSantaDetailsScreen(
    navController: NavController,
    santaId: String,
    viewModel: SecretSantaDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            navController.popBackStack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onEvent(SecretSantaDetailsEvent.DeleteSecretSanta)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ChristmasColors.AppButtonRed
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond avec flocons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ChristmasColors.AppBackground,
                            ChristmasColors.AppBackground.copy(alpha = 0.85f)
                        )
                    )
                )
        ) {
            SnowfallBackground(
                snowflakeCount = 100,
                snowColor = Color.White.copy(alpha = 0.8f)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Détails du Secret Santa",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = ChristmasColors.White
                            )
                        }
                    },
                    actions = {
                        if (state.isOrganizer) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = ChristmasColors.AppButtonRed
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChristmasColors.AppBackground,
                        titleContentColor = ChristmasColors.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ChristmasColors.White)
                }
            } else if (state.secretSanta != null) {
                val secretSanta = state.secretSanta!!
                val userParticipant = secretSanta.participants.find {
                    it.userId == currentUserId || it.email.equals(currentUserEmail, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ========== CARTE PRINCIPALE ==========
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ChristmasColors.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(12.dp)
                        ) {
                            Box {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Icône
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(ChristmasColors.AuthBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "🎄", fontSize = 56.sp)
                                    }


                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Nom
                                    Text(
                                        text = secretSanta.name,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 26.sp
                                        ),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "📅",
                                            fontSize = 28.sp
                                        )
                                        Text(
                                            text = formatDate(secretSanta.deadline),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // ========== PARTICIPANTS | BUDGET (plus de date au milieu) ==========
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // GAUCHE : Participants
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = "👥 ${secretSanta.participants.size}",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }

                                        // DROITE : Budget
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (!secretSanta.budget.isNullOrEmpty()) {
                                                    "💰 ${secretSanta.budget}€"
                                                } else {
                                                    "💰 --"
                                                },
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ========== DESCRIPTION ==========
                    if (!secretSanta.description.isNullOrEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1565C0)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = "📝", fontSize = 24.sp)
                                    Text(
                                        text = secretSanta.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = ChristmasColors.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }


                    // ========== SI TIRAGE NON FAIT ==========
                    if (!secretSanta.drawDone) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = ChristmasColors.AppButtonRed
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Texte "Tirage non effectué"
                                    Text(
                                        text = "⏳ Tirage non effectué",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ChristmasColors.White,
                                        fontSize = 18.sp
                                    )

                                    // Bouton TIRER AU SORT (organisateur uniquement)
                                    if (state.isOrganizer) {
                                        Button(
                                            onClick = { viewModel.onEvent(SecretSantaDetailsEvent.PerformDraw) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !state.isPerformingDraw,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ChristmasColors.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (state.isPerformingDraw) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = ChristmasColors.AppButtonRed,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    tint = ChristmasColors.AppButtonRed
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Effectuer le tirage au sort",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ChristmasColors.AppButtonRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }




                    // ========== QUI VOUS AVEZ PIOCHÉ (CARTE SÉPARÉE VISIBLE) ==========
                    if (secretSanta.drawDone && userParticipant != null) {
                        val gifteeId = secretSanta.assignments[userParticipant.id]
                        val giftee = secretSanta.participants.find { it.id == gifteeId }

                        if (giftee != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = ChristmasColors.AppButtonRed.copy(alpha = 0.95f)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier.size(100.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Cercle blanc derrière
                                            Surface(
                                                modifier = Modifier.size(1000.dp),
                                                shape = CircleShape,
                                                color = ChristmasColors.White,
                                                shadowElevation = 4.dp
                                            ) {}

                                            // Emoji Père Noël par dessus
                                            Text(
                                                text = "🎅",
                                                fontSize = 64.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Vous offrez à :",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = ChristmasColors.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = giftee.name,
                                            style = MaterialTheme.typography.displaySmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 36.sp
                                            ),
                                            color = ChristmasColors.White,
                                             textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Bouton SA Liste de souhaits
                                        Button(
                                            onClick = { // clic voir wishlist filleul
                                                val userId = giftee.userId
                                                if (userId.isNullOrBlank()) {
                                                    // Affiche un message d'erreur si le userId est vide
                                                    viewModel.onEvent(
                                                        SecretSantaDetailsEvent.ShowError("Ce participant n'a pas encore créé de compte")
                                                    )
                                                } else {
                                                    navController.navigate(Screen.UserWishlist.createRoute(santaId, userId))
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ChristmasColors.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(text = "🎁", fontSize = 24.sp)
                                                Text(
                                                    "Voir sa liste de souhaits",
                                                    color = ChristmasColors.AppButtonRed,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Bouton Messagerie
                                        Button(
                                            onClick = {
                                                navController.navigate(Screen.Messaging.route)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ChristmasColors.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Message,
                                                    contentDescription = null,
                                                    tint = ChristmasColors.AppButtonRed
                                                )
                                                Text(
                                                    "Lui envoyer un message",
                                                    color = ChristmasColors.AppButtonRed,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }



                    // ========== MA LISTE DE CADEAUX (PERSO) ==========
                    item {
                        Button(
                            onClick = {
                                navController.navigate(Screen.Wishlist.route)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFCC11B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = null,
                                    tint = ChristmasColors.White
                                )
                                Text(
                                    "Ma liste de cadeaux",
                                    color = ChristmasColors.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }


                    // ========== BOUTON CALENDRIER (TOUJOURS VISIBLE) ==========
                    item {
                        Button(
                            onClick = {
                                navController.navigate(Screen.Calendar.route)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChristmasColors.AuthBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = ChristmasColors.White
                                )
                                Text(
                                    "Voir le calendrier",
                                    color = ChristmasColors.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    // ========== TITRE PARTICIPANTS ==========
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Participants (${secretSanta.participants.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ChristmasColors.White,
                                fontSize = 22.sp
                            )
                        }
                    }

                    // ========== LISTE PARTICIPANTS ==========
                    items(secretSanta.participants) { participant ->
                        val isYourGiftee = secretSanta.drawDone &&
                                userParticipant != null &&
                                secretSanta.assignments[userParticipant.id] == participant.id
                        val isCurrentUser = participant.userId == currentUserId ||
                                participant.email.equals(currentUserEmail, ignoreCase = true)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ChristmasColors.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = if (isYourGiftee)
                                        ChristmasColors.AppButtonRed.copy(alpha = 0.15f)
                                    else
                                        ChristmasColors.AppBackground.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = when {
                                                participant.isOrganizer -> "👑"
                                                isYourGiftee -> "🎁"
                                                isCurrentUser -> "🎅"
                                                else -> "🎄"
                                            },
                                            fontSize = 28.sp
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = participant.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }

                                    Text(
                                        text = participant.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (participant.isOrganizer) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = ChristmasColors.AppButtonRed.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Organisateur",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ChristmasColors.AppButtonRed
                                            )
                                        }
                                    }
                                }

                                if (isCurrentUser) {
                                    Surface(
                                        color = ChristmasColors.AppBackground,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "Vous",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ChristmasColors.White
                                        )
                                    }
                                }

                                // Corbeille (organisateur uniquement)
                                if (state.isOrganizer && !isCurrentUser && !secretSanta.drawDone) {
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(
                                                SecretSantaDetailsEvent.RemoveParticipant(participant.id)
                                            )
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = ChristmasColors.AppButtonRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ========== ERREUR ==========
                    if (state.error != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = ChristmasColors.AppButtonRed.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.error!!,
                                        color = ChristmasColors.White,
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.onEvent(SecretSantaDetailsEvent.DismissError)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Fermer",
                                            tint = ChristmasColors.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
    return sdf.format(Date(timestamp))
}
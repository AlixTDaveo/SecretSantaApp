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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.R
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSantaDetailsScreen(
    navController: NavController,
    santaId: String,
    viewModel: SecretSantaDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
        // Fond avec dégradé et flocons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ChristmasColors.AppBackground,
                            ChristmasColors.AppBackground.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            SnowfallBackground(snowflakeCount = 80)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Détails") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = ChristmasColors.AppButtonRed
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChristmasColors.AppBackground,
                        titleContentColor = ChristmasColors.White,
                        navigationIconContentColor = ChristmasColors.White
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // En-tête avec image et infos
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ChristmasColors.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Icône centrée
                                Surface(
                                    modifier = Modifier.size(100.dp),
                                    shape = CircleShape,
                                    color = ChristmasColors.AppBackground.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "🎁", fontSize = 56.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Nom centré
                                Text(
                                    text = secretSanta.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = ChristmasColors.AppBackground
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Date limite centrée
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = ChristmasColors.AppBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Date limite: ${formatDate(secretSanta.deadline)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Nb participants centré
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ChristmasColors.AppBackground,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${secretSanta.participants.size} participants",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Statut tirage
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (secretSanta.drawDone)
                                    ChristmasColors.AppBackground.copy(alpha = 0.2f)
                                else
                                    ChristmasColors.AppButtonRed.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (secretSanta.drawDone) "✅" else "⏳",
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = if (secretSanta.drawDone) "Tirage effectué" else "Tirage non effectué",
                                        fontWeight = FontWeight.Bold,
                                        color = ChristmasColors.White
                                    )
                                }

                                if (!secretSanta.drawDone) {
                                    Button(
                                        onClick = {
                                            viewModel.onEvent(SecretSantaDetailsEvent.PerformDraw)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ChristmasColors.AppButtonRed
                                        ),
                                        enabled = !state.isPerformingDraw,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (state.isPerformingDraw) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = ChristmasColors.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("Tirer au sort")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Votre attribution
                    if (secretSanta.drawDone && secretSanta.assignments.containsKey(currentUserId)) {
                        item {
                            val gifteeId = secretSanta.assignments[currentUserId]
                            val giftee = secretSanta.participants.find { it.id == gifteeId }

                            if (giftee != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = ChristmasColors.SkyBlue.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🎅", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Vous offrez à :",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = ChristmasColors.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = giftee.name,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ChristmasColors.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Titre participants
                    item {
                        Text(
                            text = "Participants (${secretSanta.participants.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ChristmasColors.White
                        )
                    }

                    // Liste participants
                    items(secretSanta.participants) { participant ->
                        val isYourGiftee = secretSanta.drawDone &&
                                secretSanta.assignments[currentUserId] == participant.id

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isYourGiftee)
                                    ChristmasColors.SkyBlue.copy(alpha = 0.3f)
                                else
                                    ChristmasColors.White
                            ),
                            border = if (isYourGiftee)
                                BorderStroke(3.dp, ChristmasColors.AppBackground)
                            else
                                null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(50.dp),
                                    shape = CircleShape,
                                    color = ChristmasColors.AppBackground.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = participant.name.first().uppercase(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ChristmasColors.AppBackground
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = participant.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (isYourGiftee) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "🎁", fontSize = 24.sp)
                                        }
                                    }
                                    Text(
                                        text = participant.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (participant.id == currentUserId) {
                                    Surface(
                                        color = ChristmasColors.AppBackground,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Vous",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ChristmasColors.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Erreur
                    if (state.error != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = ChristmasColors.AppButtonRed.copy(alpha = 0.2f)
                                )
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
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
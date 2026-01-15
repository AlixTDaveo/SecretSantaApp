package com.example.secretsanta.ui.feature.secretsanta.create

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.R
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSecretSantaScreen(
    navController: NavController,
    viewModel: CreateSecretSantaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
        }
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
            SnowfallBackground(snowflakeCount = 80)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Créer un Secret Santa",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ChristmasColors.AppBackground,
                        titleContentColor = ChristmasColors.White,
                        navigationIconContentColor = ChristmasColors.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ========== INFORMATIONS GÉNÉRALES ==========
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ChristmasColors.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "ℹ️", fontSize = 24.sp)
                                Text(
                                    text = "Informations générales",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ChristmasColors.AppBackground
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nom du groupe
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = {
                                    viewModel.onEvent(CreateSecretSantaEvent.NameChanged(it))
                                },
                                label = { Text("Nom du groupe *") },
                                placeholder = { Text("Ex: Famille Noël 2025") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Text(text = "🎄", fontSize = 20.sp)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.AppBackground,
                                    focusedLabelColor = ChristmasColors.AppBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Date limite
                            OutlinedTextField(
                                value = if (state.deadline == 0L) "" else formatDate(state.deadline),
                                onValueChange = {},
                                label = { Text("Date limite *") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                leadingIcon = {
                                    Text(text = "📅", fontSize = 20.sp)
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, day ->
                                                    calendar.set(year, month, day)
                                                    viewModel.onEvent(
                                                        CreateSecretSantaEvent.DeadlineChanged(
                                                            calendar.timeInMillis
                                                        )
                                                    )
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.AppBackground,
                                    focusedLabelColor = ChristmasColors.AppBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Budget
                            OutlinedTextField(
                                value = state.budget,
                                onValueChange = {
                                    viewModel.onEvent(CreateSecretSantaEvent.BudgetChanged(it))
                                },
                                label = { Text("Budget maximum") },
                                placeholder = { Text("Ex: 30€") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Text(text = "💰", fontSize = 20.sp)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.AppBackground,
                                    focusedLabelColor = ChristmasColors.AppBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Description
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = {
                                    viewModel.onEvent(CreateSecretSantaEvent.DescriptionChanged(it))
                                },
                                label = { Text("Description / Informations") },
                                placeholder = { Text("Ex: RDV à la cafet le 24 décembre à midi") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                leadingIcon = {
                                    Text(text = "📝", fontSize = 20.sp)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.AppBackground,
                                    focusedLabelColor = ChristmasColors.AppBackground
                                )
                            )
                        }
                    }
                }

                // ========== AJOUTER UN PARTICIPANT ==========
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ChristmasColors.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "👥", fontSize = 24.sp)
                                Text(
                                    text = "Ajouter un participant",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ChristmasColors.AppBackground
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.currentParticipantName,
                                onValueChange = {
                                    viewModel.onEvent(
                                        CreateSecretSantaEvent.ParticipantNameChanged(it)
                                    )
                                },
                                label = { Text("Nom *") },
                                placeholder = { Text("Ex: Marie Dupont") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.SkyBlue,
                                    focusedLabelColor = ChristmasColors.SkyBlue
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = state.currentParticipantEmail,
                                onValueChange = {
                                    viewModel.onEvent(
                                        CreateSecretSantaEvent.ParticipantEmailChanged(it)
                                    )
                                },
                                label = { Text("Email *") },
                                placeholder = { Text("Ex: marie@exemple.com") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ChristmasColors.SkyBlue,
                                    focusedLabelColor = ChristmasColors.SkyBlue
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.onEvent(CreateSecretSantaEvent.AddParticipant)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChristmasColors.SkyBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Ajouter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // ========== LISTE DES PARTICIPANTS ==========
                if (state.participants.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Participants (${state.participants.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ChristmasColors.White,
                                fontSize = 20.sp
                            )
                            Text(text = "🎅", fontSize = 24.sp)
                        }
                    }

                    items(state.participants) { participant ->
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = ChristmasColors.SkyBlue.copy(alpha = 0.2f)
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
                                    Column {
                                        Text(
                                            text = participant.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = participant.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(
                                            CreateSecretSantaEvent.RemoveParticipant(participant.id)
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
                                containerColor = ChristmasColors.AppButtonRed.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "⚠️", fontSize = 24.sp)
                                Text(
                                    text = state.error!!,
                                    color = ChristmasColors.AppButtonRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ========== BOUTON CRÉER ==========
                item {
                    Button(
                        onClick = {
                            viewModel.onEvent(CreateSecretSantaEvent.CreateSecretSanta)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChristmasColors.AppButtonRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = ChristmasColors.White,
                                strokeWidth = 3.dp
                            )
                    } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🎁", fontSize = 28.sp)
                                Text(
                                    text = "Créer le Secret Santa",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // Espace en bas
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
    return sdf.format(Date(timestamp))
}
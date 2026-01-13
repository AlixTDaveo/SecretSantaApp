package com.example.secretsanta.ui.feature.secretsanta.create

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.R
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_secret_santa_title)) },
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
        containerColor = ChristmasColors.AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nom du groupe
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ChristmasColors.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.group_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = {
                                viewModel.onEvent(CreateSecretSantaEvent.NameChanged(it))
                            },
                            placeholder = { Text(stringResource(R.string.group_name_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Date limite
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ChristmasColors.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.deadline),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
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
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.deadline == 0L) {
                                    stringResource(R.string.select_date)
                                } else {
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(Date(state.deadline))
                                }
                            )
                        }
                    }
                }
            }

            // Ajouter un participant
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ChristmasColors.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.add_participant),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.currentParticipantName,
                            onValueChange = {
                                viewModel.onEvent(
                                    CreateSecretSantaEvent.ParticipantNameChanged(it)
                                )
                            },
                            placeholder = { Text(stringResource(R.string.participant_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.currentParticipantEmail,
                            onValueChange = {
                                viewModel.onEvent(
                                    CreateSecretSantaEvent.ParticipantEmailChanged(it)
                                )
                            },
                            placeholder = { Text(stringResource(R.string.participant_email)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.onEvent(CreateSecretSantaEvent.AddParticipant)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChristmasColors.SkyBlue
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_participant))
                        }
                    }
                }
            }

            // Liste des participants
            item {
                Text(
                    text = "${stringResource(R.string.participants)} (${state.participants.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ChristmasColors.White
                )
            }

            items(state.participants) { participant ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ChristmasColors.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = participant.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = participant.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                contentDescription = stringResource(R.string.remove_participant),
                                tint = ChristmasColors.AppButtonRed
                            )
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
                            containerColor = ChristmasColors.AppButtonRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = state.error!!,
                            color = ChristmasColors.AppButtonRed,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Bouton créer
            item {
                Button(
                    onClick = {
                        viewModel.onEvent(CreateSecretSantaEvent.CreateSecretSanta)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChristmasColors.AppButtonRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ChristmasColors.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.create_group),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
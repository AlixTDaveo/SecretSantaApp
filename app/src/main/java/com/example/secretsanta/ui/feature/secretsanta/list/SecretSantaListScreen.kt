package com.example.secretsanta.ui.feature.secretsanta.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.R
import com.example.secretsanta.ui.components.ChristmasScreen
import com.example.secretsanta.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSantaListScreen(
    navController: NavController,
    viewModel: SecretSantaListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond rouge festif avec flocons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ChristmasColors.AuthBackground,
                            ChristmasColors.AuthBackground.copy(alpha = 0.85f),
                        )
                    )
                )
        ) {
            SnowfallBackground(
                snowflakeCount = 120,
                snowColor = Color.White.copy(alpha = 0.9f)
            )
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.my_secret_santas),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = ChristmasColors.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.CreateSecretSanta.route)
                    },
                    containerColor = ChristmasColors.AppButtonRed,
                    contentColor = ChristmasColors.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Créer")
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = ChristmasColors.White
                        )
                    }
                    state.secretSantas.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_secret_santas),
                                style = MaterialTheme.typography.bodyLarge,
                                color = ChristmasColors.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    navController.navigate(Screen.CreateSecretSanta.route)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChristmasColors.AppButtonRed
                                )
                            ) {
                                Text(
                                    "Créer mon premier Secret Santa",
                                    color = ChristmasColors.White
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.secretSantas) { secretSanta ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navController.navigate(
                                            Screen.SecretSantaDetails.createRoute(secretSanta.id)
                                        )
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = ChristmasColors.White
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = secretSanta.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = ChristmasColors.AppButtonRed
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Date limite : ${formatDate(secretSanta.deadline)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${secretSanta.participants.size} participants",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ChristmasColors.AppBackground
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
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
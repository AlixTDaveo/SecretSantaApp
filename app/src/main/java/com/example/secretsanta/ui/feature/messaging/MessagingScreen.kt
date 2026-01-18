package com.example.secretsanta.ui.feature.messaging

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secretsanta.R
import com.example.secretsanta.ui.navigation.Screen
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    navController: NavController,
    viewModel: MessagingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond vert festif avec flocons
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
                            text = stringResource(R.string.messaging_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = ChristmasColors.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = ChristmasColors.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                state.info?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = ChristmasColors.White
                    )
                }

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = ChristmasColors.White
                        )
                    }
                    return@Scaffold
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = ChristmasColors.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (state.conversations.isEmpty()) {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(R.string.messaging_empty),
                            modifier = Modifier.align(Alignment.Center),
                            color = ChristmasColors.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    return@Scaffold
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.conversations) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) {
                                    navController.navigate(Screen.Chat.createRoute(c.id))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = ChristmasColors.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    text = c.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ChristmasColors.AppButtonRed
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = c.lastMessage ?: stringResource(R.string.messaging_no_message_yet),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (c.lastMessage != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
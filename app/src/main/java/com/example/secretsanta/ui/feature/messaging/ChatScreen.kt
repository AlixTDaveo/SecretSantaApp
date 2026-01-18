package com.example.secretsanta.ui.feature.messaging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors
import androidx.compose.ui.res.stringResource
import com.example.secretsanta.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

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
                            stringResource(R.string.chat_title),

                            color = ChristmasColors.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text(
                                stringResource(R.string.back),

                                color = ChristmasColors.White
                            )
                        }
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
                    .padding(12.dp)
            ) {
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
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { m ->
                        val isMe = m.senderId == currentUid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                tonalElevation = 2.dp,
                                shape = MaterialTheme.shapes.medium,
                                color = if (isMe) {
                                    ChristmasColors.AppButtonRed
                                } else {
                                    ChristmasColors.White
                                }
                            ) {
                                Text(
                                    text = m.text,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = if (isMe) TextAlign.End else TextAlign.Start,
                                    color = if (isMe) {
                                        ChristmasColors.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Barre de saisie blanche
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = ChristmasColors.White,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = viewModel::onInputChanged,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChristmasColors.White,
                            unfocusedContainerColor = ChristmasColors.White,
                            focusedBorderColor = ChristmasColors.AppButtonRed,
                            unfocusedBorderColor = ChristmasColors.AppBackground
                        ),
                        placeholder = {
                            Text(stringResource(R.string.message_placeholder))

                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = viewModel::send,
                        enabled = !state.isSending,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChristmasColors.AppButtonRed,
                            contentColor = ChristmasColors.White
                        )
                    ) {
                        Text(stringResource(R.string.send))


                    }
                }
            }
        }
    }
}
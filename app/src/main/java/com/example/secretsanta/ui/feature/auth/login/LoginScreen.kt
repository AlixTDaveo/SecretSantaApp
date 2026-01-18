package com.example.secretsanta.ui.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secretsanta.R
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ChristmasColors.AuthBackground,
                        ChristmasColors.AuthBackground.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        // Flocons
        SnowfallBackground(
            snowflakeCount = 100,
            snowColor = Color.White
        )

        // Décorations en haut
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "❄️", fontSize = 32.sp)
            Text(text = "🎄", fontSize = 40.sp)
            Text(text = "🎁", fontSize = 36.sp)
            Text(text = "🎄", fontSize = 40.sp)
            Text(text = "❄️", fontSize = 32.sp)
        }

        // Contenu centré
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card blanche
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ChristmasColors.White
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChristmasColors.AuthBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
                        label = { Text(stringResource(R.string.login_email_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChristmasColors.AuthBackground,
                            focusedLabelColor = ChristmasColors.AuthBackground,
                            cursorColor = ChristmasColors.AuthBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mot de passe
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
                        label = { Text(stringResource(R.string.login_password_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = ChristmasColors.AuthBackground
                                )
                            }
                        },
                        enabled = !state.isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChristmasColors.AuthBackground,
                            focusedLabelColor = ChristmasColors.AuthBackground,
                            cursorColor = ChristmasColors.AuthBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Erreur
                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = ChristmasColors.AuthButtonRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Bouton Connexion
                    Button(
                        onClick = { viewModel.onEvent(LoginEvent.Login) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChristmasColors.AuthButtonRed,
                            contentColor = ChristmasColors.White
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
                                text = stringResource(R.string.login_button),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lien inscription
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = stringResource(R.string.login_no_account),
                            color = ChristmasColors.AuthButtonRed,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Père Noël en bas
            Text(
                text = "🎅",
                fontSize = 64.sp
            )
        }
    }
}
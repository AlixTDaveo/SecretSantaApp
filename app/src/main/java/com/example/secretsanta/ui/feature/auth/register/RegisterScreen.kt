package com.example.secretsanta.ui.feature.auth.register

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
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onRegisterSuccess()
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
        SnowfallBackground(snowflakeCount = 100, snowColor = Color.White)

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ChristmasColors.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Créer un compte",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChristmasColors.AuthBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = { viewModel.onEvent(RegisterEvent.DisplayNameChanged(it)) },
                        label = { Text("Nom d'affichage") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChristmasColors.AuthBackground,
                            focusedLabelColor = ChristmasColors.AuthBackground,
                            cursorColor = ChristmasColors.AuthBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChristmasColors.AuthBackground,
                            focusedLabelColor = ChristmasColors.AuthBackground,
                            cursorColor = ChristmasColors.AuthBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
                        label = { Text("Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                        label = { Text("Confirmer le mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    null,
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

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.passwordError != null) {
                        Text(
                            text = when (state.passwordError) {
                                "password_too_short" -> stringResource(R.string.password_too_short)
                                "password_no_uppercase" -> stringResource(R.string.password_no_uppercase)
                                "password_no_lowercase" -> stringResource(R.string.password_no_lowercase)
                                "password_no_digit" -> stringResource(R.string.password_no_digit)
                                "password_no_special" -> stringResource(R.string.password_no_special)
                                "passwords_dont_match" -> stringResource(R.string.passwords_dont_match)
                                else -> state.passwordError!!
                            },
                            color = ChristmasColors.AuthButtonRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = ChristmasColors.AuthButtonRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.onEvent(RegisterEvent.Register) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChristmasColors.AuthButtonRed,
                            contentColor = ChristmasColors.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = ChristmasColors.White, strokeWidth = 2.dp)
                        } else {
                            Text("S'inscrire", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            "Déjà un compte ?",
                            color = ChristmasColors.AuthButtonRed,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "🎅", fontSize = 64.sp)
        }
    }
}
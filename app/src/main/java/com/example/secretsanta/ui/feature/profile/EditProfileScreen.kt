package com.example.secretsanta.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secretsanta.R
import com.example.secretsanta.ui.theme.ChristmasColors

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ChristmasColors.AuthBackground,
                        ChristmasColors.AuthBackground.copy(alpha = 0.92f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ChristmasColors.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = ChristmasColors.AuthBackground
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.edit_profile_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ChristmasColors.AuthBackground
                        )
                    )
                }

                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.DisplayNameChanged(it)) },
                    label = { Text(stringResource(R.string.edit_profile_display_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChristmasColors.AuthBackground,
                        focusedLabelColor = ChristmasColors.AuthBackground,
                        cursorColor = ChristmasColors.AuthBackground
                    )
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.edit_profile_password_section),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ChristmasColors.DarkText
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.oldPassword,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.OldPasswordChanged(it)) },
                    label = { Text(stringResource(R.string.edit_profile_old_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving,
                    visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { oldVisible = !oldVisible }) {
                            Icon(
                                imageVector = if (oldVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = ChristmasColors.AuthBackground
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChristmasColors.AuthBackground,
                        focusedLabelColor = ChristmasColors.AuthBackground,
                        cursorColor = ChristmasColors.AuthBackground
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.newPassword,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.NewPasswordChanged(it)) },
                    label = { Text(stringResource(R.string.edit_profile_new_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving,
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                imageVector = if (newVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = ChristmasColors.AuthBackground
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChristmasColors.AuthBackground,
                        focusedLabelColor = ChristmasColors.AuthBackground,
                        cursorColor = ChristmasColors.AuthBackground
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.confirmNewPassword,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.ConfirmNewPasswordChanged(it)) },
                    label = { Text(stringResource(R.string.edit_profile_confirm_new_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = ChristmasColors.AuthBackground
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChristmasColors.AuthBackground,
                        focusedLabelColor = ChristmasColors.AuthBackground,
                        cursorColor = ChristmasColors.AuthBackground
                    )
                )

                Spacer(Modifier.height(18.dp))

                state.error?.let {
                    Text(
                        text = it,
                        color = ChristmasColors.AuthButtonRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Button(
                    onClick = { viewModel.onEvent(EditProfileEvent.Save) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !state.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChristmasColors.AuthButtonRed,
                        contentColor = ChristmasColors.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ChristmasColors.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.save),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

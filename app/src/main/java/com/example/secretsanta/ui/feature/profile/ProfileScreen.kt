package com.example.secretsanta.ui.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.secretsanta.R
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.theme.ChristmasColors

@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChristmasColors.AuthBackground)
            .padding(24.dp)
    ) {
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
        ) {
            SnowfallBackground(
                snowflakeCount = 80,
                snowColor = Color.White.copy(alpha = 0.8f)
            )
        }

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
                    .padding(28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ChristmasColors.AuthBackground
                    )
                )

                Spacer(Modifier.height(20.dp))

                if (state.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(text = stringResource(R.string.loading))
                    }
                    return@Column
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = ChristmasColors.AuthButtonRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                }

                val user = state.user
                if (user == null) {
                    Text(text = stringResource(R.string.profile_no_user))
                } else {
                    Text(
                        text = stringResource(R.string.profile_display_name_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(text = user.displayName, style = MaterialTheme.typography.bodyLarge)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.profile_email_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(text = user.email, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = onNavigateToEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChristmasColors.AuthButtonRed,
                        contentColor = ChristmasColors.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.profile_edit_button),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ChristmasColors.AuthButtonRed
                    )
                ) {
                    Text(text = stringResource(R.string.profile_logout_button))
                }
            }
        }
    }
}

package com.example.secretsanta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChristmasAuthScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),  // Vert foncé en haut
                        Color(0xFF2E7D32)   // Vert moyen en bas
                    )
                )
            )
    ) {
        // Flocons de neige en arrière-plan
        SnowfallBackground(
            snowflakeCount = 60,
            snowColor = Color.White.copy(alpha = 0.9f)
        )

        // Décorations de Noël en haut
        ChristmasDecorations(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        )

        // Card blanche arrondie pour le contenu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                }
            }
        }

        // Décorations en bas
        ChristmasDecorationsBottom(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 40.dp, end = 24.dp)
        )
    }
}

@Composable
fun ChristmasDecorations(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emojis de Noël
        androidx.compose.material3.Text(
            text = "🎄",
            style = MaterialTheme.typography.displaySmall
        )
        androidx.compose.material3.Text(
            text = "🎁",
            style = MaterialTheme.typography.displayMedium
        )
        androidx.compose.material3.Text(
            text = "🎄",
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
fun ChristmasDecorationsBottom(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.Text(
            text = "🎁",
            style = MaterialTheme.typography.headlineLarge
        )
        androidx.compose.material3.Text(
            text = "🔔",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
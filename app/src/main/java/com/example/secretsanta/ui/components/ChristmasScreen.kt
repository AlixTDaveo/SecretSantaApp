package com.example.secretsanta.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ChristmasScreen(
    modifier: Modifier = Modifier,
    showSnowfall: Boolean = true,
    snowflakeCount: Int = 50,
    snowColor: Color = Color.White.copy(alpha = 0.8f),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Flocons en arrière-plan
        if (showSnowfall) {
            SnowfallBackground(
                snowflakeCount = snowflakeCount,
                snowColor = snowColor
            )
        }

        // Contenu de la page par-dessus
        content()
    }
}
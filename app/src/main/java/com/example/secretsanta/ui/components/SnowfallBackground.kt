package com.example.secretsanta.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

data class Snowflake(
    val id: Int,
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float
)

@Composable
fun SnowfallBackground(
    modifier: Modifier = Modifier,
    snowflakeCount: Int = 50,
    snowColor: Color = Color.White.copy(alpha = 0.8f)
) {
    val density = LocalDensity.current

    var snowflakes by remember {
        mutableStateOf(
            List(snowflakeCount) { index ->
                Snowflake(
                    id = index,
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * -0.5f, // Démarrent au-dessus de l'écran
                    speed = Random.nextFloat() * 0.5f + 0.3f, // 0.3 à 0.8
                    size = Random.nextFloat() * 6f + 3f, // 3 à 9 dp
                    swayAmplitude = Random.nextFloat() * 20f + 10f, // 10 à 30
                    swayFrequency = Random.nextFloat() * 2f + 1f // 1 à 3
                )
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snowfall")
    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowfall_time"
    )

    LaunchedEffect(animationTime) {
        snowflakes = snowflakes.map { snowflake ->
            val newY = snowflake.y + (snowflake.speed * 0.005f)
            val sway = sin(animationTime * snowflake.swayFrequency * 0.001f) * 0.02f

            if (newY > 1.1f) {
                // Réinitialise au sommet
                snowflake.copy(
                    y = -0.1f,
                    x = Random.nextFloat()
                )
            } else {
                snowflake.copy(
                    y = newY,
                    x = (snowflake.x + sway).coerceIn(0f, 1f)
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        snowflakes.forEach { snowflake ->
            val sizePx = with(density) { snowflake.size.dp.toPx() }

            drawCircle(
                color = snowColor,
                radius = sizePx,
                center = Offset(
                    x = snowflake.x * width,
                    y = snowflake.y * height
                ),
                alpha = 0.8f
            )
        }
    }
}
package com.example.secretsanta.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Snowflake(
    val id: Int,
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val drift: Float, // Léger mouvement horizontal
    var time: Float = 0f
)

@Composable
fun SnowfallBackground(
    modifier: Modifier = Modifier,
    snowflakeCount: Int = 100,
    snowColor: Color = Color.White
) {
    var snowflakes by remember {
        mutableStateOf(
            List(snowflakeCount) { index ->
                Snowflake(
                    id = index,
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * -1f, // Commence au-dessus
                    speed = Random.nextFloat() * 0.3f + 0.15f, // Lent
                    size = Random.nextFloat() * 2f + 1.5f, // Petits : 1.5-3.5 dp
                    drift = Random.nextFloat() * 0.3f - 0.15f // Léger drift
                )
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snowfall")
    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = Float.MAX_VALUE,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 50, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    LaunchedEffect(animationTime) {
        snowflakes = snowflakes.map { flake ->
            val newY = flake.y + (flake.speed * 0.002f)
            val newTime = flake.time + 0.05f
            val horizontalOffset = sin(newTime) * flake.drift * 0.005f

            if (newY > 1.2f) {
                // Réinitialise en haut
                Snowflake(
                    id = flake.id,
                    x = Random.nextFloat(),
                    y = -0.1f,
                    speed = Random.nextFloat() * 0.3f + 0.15f,
                    size = Random.nextFloat() * 2f + 1.5f,
                    drift = Random.nextFloat() * 0.3f - 0.15f,
                    time = 0f
                )
            } else {
                flake.copy(
                    y = newY,
                    x = (flake.x + horizontalOffset).coerceIn(0f, 1f),
                    time = newTime
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        snowflakes.forEach { flake ->
            val x = flake.x * size.width
            val y = flake.y * size.height
            val sizePx = flake.size * density

            // Flocon simple : petite étoile à 6 branches
            drawSimpleSnowflake(
                center = Offset(x, y),
                size = sizePx,
                color = snowColor
            )
        }
    }
}

private fun DrawScope.drawSimpleSnowflake(
    center: Offset,
    size: Float,
    color: Color
) {
    // Petit cercle central
    drawCircle(
        color = color,
        radius = size * 0.8f,
        center = center,
        alpha = 0.9f
    )

    // 6 petites branches
    for (i in 0 until 6) {
        val angle = (i * 60f)
        rotate(angle, center) {
            val path = Path().apply {
                moveTo(center.x, center.y)
                lineTo(center.x, center.y - size * 1.5f)
            }
            drawPath(path, color, alpha = 0.7f)
        }
    }
}
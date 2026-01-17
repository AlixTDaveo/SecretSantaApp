package com.example.secretsanta.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Confetti(
    val startX: Float,
    val startY: Float,
    val color: Color,
    val size: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiExplosion(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    var confettiList by remember { mutableStateOf<List<Confetti>>(emptyList()) }

    // Animation progress (0f à 1f sur 1.5 secondes)
    val animationProgress by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            if (it == 1f) {
                // Reset après l'animation
                confettiList = emptyList()
            }
        }
    )

    // Générer les confettis au déclenchement
    LaunchedEffect(trigger) {
        if (trigger) {
            confettiList = List(200) { // 200 paillettes
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val speed = Random.nextFloat() * 300f + 200f

                Confetti(
                    startX = 0.5f, // Centre horizontal (proportion)
                    startY = 0.4f, // Centre vertical (proportion)
                    color = listOf(
                        Color(0xFFFFD700), // Or
                        Color(0xFFD32F2F), // Rouge Noël
                        Color(0xFF1B5E20), // Vert sapin
                        Color(0xFFFFFFFF), // Blanc neige
                        Color(0xFFC0C0C0)  // Argent
                    ).random(),
                    size = Random.nextFloat() * 8f + 4f,
                    velocityX = cos(angle) * speed,
                    velocityY = sin(angle) * speed - 100f, // Légère poussée vers le haut
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 720f - 360f
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (animationProgress > 0f) {
            confettiList.forEach { confetti ->
                val progress = animationProgress

                // Position avec gravité
                val x = size.width * confetti.startX + confetti.velocityX * progress
                val y = size.height * confetti.startY +
                        confetti.velocityY * progress +
                        500f * progress * progress // Gravité

                // Rotation
                val rotation = confetti.rotation + confetti.rotationSpeed * progress

                // Opacité (disparaît à la fin)
                val alpha = (1f - progress * 0.7f).coerceAtLeast(0f)

                // Dessiner le confetti
                rotate(rotation, pivot = Offset(x, y)) {
                    drawPath(
                        path = Path().apply {
                            // Forme de petit rectangle/paillette
                            moveTo(x - confetti.size / 2, y - confetti.size * 1.5f)
                            lineTo(x + confetti.size / 2, y - confetti.size * 1.5f)
                            lineTo(x + confetti.size / 2, y + confetti.size * 1.5f)
                            lineTo(x - confetti.size / 2, y + confetti.size * 1.5f)
                            close()
                        },
                        color = confetti.color.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}
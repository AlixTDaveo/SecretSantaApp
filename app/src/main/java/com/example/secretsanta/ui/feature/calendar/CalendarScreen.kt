package com.example.secretsanta.ui.feature.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.secretsanta.domain.model.SecretSanta
import com.example.secretsanta.ui.components.SnowfallBackground
import com.example.secretsanta.ui.navigation.Screen
import com.example.secretsanta.ui.theme.ChristmasColors
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.compose.material3.Divider
import java.time.format.DateTimeFormatter


private val SantaRed = ChristmasColors.AppButtonRed

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val locale = Locale.getDefault()
    val firstDayOfWeek = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val weekdayHeaders: List<String> = remember(locale, firstDayOfWeek) {
        buildWeekdayHeaders(firstDayOfWeek, locale)
    }
    val santasByDate = remember(state.secretSantas) {
        state.secretSantas.groupBy { it.deadline.toLocalDate() }
    }

    val monthCells = remember(state.currentMonth, firstDayOfWeek) {
        buildMonthCells(state.currentMonth, firstDayOfWeek)
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

        // Contenu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            CalendarHeader(
                currentMonth = state.currentMonth,
                locale = locale,
                onPrev = { viewModel.onEvent(CalendarEvent.PreviousMonth) },
                onNext = { viewModel.onEvent(CalendarEvent.NextMonth) }
            )

            Spacer(Modifier.height(12.dp))

            WeekdayRow(headers = weekdayHeaders)

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(monthCells) { dateOrNull ->
                    if (dateOrNull == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val count = santasByDate[dateOrNull].orEmpty().size
                        DayCell(
                            date = dateOrNull,
                            santasCount = count,
                            isSelected = state.selectedDate == dateOrNull,
                            onClick = { viewModel.onEvent(CalendarEvent.DayClicked(dateOrNull)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SelectedDayDetails(
                selectedDate = state.selectedDate,
                santas = state.selectedSantas,
                onOpenDetails = { santaId ->
                    navController.navigate(Screen.SecretSantaDetails.createRoute(santaId))
                }
            )
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    locale: Locale,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mois précédent",
                tint = Color.White
            )
        }

        val monthName = remember(currentMonth, locale) {
            currentMonth.month.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }

        Text(
            text = "$monthName ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = Color.White
        )

        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Mois suivant",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun WeekdayRow(headers: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        headers.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    santasCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasSanta = santasCount > 0
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current

    val bg = when {
        isSelected -> Color.White.copy(alpha = 0.22f)
        hasSanta -> SantaRed.copy(alpha = 0.18f)
        else -> Color.White.copy(alpha = 0.12f)
    }

    val modifier = Modifier
        .aspectRatio(1f)
        .clip(CircleShape)
        .background(bg)
        .clickable(
            enabled = true,
            interactionSource = interactionSource,
            indication = indication,
            onClick = onClick
        )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasSanta) SantaRed else Color.White
        )

        if (hasSanta) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SantaRed)
                )
                if (santasCount > 1) {
                    Text(
                        text = "+${santasCount - 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SantaRed
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedDayDetails(
    selectedDate: LocalDate?,
    santas: List<SecretSanta>,
    onOpenDetails: (String) -> Unit
) {
    // Affiche seulement si une date est sélectionnée
    if (selectedDate != null) {
        Spacer(Modifier.height(16.dp)) // Espace entre calendrier et liste

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ChristmasColors.White // Blanc
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Titre de la date
                Text(
                    text = "📅 ${formatSelectedDate(selectedDate)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ChristmasColors.AppButtonRed // Rouge
                )

                Spacer(Modifier.height(16.dp))

                when {
                    santas.isEmpty() -> {
                        Text(
                            text = "Aucun Secret Santa prévu ce jour-là 🎅",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            santas.forEach { santa ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onOpenDetails(santa.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = ChristmasColors.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)) // Bordure grise légère
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        // Nom du Secret Santa
                                        Text(
                                            text = santa.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ChristmasColors.AppButtonRed, // Rouge
                                            fontSize = 18.sp
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        Divider(
                                            color = ChristmasColors.AppBackground.copy(alpha = 0.2f),
                                            thickness = 1.dp
                                        )

                                        Spacer(Modifier.height(8.dp))

                                        // Infos
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Participants
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = "👥", fontSize = 16.sp)
                                                Text(
                                                    text = "${santa.participants.size} participants",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Black // Noir
                                                )
                                            }

                                            // Badge Tirage
                                            if (santa.drawDone) {
                                                Surface(
                                                    color = Color(0xFF2E7D32), // Vert foncé
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "✅ Tiré",
                                                        modifier = Modifier.padding(
                                                            horizontal = 10.dp,
                                                            vertical = 5.dp
                                                        ),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ChristmasColors.White,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- Helpers dates ---------------- */

private fun buildWeekdayHeaders(firstDay: DayOfWeek, locale: Locale): List<String> {
    val days = DayOfWeek.values().toList()
    val startIndex = days.indexOf(firstDay)
    val ordered = (0 until 7).map { i -> days[(startIndex + i) % 7] }
    return ordered.map { it.getDisplayName(TextStyle.SHORT, locale) }
}

private fun buildMonthCells(month: YearMonth, firstDayOfWeek: DayOfWeek): List<LocalDate?> {
    val firstOfMonth = month.atDay(1)
    val offset = weekOffset(firstOfMonth.dayOfWeek, firstDayOfWeek)

    val cells = mutableListOf<LocalDate?>()
    repeat(offset) { cells.add(null) }

    for (day in 1..month.lengthOfMonth()) {
        cells.add(month.atDay(day))
    }

    while (cells.size % 7 != 0) cells.add(null)
    return cells
}

private fun weekOffset(day: DayOfWeek, firstDayOfWeek: DayOfWeek): Int {
    val dayIndex = day.value % 7
    val firstIndex = firstDayOfWeek.value % 7
    return (dayIndex - firstIndex + 7) % 7
}

private fun Long.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

private fun formatSelectedDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)
    return date.format(formatter)
}

package com.example.myworkouts.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myworkouts.data.models.SavedWorkout
import com.example.myworkouts.util.generateCalendarWeeks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue


@Composable
fun CalendarScreen(
    savedWorkouts: List<SavedWorkout>,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }

    val calendarWeeks = generateCalendarWeeks(currentMonth)
    val workoutDates = remember(savedWorkouts) {
        savedWorkouts.map {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }

    var isSwipeLocked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun changeMonth(direction: Int) {
        if (!isSwipeLocked) {
            isSwipeLocked = true
            currentMonth = if (direction > 0) currentMonth.plusMonths(1) else currentMonth.minusMonths(1)

            coroutineScope.launch {
                delay(400)
                isSwipeLocked = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { changeMonth(-1) }) {
                Icon(Icons.Default.ArrowBack, "Предыдущий месяц")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { changeMonth(1) }) {
                Icon(Icons.Default.ArrowForward, "Следующий месяц")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (totalDrag.absoluteValue > 200 && !isSwipeLocked) {
                                changeMonth(if (totalDrag > 0) -1 else 1)
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    ) { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.width(8.dp))
                    Spacer(Modifier.width(4.dp))
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(calendarWeeks.size) { index ->
                        val week = calendarWeeks[index]
                        val isHighRep = index % 2 == 0
                        val stripColor = if (isHighRep) Color.Cyan else Color.Red

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(40.dp)
                                    .background(stripColor, RoundedCornerShape(3.dp))
                            )

                            Spacer(Modifier.width(4.dp))

                            week.days.forEach { date ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        val isToday = date == today
                                        val hasWorkout = workoutDates.contains(date)

                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onDayClick(date) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = when {
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    hasWorkout -> Color.Green.copy(alpha = 0.2f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = date.dayOfMonth.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (hasWorkout) Color.Green else MaterialTheme.colorScheme.onSurface
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .width(6.dp)
                    .height(16.dp)
                    .background(Color.Cyan, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(4.dp))
                Text("Многоповторная", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .width(6.dp)
                    .height(16.dp)
                    .background(Color.Red, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(4.dp))
                Text("Силовая", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
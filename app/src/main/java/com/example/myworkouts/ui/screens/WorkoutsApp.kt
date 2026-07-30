package com.example.myworkouts.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myworkouts.data.models.SavedWorkout
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.compareTo


@Composable
fun WorkoutsApp(
    navController: NavController,
    savedWorkouts: List<SavedWorkout>
) {
    val today = LocalDate.now()

    val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val dayEnd = dayStart + 24 * 60 * 60 * 1000

    val todayWorkouts = savedWorkouts.filter {
        it.date >= dayStart && it.date < dayEnd
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Мои тренировки",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        Text(
            text = currentDate,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (todayWorkouts.isEmpty()) {
                Text(
                    text = "Пока нет тренировок",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todayWorkouts.size) { index ->
                        val workout = todayWorkouts[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate("workout_detail_view/${workout.id}") }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = workout.name,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                if (workout.exercises.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = workout.exercises.joinToString(" | "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                navController.navigate("workout_detail/$today")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Добавить тренировку")
        }
    }
}
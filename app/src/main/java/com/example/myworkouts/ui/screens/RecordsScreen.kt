package com.example.myworkouts.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myworkouts.data.models.RecordData
import com.example.myworkouts.data.models.SavedWorkout


@Composable
fun RecordsScreen(
    savedWorkouts: List<SavedWorkout>,
    navController: NavController
) {
    val trackedExercises = listOf("подтягивания", "крюк", "верх", "боковое давление")

    val records = remember(savedWorkouts) {
        trackedExercises.associateWith { exercise ->
            var maxWeight = 0.0
            var maxReps = 0
            var recordId = 0L

            savedWorkouts.forEach { workout ->
                workout.setsData[exercise]?.forEach { setData ->
                    val weight = setData.weight.toDoubleOrNull() ?: 0.0


                    if (weight > maxWeight || (weight == maxWeight && setData.reps > maxReps)) {
                        maxWeight = weight
                        maxReps = setData.reps
                        recordId = workout.id
                    }
                }
            }

            RecordData(maxWeight = maxWeight, maxReps = maxReps, workoutId = recordId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Мои рекорды",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(trackedExercises.size) { index ->
                val exercise = trackedExercises[index]
                val record = records[exercise] ?: RecordData(maxWeight = 0.0, maxReps = 0)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (record.workoutId > 0) {
                                navController.navigate("workout_detail_view/${record.workoutId}")
                            } else {
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = exercise.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Макс. вес",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (record.maxWeight > 0.0) { "${record.maxWeight}" } else { "-" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Макс. повт.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (record.maxReps > 0) { "${record.maxReps}" } else { "-" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (savedWorkouts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    ) {
                        Text(
                            text = "Добавьте первую тренировку,\nчтобы увидеть рекорды",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
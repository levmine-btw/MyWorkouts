package com.example.myworkouts.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myworkouts.data.models.RecordData
import com.example.myworkouts.data.models.SavedWorkout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    savedWorkouts: List<SavedWorkout>,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рекорды") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
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
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Мои рекорды",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
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
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                        text = if (record.maxWeight > 0.0) "${record.maxWeight}" else "-",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.height(40.dp).width(1.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Макс. повт.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (record.maxReps > 0) "${record.maxReps}" else "-",
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
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
}
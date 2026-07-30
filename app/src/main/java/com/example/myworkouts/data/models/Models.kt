package com.example.myworkouts.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate


data class SetData(
    val reps: Int = 0,
    val weight: String = ""
)

data class SavedWorkout(
    val id : Long = System.currentTimeMillis(),
    val name: String,
    val exercises: List<String>,
    val date: Long,
    val setsData: Map<String, List<SetData>> = emptyMap()
)

data class CalendarWeek(val days: List<LocalDate?>)

data class RecordData(
    val maxWeight: Double = 0.0,
    val maxReps: Int = 0,
    val workoutId: Long = 0L
)


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Workouts : Screen("workouts", "Тренировки", Icons.Default.Favorite)
    object Calendar : Screen("calendar", "Календарь", Icons.Default.DateRange)
    object Records : Screen("records", "Рекорды", Icons.Default.Star)
}
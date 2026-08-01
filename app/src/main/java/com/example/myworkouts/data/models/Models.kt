package com.example.myworkouts.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class SetData(
    val reps: Int = 0,
    val weight: String = ""
)

@Serializable
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


sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Workouts : Screen("workouts", "Тренировки", Icons.Default.Favorite)
    object Calendar : Screen("calendar", "Календарь", Icons.Default.DateRange)
    object Profile : Screen("profile", "Профиль", Icons.Default.Person)

    object Stats : Screen("profile/stats", "Статистика")
    object Exercises : Screen("profile/exercises", "Упражнения")
    object Records : Screen("profile/records", "Рекорды")
    object Settings : Screen("profile/settings", "Настройки")
    object Personalization : Screen("profile/settings/personalization", "Персонализация")
    object Security : Screen("profile/settings/security", "Безопасность")
    object About : Screen("profile/settings/about", "О приложении")
}
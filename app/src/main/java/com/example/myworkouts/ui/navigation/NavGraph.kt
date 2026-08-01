package com.example.myworkouts.ui.navigation

import com.example.myworkouts.ui.profile.StatsScreen
import com.example.myworkouts.ui.profile.ExercisesScreen
import com.example.myworkouts.ui.profile.RecordsScreen
import com.example.myworkouts.ui.profile.SettingsScreen
import com.example.myworkouts.ui.profile.PersonalizationScreen
import com.example.myworkouts.ui.profile.SecurityScreen
import com.example.myworkouts.ui.profile.AboutScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myworkouts.data.models.SavedWorkout
import com.example.myworkouts.data.models.Screen
import com.example.myworkouts.ui.screens.CalendarScreen
import com.example.myworkouts.ui.screens.DayWorkoutScreen
import com.example.myworkouts.ui.profile.RecordsScreen
import com.example.myworkouts.ui.screens.WorkoutScreen
import com.example.myworkouts.ui.screens.WorkoutsApp
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myworkouts.ui.profile.AboutScreen
import com.example.myworkouts.ui.profile.ExercisesScreen
import com.example.myworkouts.ui.profile.PersonalizationScreen
import com.example.myworkouts.ui.profile.ProfileScreen
import com.example.myworkouts.ui.profile.SecurityScreen
import com.example.myworkouts.ui.profile.SettingsScreen
import com.example.myworkouts.ui.profile.StatsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun MyWorkoutsNavGraph(
    navController: NavHostController,
    savedWorkouts: List<SavedWorkout>,
    onWorkoutSaved: (SavedWorkout) -> Unit,
    onWorkoutDeleted: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var previousWorkoutsSize by remember { mutableStateOf(savedWorkouts.size) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(savedWorkouts.size) {
        if (savedWorkouts.size < previousWorkoutsSize && previousWorkoutsSize > 0) {
            kotlinx.coroutines.withTimeoutOrNull(1500) {
                snackbarHostState.showSnackbar(
                    message = "Тренировка удалена"
                )
            }
        }
        previousWorkoutsSize = savedWorkouts.size
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.widthIn(max = 250.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.inverseSurface
                    ) {
                        Text(
                            text = data.visuals.message,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val screens = listOf(Screen.Workouts, Screen.Calendar, Screen.Profile)

                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route ||
                                (screen == Screen.Calendar && currentRoute?.startsWith("day_workouts/") == true),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                    launchSingleTop = true

                                    when (screen) {
                                        Screen.Workouts -> popUpTo(Screen.Workouts.route) { inclusive = true }
                                        Screen.Calendar -> popUpTo(Screen.Calendar.route) { inclusive = true }
                                        Screen.Profile -> popUpTo(Screen.Profile.route) { inclusive = true }
                                        else -> {

                                        }
                                    }
                                }
                            }
                        },
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Workouts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Workouts.route) {
                WorkoutsApp(
                    savedWorkouts = savedWorkouts,
                    navController = navController
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    savedWorkouts = savedWorkouts,
                    onDayClick = { date ->
                        navController.navigate("day_workouts/$date")
                    }
                )
            }

            composable("profile") {
                ProfileScreen(navController)
            }

            composable(Screen.Stats.route) {
                StatsScreen(navController = navController)
            }
            composable(Screen.Exercises.route) {
                ExercisesScreen(navController = navController)
            }
            composable(Screen.Records.route) {
                RecordsScreen(savedWorkouts = savedWorkouts, navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController)
            }
            composable(Screen.Personalization.route) {
                PersonalizationScreen(navController = navController)
            }
            composable(Screen.Security.route) {
                SecurityScreen(navController = navController)
            }
            composable(Screen.About.route) {
                AboutScreen(navController = navController)
            }

            composable(
                route = "day_workouts/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                val date = LocalDate.parse(dateStr)
                DayWorkoutScreen(
                    navController = navController,
                    date = date,
                    savedWorkouts = savedWorkouts,
                    onBackClick = { navController.popBackStack() },
                    onAddWorkout = {
                        navController.navigate("workout_detail/$date")
                    }
                )
            }

            composable(
                route = "workout_detail/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: ""
                val date = LocalDate.parse(dateStr)
                WorkoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onWorkoutSaved = { name, setsData ->
                        val newWorkout = SavedWorkout(
                            name = name,
                            exercises = setsData.keys.toList(),
                            date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                            setsData = setsData
                        )
                        onWorkoutSaved(newWorkout)
                        navController.popBackStack()
                    },
                    initialData = null,
                    initialSetsData = null,
                    isEditing = true,
                    onDeleteWorkout = null
                )
            }

            composable(
                route = "workout_detail_view/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val workout = savedWorkouts.find { it.id == id }
                var showDeleteDialog by remember { mutableStateOf(false) }

                if (workout != null) {
                    WorkoutScreen(
                        onBackClick = { navController.popBackStack() },
                        onWorkoutSaved = { name, setsData ->
                            val updatedWorkout = SavedWorkout(
                                id = id,
                                name = name,
                                exercises = setsData.keys.toList(),
                                date = workout.date,
                                setsData = setsData
                            )
                            onWorkoutSaved(updatedWorkout)

                            coroutineScope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        },
                        initialData = workout.name to workout.exercises,
                        initialSetsData = workout.setsData,
                        isEditing = true,
                        onDeleteWorkout = { showDeleteDialog = true },
                        coroutineScope = coroutineScope
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Подтверждение") },
                            text = { Text("Вы действительно хотите удалить \"${workout.name}\"?") },
                            confirmButton = {
                                Button(onClick = {
                                    onWorkoutDeleted(id)
                                    showDeleteDialog = false
                                    navController.popBackStack()
                                }) { Text("Удалить") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Отмена")
                                }
                            }
                        )
                    }
                } else {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                }
            }
        }
    }
}
package com.example.myworkouts.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.myworkouts.ui.screens.RecordsScreen
import com.example.myworkouts.ui.screens.WorkoutScreen
import com.example.myworkouts.ui.screens.WorkoutsApp
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun MyWorkoutsNavGraph(
    navController: NavHostController,
    savedWorkouts: List<SavedWorkout>,
    onWorkoutSaved: (SavedWorkout) -> Unit,
    onWorkoutDeleted: (Long) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomAppBar {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                val screens = listOf(Screen.Workouts, Screen.Calendar, Screen.Records)

                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route ||
                                (screen == Screen.Calendar && currentRoute?.startsWith("day_workouts/") == true),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
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

            composable(Screen.Records.route) {
                RecordsScreen(
                    savedWorkouts = savedWorkouts,
                    navController = navController
                )
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
                            navController.popBackStack()
                        },
                        initialData = workout.name to workout.exercises,
                        initialSetsData = workout.setsData,
                        isEditing = true,
                        onDeleteWorkout = {
                            onWorkoutDeleted(id)
                            navController.popBackStack()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
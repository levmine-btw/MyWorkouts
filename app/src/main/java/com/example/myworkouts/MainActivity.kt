package com.example.myworkouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.myworkouts.data.models.SavedWorkout
import com.example.myworkouts.ui.navigation.MyWorkoutsNavGraph
import com.example.myworkouts.ui.theme.MyWorkoutsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWorkoutsTheme {
                val navController = rememberNavController()

                var savedWorkouts by remember {
                    mutableStateOf<List<SavedWorkout>>(emptyList())
                }

                val snackbarHostState = remember { SnackbarHostState() }

                MyWorkoutsNavGraph(
                    navController = navController,
                    savedWorkouts = savedWorkouts,
                    onWorkoutSaved = { workout ->
                        val index = savedWorkouts.indexOfFirst { it.id == workout.id }
                        if (index != -1) {
                            savedWorkouts = savedWorkouts.toMutableList().apply { set(index, workout) }
                        } else {
                            savedWorkouts = savedWorkouts + workout
                        }
                    },
                    onWorkoutDeleted = { id ->
                        savedWorkouts = savedWorkouts.filterNot { it.id == id }
                    },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
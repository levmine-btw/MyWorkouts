package com.example.myworkouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.myworkouts.data.WorkoutsDataStore
import com.example.myworkouts.ui.navigation.MyWorkoutsNavGraph
import com.example.myworkouts.ui.theme.MyWorkoutsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWorkoutsTheme {
                val navController = rememberNavController()

                val context = LocalContext.current
                val savedWorkouts  by WorkoutsDataStore.getWorkouts(context)
                    .collectAsState(initial = emptyList())

                val coroutineScope = rememberCoroutineScope()

                val snackbarHostState = remember { SnackbarHostState() }

                MyWorkoutsNavGraph(
                    navController = navController,
                    savedWorkouts = savedWorkouts,
                    onWorkoutSaved = { workout ->
                        coroutineScope.launch {
                            val updatedList = if (savedWorkouts.any { it.id == workout.id }) {
                                savedWorkouts.map { if (it.id == workout.id) { workout } else { it } }
                            } else {
                                savedWorkouts + workout
                            }
                            WorkoutsDataStore.saveWorkouts(context, updatedList)
                        }
                    },
                    onWorkoutDeleted = { id ->
                        coroutineScope.launch {
                            val updatedList = savedWorkouts.filterNot { it.id == id }
                            WorkoutsDataStore.saveWorkouts(context, updatedList)
                        }
                    },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}
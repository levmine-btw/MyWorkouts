package com.example.myworkouts

import android.health.connect.datatypes.ExerciseLap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myworkouts.ui.theme.MyWorkoutsTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.input.KeyboardType
import kotlin.collections.emptyList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWorkoutsTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            WorkoutsApp(
                                onNavigateToWorkout = {
                                    navController.navigate("workout")
                                }
                            )
                        }

                        composable("workout") {
                            WorkoutScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun WorkoutsApp(onNavigateToWorkout: () -> Unit) {
    val workouts = listOf<String>()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Мои тренировки",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (workouts.isEmpty()) {
                Text(
                    text = "Пока нет тренировок",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Список тренировок")
            }
        }
        Button(
            onClick = onNavigateToWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Добавить тренировку")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit
) {
    var workoutData by remember { mutableStateOf<Map<String, List<Int>>>(emptyMap()) }
    var selectedExercise by remember {mutableStateOf<String?>(null)}
    val currentSets = selectedExercise?.let { workoutData[it] ?: emptyList() } ?: emptyList()
    var showExerciseDialog by remember {mutableStateOf(false)}

    var expandedExercise by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val allExercises = workoutData.keys.toList()

    fun toggleExpand(exercise: String) {
        expandedExercise = if (expandedExercise == exercise) { null } else { exercise }
    }
    fun addSetForExercise(exercise: String) {
       val currentList = workoutData[exercise] ?: emptyList()
        workoutData = workoutData + (exercise to currentList + 0)
    }

    fun updateSetForExercise(exercise: String, index: Int, value: Int) {
        val currentList = workoutData[exercise] ?: emptyList()
        val newList = currentList.toMutableList().apply {
            if (index < size) { this[index] = value } else { add(value) }
        }
        workoutData = workoutData + (exercise to newList)
    }

    fun removeSetForExercise(exercise: String, index: Int) {
        val currentList = workoutData[exercise] ?: emptyList()
        if (index < currentList.size) {
            val newList = currentList.toMutableList().apply { removeAt(index) }
            workoutData = workoutData + (exercise to newList)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая тренировка") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) {
        innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            allExercises.forEach { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(vertical = 4.dp),
                    onClick = { toggleExpand(exercise) }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = exercise,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expandedExercise == exercise)
                                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        if (expandedExercise == exercise) {
                            Divider()

                            val sets = workoutData[exercise] ?: emptyList()
                            sets.forEachIndexed { index, reps ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = if (reps == 0) "" else reps.toString(),
                                        onValueChange = { text ->
                                            updateSetForExercise(exercise, index, text.toIntOrNull() ?: 0)
                                        },
                                        label = { Text("Подход ${index + 1}") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    Text("повт.", modifier = Modifier.padding(horizontal = 8.dp))

                                    IconButton(onClick = { removeSetForExercise(exercise, index) }) {
                                        Icon(Icons.Default.Close, "Удалить подход")
                                    }
                                }
                            }

                            TextButton(
                                onClick = { addSetForExercise(exercise) },
                                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Новый подход")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showExerciseDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить упражнение")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    val exercises = listOf(
        "Подтягивания",
        "Армрестлинг: крюк",
        "Армрестлинг - верх",
        "Армрестлинг - боковое давление"
    )

    if (showExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showExerciseDialog = false },
            title = { Text("Выберите упражнение") },
            text = {
                Column {
                    exercises.forEach { exercise ->
                        TextButton(
                            onClick = {
                                val exerciseName = exercise

                                if(!workoutData.containsKey(exerciseName)) {
                                    workoutData = workoutData + (exerciseName to emptyList<Int>())
                                    expandedExercise = exerciseName
                                } else {
                                    expandedExercise = exerciseName
                                }

                                showExerciseDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(exercise)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.savedstate.serialization.saved
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
                    var savedWorkouts by remember { mutableStateOf<List<SavedWorkout>>(emptyList()) }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            WorkoutsApp(
                                onNavigateToWorkout = {
                                    navController.navigate("workout")
                                },
                                savedWorkouts = savedWorkouts
                            )
                        }

                        composable("workout") {
                            WorkoutScreen(
                                onBackClick = { navController.popBackStack() },
                                onWorkoutSaved = { name, data ->
                                    val newWorkout = SavedWorkout(
                                        name = name,
                                        exercises = data.keys.toList()
                                    )
                                    savedWorkouts = savedWorkouts + newWorkout
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

data class SetData(
    val reps: Int = 0,
    val weight: String = ""
)

data class SavedWorkout(
    val name: String,
    val exercises: List<String>,
    val date: Long = System.currentTimeMillis()
)

@Composable
fun WorkoutsApp(
    onNavigateToWorkout: () -> Unit,
    savedWorkouts: List<SavedWorkout>
) {
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
            if (savedWorkouts.isEmpty()) {
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
                    items(savedWorkouts.size) { index ->
                        val workout = savedWorkouts[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {  }
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
    onBackClick: () -> Unit,
    onWorkoutSaved: (name: String, data: Map<String, List<SetData>>) -> Unit
) {
    var workoutData by remember { mutableStateOf<Map<String, List<SetData>>>(emptyMap()) }
    var showExerciseDialog by remember {mutableStateOf(false)}
    var expandedExercise by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val allExercises = workoutData.keys.toList()

    var showSaveDialog by remember { mutableStateOf(false) }
    var workoutName by remember { mutableStateOf("") }

    fun toggleExpand(exercise: String) {
        expandedExercise = if (expandedExercise == exercise) { null } else { exercise }
    }
    fun addSetForExercise(exercise: String) {
       val currentList = workoutData[exercise] ?: emptyList()
        workoutData = workoutData + (exercise to currentList + SetData(weight = ""))
    }

    fun updateSetReps(exercise: String, index: Int, value: Int) {
        val currentList = workoutData[exercise] ?: emptyList()
        val newData = currentList[index].copy(reps = value)
        val newList = currentList.toMutableList().apply { this[index] = newData }
        workoutData = workoutData + (exercise to newList)
    }

    fun updateSetWeight(exercise: String, index: Int, value: String) {
        val currentList = workoutData[exercise] ?: emptyList()
        val newData = currentList[index].copy(weight = value)
        val newList = currentList.toMutableList().apply { this[index] = newData }
        workoutData = workoutData + (exercise to newList)
    }

    fun removeSetForExercise(exercise: String, index: Int) {
        val currentList = workoutData[exercise] ?: emptyList()
        if (index < currentList.size) {
            val newList = currentList.toMutableList().apply { removeAt(index) }
            workoutData = workoutData + (exercise to newList)
        }
    }

    fun saveWorkout() {
        if (workoutName.isNotBlank()) {
            onWorkoutSaved(workoutName, workoutData)
            showSaveDialog = false
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
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toggleExpand(exercise) }
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
                            sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.width(24.dp)
                                    )

                                    OutlinedTextField(
                                        value = if (set.reps == 0) "" else set.reps.toString(),
                                        onValueChange = { text ->
                                            val newReps = text.toIntOrNull() ?: 0
                                            updateSetReps(exercise, index, text.toIntOrNull() ?: 0)
                                        },
                                        modifier = Modifier.weight(0.3f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )

                                    Text("повт.", modifier = Modifier.padding(horizontal = 4.dp))

                                    OutlinedTextField(
                                        value = set.weight,
                                        onValueChange = { text ->
                                            if (text.all { it.isDigit() || it == '.' }) {
                                                if (text.count { it == '.'} <= 1) {
                                                    updateSetWeight(exercise, index, text)
                                                }
                                            }
                                        },
                                        label = { Text("Вес") },
                                        modifier = Modifier.weight(0.3f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )

                                    Text("кг.", modifier = Modifier.padding(start = 4.dp, end = 8.dp))

                                    IconButton(
                                        onClick = { removeSetForExercise(exercise, index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "Удалить", modifier = Modifier.size(16.dp))
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

            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Сохранить тренировку")
            }

            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Название тренировки") },
                    text = {
                        OutlinedTextField(
                            value = workoutName,
                            onValueChange = {
                                if (it.length <= 256) { workoutName = it }
                            },
                            label = { Text("Что было особенного?") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = { saveWorkout() }) {
                            Text("Сохранить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    val exercises = listOf(
        "подтягивания",
        "крюк",
        "верх",
        "боковое давление"
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
                                    workoutData = workoutData + (exerciseName to emptyList<SetData>())
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
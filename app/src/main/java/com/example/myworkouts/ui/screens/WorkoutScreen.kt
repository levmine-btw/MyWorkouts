package com.example.myworkouts.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myworkouts.data.models.SetData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.forEachIndexed
import kotlin.collections.plus
import kotlin.collections.toMutableList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBackClick: () -> Unit,
    onWorkoutSaved: ((String, Map<String, List<SetData>>) -> Unit)? = null,
    initialData: Pair<String, List<String>>? = null,
    initialSetsData: Map<String, List<SetData>>? = null,
    isEditing: Boolean = true,
    onDeleteWorkout: (() -> Unit)? = null
) {
    var workoutData by remember {
        mutableStateOf<Map<String, List<SetData>>>(
            initialSetsData ?: (initialData?.second?.associateWith { emptyList<SetData>() } ?: emptyMap())
        )
    }

    var showExerciseDialog by remember { mutableStateOf(false) }
    var expandedExercise by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val allExercises = workoutData.keys.toList()

    var showSaveDialog by remember { mutableStateOf(false) }
    var workoutName by remember { mutableStateOf(initialData?.first ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun toggleExpand(exercise: String) {
        expandedExercise = if (expandedExercise == exercise) null else exercise
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
            onWorkoutSaved?.invoke(workoutName, workoutData)
            showSaveDialog = false

            if (initialData != null) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Изменения применены",
                        duration = SnackbarDuration.Long
                    )
                    delay(3000)
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing && initialData != null) { initialData.first } else { "Новая тренировка" },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditing && initialData != null) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FloatingActionButton(
                        onClick = onDeleteWorkout ?: {},
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 32.dp)
                    ) {
                        Icon(Icons.Default.Delete, "Удалить")
                    }

                    FloatingActionButton(
                        onClick = { saveWorkout() },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Default.Check, "Применить изменения")
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { innerPadding ->
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
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { toggleExpand(exercise) }
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
                            HorizontalDivider()
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

                                    if (!isEditing) {
                                        Text(
                                            text = if (set.reps == 0) "" else set.reps.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(0.3f)
                                        )
                                        Text("повт.", modifier = Modifier.padding(horizontal = 4.dp))
                                        Text(
                                            text = set.weight,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(0.3f)
                                        )
                                        Text("кг.", modifier = Modifier.padding(start = 4.dp, end = 8.dp))
                                    }
                                    else {
                                        OutlinedTextField(
                                            value = if (set.reps == 0) "" else set.reps.toString(),
                                            onValueChange = { text ->
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
                                                    if (text.count { it == '.' } <= 1) {
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
                            }
                            if (isEditing) {
                                TextButton(
                                    onClick = { addSetForExercise(exercise) },
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(start = 16.dp, bottom = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Новый подход")
                                }
                            }
                        }
                    }
                }
            }
            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showExerciseDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить упражнение")
                }

                if (initialData == null) {
                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Сохранить тренировку")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    if (showSaveDialog && initialData == null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Название тренировки") },
            text = {
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { if (it.length <= 256) workoutName = it },
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

    val exercises = listOf("подтягивания", "крюк", "верх", "боковое давление")
    if (showExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showExerciseDialog = false },
            title = { Text("Выберите упражнение") },
            text = {
                Column {
                    exercises.forEach { exercise ->
                        TextButton(
                            onClick = {
                                if (!workoutData.containsKey(exercise)) {
                                    workoutData = workoutData + (exercise to emptyList<SetData>())
                                }
                                expandedExercise = exercise
                                showExerciseDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(exercise) }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
package com.example.myworkouts

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.annotation.RequiresApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myworkouts.ui.theme.MyWorkoutsTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.*
import androidx.savedstate.serialization.saved
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWorkoutsTheme() {
                val navController = rememberNavController()
                var savedWorkouts by remember { mutableStateOf<List<SavedWorkout>>(emptyList()) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                            bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Workouts.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
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
                                onNavigateToWorkout = {
                                    navController.navigate("workout_detail")
                                },
                                savedWorkouts = savedWorkouts
                            )
                        }

                        composable(Screen.Calendar.route) {
                            CalendarScreen(
                                savedWorkouts = savedWorkouts,
                                onDayClick = { date ->
                                    println("Выбрана дата $date")
                                }
                            )
                        }

                        composable(Screen.Records.route) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Рекорды")
                            }
                        }

                        composable("workout_detail") {
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Workouts : Screen("workouts", "Тренировки", Icons.Default.Favorite)
    object Calendar : Screen("calendar", "Календарь", Icons.Default.DateRange)
    object Records : Screen("records", "Рекорды", Icons.Default.Star)
}

val bottomNavItems = listOf(Screen.Workouts, Screen.Calendar, Screen.Records)

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
                            imageVector = Icons.Default.ArrowBack,
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

fun generateCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()

    val dayOfWeek = firstDayOfMonth.dayOfWeek.value
    val calendarDays = mutableListOf<LocalDate?>()

    repeat(dayOfWeek - 1) {
        calendarDays.add(null)
    }

    for (day in 1..daysInMonth) {
        calendarDays.add(yearMonth.atDay(day))
    }

    return calendarDays
}

@Composable
fun CalendarScreen(
    savedWorkouts: List<SavedWorkout>,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }
    val calendarDays = generateCalendarDays(currentMonth)
    val workoutDates = remember(savedWorkouts) {
        savedWorkouts.map {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }

    Column( modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, "Предыдущий месяц")
            }

            val monthName = currentMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))

            Text(
                text = monthName,
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, "Следующий месяц")
                }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays.size) { index ->
                val date = calendarDays[index]

                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val isToday = date == today
                    val hasWorkout = workoutDates.contains(date)

                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onDayClick(date) },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                hasWorkout -> Color.Green.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (hasWorkout) { Color.Green } else { MaterialTheme.colorScheme.onSurface }
                            )
                        }
                    }
                }
            }
        }
    }
}
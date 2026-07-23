package com.example.myworkouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myworkouts.ui.theme.MyWorkoutsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.draw.clip


// Импорты


data class SetData(val reps: Int = 0, val weight: String = "")

data class SavedWorkout(
    val name: String,
    val exercises: List<String>,
    val date: Long,
    val setsData: Map<String, List<SetData>> = emptyMap()
)

data class CalendarWeek(val days: List<LocalDate?>)

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Workouts : Screen("workouts", "Тренировки", Icons.Default.Favorite)
    object Calendar : Screen("calendar", "Календарь", Icons.Default.DateRange)
    object Records : Screen("records", "Рекорды", Icons.Default.Star)
}

val bottomNavItems = listOf(Screen.Workouts, Screen.Calendar, Screen.Records)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWorkoutsTheme {
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
                            WorkoutsApp(navController = navController, savedWorkouts = savedWorkouts)
                        }

                        composable(Screen.Calendar.route) {
                            CalendarScreen(savedWorkouts = savedWorkouts) { date ->
                                navController.navigate("day_workouts/${date.toString()}")
                            }
                        }

                        composable(Screen.Records.route) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Рекорды")
                            }
                        }

                        composable("workout_detail/{date}") { backStackEntry ->
                            val dateString = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                            val workoutDate = LocalDate.parse(dateString)
                            WorkoutScreen(
                                onBackClick = { navController.popBackStack() },
                                onWorkoutSaved = { name, data ->
                                    val dateMillis = workoutDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    savedWorkouts += SavedWorkout(
                                        name = name,
                                        exercises = data.keys.toList(),
                                        date = dateMillis,
                                        setsData = data
                                    )
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("workout_detail_view/{date}/{name}") { backStackEntry ->
                            val dateString = backStackEntry.arguments?.getString("date") ?: ""
                            val name = backStackEntry.arguments?.getString("name") ?: ""
                            val coroutineScope = rememberCoroutineScope()

                            val dayStart = LocalDate.parse(dateString).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            val dayEnd = dayStart + 24 * 60 * 60 * 1000

                            val workout = savedWorkouts.firstOrNull {
                                it.date >= dayStart && it.date < dayEnd && it.name == name
                            }

                            if (workout != null) {
                                WorkoutScreen(
                                    onBackClick = { navController.popBackStack() },
                                    initialData = workout.name to workout.exercises,
                                    initialSetsData = workout.setsData,
                                    isEditing = true,
                                    onWorkoutSaved = { updatedName, updatedData ->
                                        savedWorkouts = savedWorkouts.map { w ->
                                            if (w.date == workout.date && w.name == workout.name) {
                                                SavedWorkout(updatedName, updatedData.keys.toList(), w.date, updatedData)
                                            } else w
                                        }
                                        coroutineScope.launch {
                                            delay(300)
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        }

                        composable("day_workouts/{date}") { backStackEntry ->
                            val dateString = backStackEntry.arguments?.getString("date") ?: ""
                            val selectedDate = LocalDate.parse(dateString)
                            DayWorkoutsScreen(
                                navController = navController,
                                date = selectedDate,
                                savedWorkouts = savedWorkouts,
                                onBackClick = { navController.popBackStack() },
                                onAddWorkout = { navController.navigate("workout_detail/${selectedDate.toString()}") }
                            )
                        }
                    }
                }
            }
        }
    }
}


// Главный экран
@Composable
fun WorkoutsApp(
    navController: NavController,
    savedWorkouts: List<SavedWorkout>
) {
    val today = LocalDate.now()

    val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val dayEnd = dayStart + 24 * 60 * 60 * 1000

    val todayWorkouts = savedWorkouts.filter {
        it.date >= dayStart && it.date < dayEnd
    }

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
            if (todayWorkouts.isEmpty()) {
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
                    items(todayWorkouts.size) { index ->
                        val workout = todayWorkouts[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate("workout_detail_view/${today}/${workout.name}") }
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
            onClick = {
                navController.navigate("workout_detail/${today}")
            },
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
    onWorkoutSaved: ((String, Map<String, List<SetData>>) -> Unit)? = null,
    initialData: Pair<String, List<String>>? = null,
    initialSetsData: Map<String, List<SetData>>? = null,
    isEditing: Boolean = true
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
                        message = "Изменения прмиенены",
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
                title = { Text(if (isEditing) "Тренировка" else "Просмотр") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditing && initialData != null && onWorkoutSaved != null) {
                FloatingActionButton(onClick = { saveWorkout() }) {
                    Icon(Icons.Default.Check, "Применить изменения")
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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

fun generateCalendarWeeks(yearMonth: YearMonth): List<CalendarWeek> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value

    val weeks = mutableListOf<CalendarWeek>()
    var currentWeekDays = mutableListOf<LocalDate?>()

    repeat(dayOfWeek - 1) { currentWeekDays.add(null) }

    for (day in 1..daysInMonth) {
        currentWeekDays.add(yearMonth.atDay(day))
        if (currentWeekDays.size == 7 || day == daysInMonth) {
            while (currentWeekDays.size < 7) currentWeekDays.add(null)
            weeks.add(CalendarWeek(days = currentWeekDays.toList()))
            currentWeekDays = mutableListOf()
        }
    }
    return weeks
}

@Composable
fun CalendarScreen(
    savedWorkouts: List<SavedWorkout>,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }

    val calendarWeeks = generateCalendarWeeks(currentMonth)
    val workoutDates = remember(savedWorkouts) {
        savedWorkouts.map {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }

    var isSwipeLocked by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun changeMonth(direction: Int) {
        if (!isSwipeLocked) {
            isSwipeLocked = true
            currentMonth = if (direction > 0) currentMonth.plusMonths(1) else currentMonth.minusMonths(1)

            coroutineScope.launch {
                delay(400)
                isSwipeLocked = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { changeMonth(-1) }) {
                Icon(Icons.Default.ArrowBack, "Предыдущий месяц")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { changeMonth(1) }) {
                Icon(Icons.Default.ArrowForward, "Следующий месяц")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (totalDrag.absoluteValue > 200 && !isSwipeLocked) {
                                changeMonth(if (totalDrag > 0) -1 else 1)
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = { totalDrag = 0f }
                    ) { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Box(modifier = Modifier.width(8.dp))
                    Spacer(Modifier.width(4.dp))
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(calendarWeeks.size) { index ->
                        val week = calendarWeeks[index]
                        val isHighRep = index % 2 == 0
                        val stripColor = if (isHighRep) Color.Cyan else Color.Red

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(40.dp)
                                    .background(stripColor, RoundedCornerShape(3.dp))
                            )

                            Spacer(Modifier.width(4.dp))

                            week.days.forEach { date ->
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        val isToday = date == today
                                        val hasWorkout = workoutDates.contains(date)

                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onDayClick(date) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = when {
                                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                                    hasWorkout -> Color.Green.copy(alpha = 0.2f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = date.dayOfMonth.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (hasWorkout) Color.Green else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(6.dp).height(16.dp).background(Color.Cyan, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(4.dp))
                Text("Многоповторная", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(6.dp).height(16.dp).background(Color.Red, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(4.dp))
                Text("Силовая", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayWorkoutsScreen(
    navController: NavController,
    date: LocalDate,
    savedWorkouts: List<SavedWorkout>,
    onBackClick: () -> Unit,
    onAddWorkout: () -> Unit
) {
    val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val dayEnd = dayStart + 24 * 60 * 60 * 1000

    val dayWorkouts = savedWorkouts.filter {
        it.date >= dayStart && it.date < dayEnd
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))),
                        style = MaterialTheme.typography.titleMedium
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
            FloatingActionButton(onClick = onAddWorkout) {
                Icon(Icons.Default.Add, "Добавить тренировку")
            }
        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (dayWorkouts.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Тренировок нет",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dayWorkouts.size) { index ->
                        val workout = dayWorkouts[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate("workout_detail_view/${date}/${workout.name}") }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = workout.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                if (workout.exercises.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
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
    }
}
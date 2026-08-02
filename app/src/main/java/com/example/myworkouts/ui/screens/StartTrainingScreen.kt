package com.example.myworkouts.ui.screens

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.remote.creation.dsl.first
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myworkouts.R
import com.example.myworkouts.data.WorkoutsDataStore
import com.example.myworkouts.data.models.SavedWorkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTrainingScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isTraining by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf("idle") }

    var useTimer by remember { mutableStateOf(false) }
    var timerMinutes by remember { mutableStateOf("1") }
    var timerSeconds by remember { mutableStateOf("0") }
    var remainingTimeMs by remember { mutableLongStateOf(0L) }

    var soundPool by remember { mutableStateOf<SoundPool?>(null) }
    var readySoundId by remember { mutableIntStateOf(0) }
    var goSoundId by remember { mutableIntStateOf(0) }
    var beepSoundId by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                readySoundId = load(context, R.raw.ready, 1)
                goSoundId = load(context, R.raw.go, 1)
                beepSoundId = load(context, R.raw.beep, 1)
            }
    }

    DisposableEffect(Unit) {
        onDispose { soundPool?.release(); soundPool = null }
    }

    var timerRestartKey by remember { mutableIntStateOf(0) }
    var isTimerActive by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }

    val smoothElapsed by remember { derivedStateOf { elapsedMs } }

    LaunchedEffect(isTimerActive) {
        if (!isTimerActive) return@LaunchedEffect

        val startTime = System.currentTimeMillis()

        withContext(Dispatchers.Default) {
            while (isActive && isTimerActive) {
                val currentElapsed = System.currentTimeMillis() - startTime

                if (useTimer && !isTraining) { break }

                elapsedMs = currentElapsed

                delay(16)
            }
        }
    }

    LaunchedEffect(useTimer) {
        if (isTraining) {  }
    }

    LaunchedEffect(isTraining) {
        if (!isTraining) {
            currentPhase = "idle"
            return@LaunchedEffect
        }

        var attempts = 0
        while (soundPool == null && attempts < 10) { delay(100); attempts++ }

        val totalTimerMs = if (useTimer) {
            (timerMinutes.toLongOrNull() ?: 0) * 60000 + (timerSeconds.toLongOrNull() ?: 0) * 1000
        } else 0L

        val trainingStartTime = System.currentTimeMillis()
        remainingTimeMs = totalTimerMs

        while (coroutineContext.isActive && isTraining) {
            if (useTimer && totalTimerMs > 0) {
                val passed = System.currentTimeMillis() - trainingStartTime
                remainingTimeMs = (totalTimerMs - passed).coerceAtLeast(0)

                if (remainingTimeMs <= 0) {
                    soundPool?.play(beepSoundId, 1f, 1f, 0, 0, 1f)
                    delay(1000)
                    isTraining = false
                    break
                }
            }

            currentPhase = "idle"
            delay(1000)
            if (!isTraining) break

            currentPhase = "ready"
            soundPool?.play(readySoundId, 1f, 1f, 1, 0, 1f)

            val randomDelay = Random.nextLong(5, 501)
            delay(randomDelay)
            if (!isTraining) break

            currentPhase = "go"
            soundPool?.play(goSoundId, 1f, 1f, 1, 0, 1f)

            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Отработка старта") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(when (currentPhase) {
                    "ready" -> Color(0xFFFFEB3B).copy(alpha = 0.2f)
                    "go" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surface
                }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = formatTime(smoothElapsed),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = when (currentPhase) {
                    "ready" -> "READY!"
                    "go" -> "GO!"
                    else -> if (isTraining) "Выполнено" else "Нажми СТАРТ"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when (currentPhase) {
                    "ready" -> Color(0xFFF57F17)
                    "go" -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (!isTraining) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = useTimer, onCheckedChange = { useTimer = it })
                        Text("Фиксированное время", modifier = Modifier.padding(start = 8.dp))
                    }

                    if (useTimer) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = timerMinutes,
                                onValueChange = { if (it.all(Char::isDigit)) timerMinutes = it },
                                label = { Text("Мин") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp), singleLine = true
                            )
                            Text(":")
                            OutlinedTextField(
                                value = timerSeconds,
                                onValueChange = { if (it.all(Char::isDigit) && it.length <= 2) timerSeconds = it },
                                label = { Text("Сек") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp), singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isTraining) {
                    Button(
                        onClick = {
                            if (useTimer) {
                                val mins = timerMinutes.toLongOrNull() ?: 0
                                val secs = timerSeconds.toLongOrNull() ?: 0
                                val totalMs = mins * 60000 + secs * 1000

                                if (totalMs <= 0) return@Button
                            }

                            isTraining = true
                            isTimerActive = true
                            timerRestartKey++

                            remainingTimeMs = if (useTimer) {
                                (timerMinutes.toLongOrNull() ?: 0) * 60000 + (timerSeconds.toLongOrNull() ?: 0) * 1000
                            } else 0L
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("СТАРТ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            isTraining = false
                            isTimerActive = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("СТОП", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (elapsedMs == 0L) { return@Button }

                        coroutineScope.launch {
                            val allWorkouts = WorkoutsDataStore.getWorkouts(context).first()
                            val existingStarts = allWorkouts.filter { it.name.startsWith("[Старт] Отработка") }
                            val nextNumber = if (existingStarts.isEmpty()) 1
                            else existingStarts.maxOf { w ->
                                w.name.removePrefix("[Старт] Отработка ").toIntOrNull() ?: 0
                            } + 1

                            val startWorkout = SavedWorkout(
                                name = "[Старт] Отработка $nextNumber",
                                exercises = listOf("Старт"),
                                date = System.currentTimeMillis(),
                                isStartDrill = false,
                                startDurationMs = elapsedMs,
                                setsData = emptyMap()
                            )
                            WorkoutsDataStore.saveWorkouts(context, allWorkouts + startWorkout)
                            navController.popBackStack()
                        }
                    },
                    enabled = elapsedMs > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (elapsedMs > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (elapsedMs > 0) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = "Сохранить отработку",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val minutes = ms / 60000
    val seconds = (ms % 60000) / 1000
    val millis = (ms % 1000) / 10
    return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, millis)
}
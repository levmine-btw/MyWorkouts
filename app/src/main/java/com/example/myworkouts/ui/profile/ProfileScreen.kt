package com.example.myworkouts.ui.profile

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myworkouts.data.models.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val mainSections = listOf(
        Screen.Stats to "Статистика",
        Screen.Exercises to "Упражнения",
        Screen.Records to "Рекорды",
        Screen.Settings to "Настройки"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(modifier = Modifier.size(90.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "ЛК", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(text = "Лев Киприянов", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mainSections.size) { index ->
                val (screen, title) = mainSections[index]
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(screen.route) },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
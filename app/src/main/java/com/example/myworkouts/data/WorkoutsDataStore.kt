package com.example.myworkouts.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myworkouts.data.models.SavedWorkout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workouts_prefs")

object WorkoutsDataStore {
    private val WORKOUTS_KEY = stringPreferencesKey("saved_workouts_json")

    fun getWorkouts(context: Context) : Flow<List<SavedWorkout>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[WORKOUTS_KEY] ?: "[]"
            try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveWorkouts(context: Context, workouts: List<SavedWorkout>) {
        context.dataStore.edit { preferences ->
            preferences[WORKOUTS_KEY] = Json.encodeToString(workouts)
        }
    }
}
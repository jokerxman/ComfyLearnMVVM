package com.hompimpa.comfylearn.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.mathGameDataStore: DataStore<Preferences> by preferencesDataStore(name = "math_game_progress")

class GameManager(private val context: Context) {

    companion object {
        val PROBLEMS_SOLVED_KEY = intPreferencesKey("problems_solved_count")
    }

    val problemsSolvedFlow: Flow<Int> = context.mathGameDataStore.data
        .map { preferences ->
            preferences[PROBLEMS_SOLVED_KEY] ?: 0
        }

    suspend fun incrementProblemsSolved() {
        context.mathGameDataStore.edit { settings ->
            val currentCount = settings[PROBLEMS_SOLVED_KEY] ?: 0
            settings[PROBLEMS_SOLVED_KEY] = currentCount + 1
        }
    }
}

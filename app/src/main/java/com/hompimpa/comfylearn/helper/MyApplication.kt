package com.hompimpa.comfylearn.helper

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        SoundManager.initialize(this)
        val prefs = SettingPreferences.getInstance(dataStore)

        applicationScope.launch {
            val languageTag = prefs.getLanguageSetting().first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))

            val isDarkMode = prefs.getThemeSetting().first()
            val mode =
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}

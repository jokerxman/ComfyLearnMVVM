package com.hompimpa.comfylearn.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hompimpa.comfylearn.helper.Event
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SettingPreferences.getInstance(application.dataStore)

    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> = _isDarkMode

    private val _languageCode = MutableLiveData<String>()
    val languageCode: LiveData<String> = _languageCode

    private val _finishActivityWithResult = MutableLiveData<Event<Unit>>()
    val finishActivityWithResult: LiveData<Event<Unit>> = _finishActivityWithResult

    private val _navigateToLogin = MutableLiveData<Event<Unit>>()
    val navigateToLogin: LiveData<Event<Unit>> = _navigateToLogin

    init {
        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        viewModelScope.launch {
            _isDarkMode.value = prefs.getThemeSetting().first()
            _languageCode.value = prefs.getLanguageSetting().first()
        }
    }

    fun applyChanges(isDarkModeEnabled: Boolean, languageCode: String) {
        viewModelScope.launch {
            prefs.saveThemeSetting(isDarkModeEnabled)
            prefs.saveLanguageSetting(languageCode)

            val mode =
                if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)

            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

            _finishActivityWithResult.value = Event(Unit)
        }
    }
}
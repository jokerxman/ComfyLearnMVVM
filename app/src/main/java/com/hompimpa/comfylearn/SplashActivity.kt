package com.hompimpa.comfylearn

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.helper.SettingPreferences
import com.hompimpa.comfylearn.helper.dataStore
import com.hompimpa.comfylearn.ui.auth.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class SplashActivity : BaseActivity() {

    private lateinit var settingPreferences: SettingPreferences

    override fun attachBaseContext(newBase: Context) {
        settingPreferences = SettingPreferences.getInstance(newBase.dataStore)
        val languageCode = kotlinx.coroutines.runBlocking {
            settingPreferences.getLanguageSetting().first()
        }
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        val updatedContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val isDarkModeActive = settingPreferences.getThemeSetting().first()
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            setContentView(R.layout.activity_splash)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}

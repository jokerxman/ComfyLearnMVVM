package com.hompimpa.comfylearn.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.hompimpa.comfylearn.R
import java.util.Locale

object SoundManager {

    enum class Sound {
        CORRECT_ANSWER,
        INCORRECT_ANSWER,
        BUTTON_CLICK
    }

    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d("SoundManager", "Sound ID $sampleId loaded successfully.")
            } else {
                Log.e("SoundManager", "Failed to load sound ID $sampleId. Status: $status")
            }
        }

        val appContext = context.applicationContext
        soundMap[Sound.CORRECT_ANSWER.name] = soundPool.load(appContext, R.raw.correct_answer, 1)
        soundMap[Sound.BUTTON_CLICK.name] = soundPool.load(appContext, R.raw.button_click, 1)
        soundMap[Sound.INCORRECT_ANSWER.name] =
            soundPool.load(appContext, R.raw.incorrect_answer, 1)

        isInitialized = true
    }

    fun playSound(sound: Sound) {
        if (!isInitialized) return
        soundMap[sound.name]?.let { soundId ->
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playSoundByName(context: Context, itemName: String) {
        if (!isInitialized) return

        val soundKey = itemName.lowercase(Locale.ROOT)

        if (soundMap.containsKey(soundKey)) {
            val soundId = soundMap[soundKey]!!
            if (soundId > 0) {
                Log.d("SoundManager", "Playing cached sound for '$soundKey'")
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } else {
                Log.w("SoundManager", "Sound for '$soundKey' is still loading.")
            }
            return
        }

        @Suppress("DiscouragedApi")
        val resourceName = "item_" + soundKey.replace(" ", "_")

        @Suppress("DiscouragedApi")
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)

        if (resourceId != 0) {
            Log.d("SoundManager", "Loading dynamic sound for '$soundKey' (Resource: $resourceName)")
            val loadedSoundId = soundPool.load(context, resourceId, 1)
            soundMap[soundKey] = loadedSoundId

            if (loadedSoundId == 0) {
                Log.e("SoundManager", "Failed to load dynamic sound for '$soundKey' immediately.")
                soundMap.remove(soundKey)
            }
        } else {
            Log.e("SoundManager", "Dynamic sound resource not found: R.raw.$resourceName")
        }
    }

    fun release() {
        if (isInitialized) {
            soundPool.release()
            soundMap.clear()
            isInitialized = false
        }
    }
}
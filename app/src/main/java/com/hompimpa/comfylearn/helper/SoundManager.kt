package com.hompimpa.comfylearn.helper

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.hompimpa.comfylearn.R
import java.util.Locale

object SoundManager {

    enum class Sound {
        CORRECT_ANSWER,
        INCORRECT_ANSWER,
        BUTTON_CLICK
    }

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private val loadingMap = mutableMapOf<Int, String>()

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

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadingMap[sampleId]?.let { soundKey ->
                    soundMap[soundKey] = sampleId
                    loadingMap.remove(sampleId)
                }
            }
        }

        loadSound(context, Sound.CORRECT_ANSWER.name, R.raw.correct_answer)
        loadSound(context, Sound.INCORRECT_ANSWER.name, R.raw.incorrect_answer)
        loadSound(context, Sound.BUTTON_CLICK.name, R.raw.button_click)

        isInitialized = true
    }

    private fun loadSound(context: Context, key: String, resId: Int) {
        soundPool?.let {
            val soundId = it.load(context, resId, 1)
            loadingMap[soundId] = key
        }
    }

    fun playSound(sound: Sound) {
        if (!isInitialized) return

        soundMap[sound.name]?.let { soundId ->
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playSoundByName(context: Context, itemName: String) {
        if (!isInitialized) return

        val soundKey = itemName.lowercase(Locale.ROOT)

        soundMap[soundKey]?.let { soundId ->
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            return
        }

        val resourceName = "item_" + soundKey.replace(" ", "_")
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resId != 0) {
            loadSound(context, soundKey, resId)
        }
    }
}
package com.hompimpa.comfylearn.helper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.initialize(this)
    }

    override fun onDestroy() {
        SoundManager.release()
        super.onDestroy()
    }
}

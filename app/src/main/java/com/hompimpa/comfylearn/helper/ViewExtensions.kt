package com.hompimpa.comfylearn.helper

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.hompimpa.comfylearn.R

fun ScrollView.setupScrollIndicator(indicator: View) {
    this.getChildAt(0)?.doOnLayout { content ->
        indicator.visibility = if (content.height > this.height) {
            val bounce = AnimationUtils.loadAnimation(context, R.anim.bounce)
            indicator.startAnimation(bounce)
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    this.setOnScrollChangeListener { _, _, scrollY, _, _ ->
        if (scrollY > 0 && indicator.isVisible) {
            indicator.animate().alpha(0f).withEndAction {
                indicator.visibility = View.GONE
            }
        }
    }

}

fun View.setOnSoundClickListener(action: (View) -> Unit) {
    this.setOnClickListener { view ->
        SoundManager.playSound(SoundManager.Sound.BUTTON_CLICK)
        action(view)
    }
}

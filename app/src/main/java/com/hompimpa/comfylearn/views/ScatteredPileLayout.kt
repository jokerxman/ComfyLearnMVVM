package com.hompimpa.comfylearn.views

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isGone
import com.hompimpa.comfylearn.R
import kotlin.random.Random

class ScatteredPileLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val random = Random
    private val placedChildRects = mutableListOf<RectF>()
    private val childStates = mutableMapOf<View, ChildState>()
    private val tempRect = RectF()
    private val maxPlacementAttempts: Int = 100

    var maxRotationDegrees: Float = 25f
    var positionSpreadFactor: Float = 0.8f

    data class ChildState(
        var x: Float = 0f,
        var y: Float = 0f,
        var rotation: Float = 0f,
        var initialized: Boolean = false
    )

    init {
        context.withStyledAttributes(attrs, R.styleable.ScatteredPileLayout, defStyleAttr, 0) {
            maxRotationDegrees = getFloat(R.styleable.ScatteredPileLayout_maxRotationDegrees, 25f)
            positionSpreadFactor =
                getFloat(R.styleable.ScatteredPileLayout_positionSpreadFactor, 0.8f)
                    .coerceIn(0.1f, 1.0f)
        }
    }

    override fun addView(child: View, index: Int, params: LayoutParams?) {
        childStates.getOrPut(child) { ChildState() }
        super.addView(child, index, params)
    }

    override fun removeView(view: View?) {
        if (view != null) {
            childStates.remove(view)
        }
        super.removeView(view)
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams =
        MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams = MarginLayoutParams(p)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxChildWidth = 0
        var maxChildHeight = 0

        children.forEach { child ->
            if (child.isGone) return@forEach
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = child.layoutParams as MarginLayoutParams
            maxChildWidth =
                maxOf(maxChildWidth, child.measuredWidth + lp.leftMargin + lp.rightMargin)
            maxChildHeight =
                maxOf(maxChildHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
        }

        val desiredWidth = resolveSize(paddingLeft + paddingRight + maxChildWidth, widthMeasureSpec)
        val desiredHeight =
            resolveSize(paddingTop + paddingBottom + maxChildHeight, heightMeasureSpec)
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val availableWidth = r - l - paddingLeft - paddingRight
        val availableHeight = b - t - paddingTop - paddingBottom
        if (availableWidth <= 0 || availableHeight <= 0) return

        if (changed) placedChildRects.clear()

        children.forEach { child ->
            if (child.isGone) return@forEach
            val childState = childStates[child] ?: return@forEach

            if (changed || !childState.initialized) {
                calculateChildPosition(child, childState, availableWidth, availableHeight)
            }
            applyChildState(child, childState)
        }
    }

    private fun calculateChildPosition(
        child: View,
        state: ChildState,
        availableWidth: Int,
        availableHeight: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        var attempt = 0
        var positionFound = false
        val effectiveSpreadWidth = (availableWidth * positionSpreadFactor).toInt()
        val effectiveSpreadHeight = (availableHeight * positionSpreadFactor).toInt()
        val constrainedWidth = (effectiveSpreadWidth - child.measuredWidth).coerceAtLeast(1)
        val constrainedHeight = (effectiveSpreadHeight - child.measuredHeight).coerceAtLeast(1)

        while (attempt < maxPlacementAttempts && !positionFound) {
            val x = (paddingLeft + lp.leftMargin + random.nextInt(constrainedWidth)).toFloat()
            val y = (paddingTop + lp.topMargin + random.nextInt(constrainedHeight)).toFloat()
            tempRect[x, y, x + child.measuredWidth] = y + child.measuredHeight

            if (placedChildRects.none { RectF.intersects(it, tempRect) }) {
                state.x = x
                state.y = y
                placedChildRects.add(RectF(tempRect))
                positionFound = true
            }
            attempt++
        }

        if (!positionFound) {
            state.x = (paddingLeft + lp.leftMargin + (availableWidth - child.measuredWidth) / 2f)
            state.y = (paddingTop + lp.topMargin + (availableHeight - child.measuredHeight) / 2f)
        }

        state.rotation = (random.nextFloat() * maxRotationDegrees * 2) - maxRotationDegrees
        state.initialized = true
    }

    private fun applyChildState(child: View, state: ChildState) {
        val lp = child.layoutParams as MarginLayoutParams
        val layoutX = paddingLeft + lp.leftMargin
        val layoutY = paddingTop + lp.topMargin

        child.translationX = state.x - layoutX
        child.translationY = state.y - layoutY
        child.rotation = state.rotation
        child.layout(
            layoutX,
            layoutY,
            layoutX + child.measuredWidth,
            layoutY + child.measuredHeight
        )
    }

    fun getChildState(child: View): ChildState? = childStates[child]

    fun rescatterChildren() {
        childStates.values.forEach { it.initialized = false }
        placedChildRects.clear()
        requestLayout()
    }

    override fun bringChildToFront(child: View) {
        if (indexOfChild(child) < childCount - 1) {
            removeView(child)
            addView(child)
        }
    }
}
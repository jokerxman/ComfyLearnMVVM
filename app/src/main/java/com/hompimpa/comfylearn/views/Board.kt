package com.hompimpa.comfylearn.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

class Board @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Mode {
        DRAW,
        SHAPE_RECTANGLE
    }

    private var currentMode = Mode.DRAW
    private var currentColor = Color.BLACK
    private var lastPenColor = Color.BLACK
    private var brushSize = 20f
    private var canvasBackgroundColor = Color.WHITE

    private var drawingBitmap: Bitmap? = null
    private var drawingCanvas: Canvas? = null
    private var backgroundBitmap: Bitmap? = null

    private var currentDrawingPath = Path()
    private var shapePreviewPath = Path()
    private var pathStartPoint = Pair(0f, 0f)

    private val drawingPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    init {
        updateDrawingPaint()
    }

    fun setPenColor(newColor: Int) {
        currentMode = Mode.DRAW
        currentColor = newColor
        lastPenColor = newColor
        updateDrawingPaint()
    }

    fun setBrushSize(newSize: Float) {
        brushSize = newSize
        updateDrawingPaint()
    }

    fun setEraserMode(isErasing: Boolean) {
        currentMode = Mode.DRAW
        currentColor = if (isErasing) Color.TRANSPARENT else lastPenColor
        updateDrawingPaint()
    }

    fun setDrawingMode(mode: Mode) {
        currentMode = mode
        currentDrawingPath.reset()
        shapePreviewPath.reset()
        invalidate()
    }

    fun setBackgroundImage(bitmap: Bitmap) {
        backgroundBitmap = if (width > 0 && height > 0) {
            bitmap.scale(width, height, false)
        } else {
            bitmap
        }
        clearDrawing()
    }

    fun exportBitmap(): Bitmap {
        val resultBitmap = createBitmap(width, height)
        val resultCanvas = Canvas(resultBitmap)

        backgroundBitmap?.let {
            resultCanvas.drawBitmap(it, 0f, 0f, null)
        } ?: resultCanvas.drawColor(canvasBackgroundColor)

        drawingBitmap?.let { resultCanvas.drawBitmap(it, 0f, 0f, null) }
        return resultBitmap
    }

    fun clear() {
        backgroundBitmap = null
        clearDrawing()
    }

    private fun clearDrawing() {
        drawingBitmap?.eraseColor(Color.TRANSPARENT)
        invalidate()
    }

    private fun updateDrawingPaint() {
        drawingPaint.color = currentColor
        drawingPaint.strokeWidth = brushSize
        drawingPaint.xfermode = if (currentColor == Color.TRANSPARENT) {
            PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            drawingBitmap = createBitmap(w, h)
            drawingCanvas = Canvas(drawingBitmap!!)
            backgroundBitmap?.let {
                backgroundBitmap = it.scale(w, h, false)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        backgroundBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        } ?: canvas.drawColor(canvasBackgroundColor)

        drawingBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        when (currentMode) {
            Mode.DRAW -> canvas.drawPath(currentDrawingPath, drawingPaint)
            Mode.SHAPE_RECTANGLE -> canvas.drawPath(shapePreviewPath, drawingPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        val handled = when (currentMode) {
            Mode.DRAW -> handleDrawEvent(event, x, y)
            Mode.SHAPE_RECTANGLE -> handleShapeEvent(event, x, y)
        }

        if (event.action == MotionEvent.ACTION_UP && handled) {
            performClick()
        }

        invalidate()
        return handled
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun handleDrawEvent(event: MotionEvent, x: Float, y: Float): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> currentDrawingPath.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> currentDrawingPath.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                drawingCanvas?.drawPath(currentDrawingPath, drawingPaint)
                currentDrawingPath.reset()
            }

            else -> return false
        }
        return true
    }

    private fun handleShapeEvent(event: MotionEvent, x: Float, y: Float): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pathStartPoint = Pair(x, y)
                shapePreviewPath.reset()
            }

            MotionEvent.ACTION_MOVE -> {
                shapePreviewPath.reset()
                shapePreviewPath.addRect(
                    pathStartPoint.first,
                    pathStartPoint.second,
                    x,
                    y,
                    Path.Direction.CW
                )
            }

            MotionEvent.ACTION_UP -> {
                drawingCanvas?.drawRect(
                    pathStartPoint.first,
                    pathStartPoint.second,
                    x,
                    y,
                    drawingPaint
                )
                shapePreviewPath.reset()
                currentMode = Mode.DRAW
            }

            else -> return false
        }
        return true
    }
}
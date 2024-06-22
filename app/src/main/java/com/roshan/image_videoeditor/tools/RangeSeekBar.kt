package com.roshan.image_videoeditor.tools

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.roshan.image_videoeditor.R

interface OnRangeChangedListener {
    fun onRangeChanged(startProgress: Int, endProgress: Int)
}

class RangeSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint().apply {
        color = resources.getColor(R.color.appColor) // Thumb color
        strokeWidth = 15F
    }
    private val linePaint: Paint = Paint().apply {
        color = resources.getColor(R.color.gray) // Thumb color
        strokeWidth = 15F
    }

    private var barHeight = 10 // Height of the seek bar
    private var startThumbX = 0
    private var endThumbX = 0
    private var thumbRadius = 24 // Radius of the thumbs
    private var maxProgress = 100 // Maximum value of the progress (0 to 100 range)
    private var startProgress = 0 // Start position of the range
    private var endProgress = 100 // End position of the range (default to maximum)

    private var rangeChangedListener: OnRangeChangedListener? = null

//    init {
//        attrs?.let {
//            val ta = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar)
//            barHeight = ta.getDimensionPixelSize(R.styleable.RangeSeekBar_barHeight, barHeight)
//            thumbRadius = ta.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbRadius, thumbRadius)
//            ta.recycle()
//        }
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the range bar
        canvas.drawLine(
            thumbRadius.toFloat(),
            (height / 2).toFloat(),
            (width - thumbRadius).toFloat(),
            (height / 2).toFloat(),
            linePaint
        )

        // Calculate and draw the start thumb
        startThumbX = ((startProgress.toDouble() / maxProgress) * (width - 2 * thumbRadius) + thumbRadius).toInt()
        canvas.drawCircle(startThumbX.toFloat(), (height / 2).toFloat(), thumbRadius.toFloat(), paint)

        // Calculate and draw the end thumb
        endThumbX = ((endProgress.toDouble() / maxProgress) * (width - 2 * thumbRadius) + thumbRadius).toInt()
        canvas.drawCircle(endThumbX.toFloat(), (height / 2).toFloat(), thumbRadius.toFloat(), paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Calculate progress based on touch position
                var progress =
                    ((event.x - thumbRadius) / (width - 2 * thumbRadius) * maxProgress).toInt()

                // Ensure progress stays within bounds
                progress = when {
                    progress < 0 -> 0
                    progress > maxProgress -> maxProgress
                    else -> progress
                }

                // Determine which thumb is being moved
                if (Math.abs(event.x - startThumbX) < Math.abs(event.x - endThumbX)) {
                    startProgress = progress
                } else {
                    endProgress = progress
                }

                // Ensure startProgress is always less than endProgress
                if (startProgress >= endProgress) {
                    startProgress = endProgress - 1 // Adjust startProgress to be less than endProgress
                }

                invalidate() // Redraw the view

                // Notify listener about range change
                rangeChangedListener?.onRangeChanged(startProgress, endProgress)
            }
        }
        return true
    }

    fun setMax(max: Int) {
        maxProgress = max
        invalidate()
    }

    fun setOnRangeChangedListener(listener: OnRangeChangedListener) {
        rangeChangedListener = listener
    }

    fun getStartProgress(): Int {
        return startProgress
    }

    fun getEndProgress(): Int {
        return endProgress
    }

    fun setRange(start: Int, end: Int) {
        startProgress = start
        endProgress = end
        invalidate()
    }
}
package com.mygpi.mygpimobilefitness.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.mygpi.mygpimobilefitness.R

@TargetApi(Build.VERSION_CODES.N)
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
open class RoundedView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var gradientStartColor = DEFAULT_BORDER_COLOR
    private var gradientCenterColor = DEFAULT_BORDER_COLOR
    private var gradientEndColor = DEFAULT_BORDER_COLOR
    private var fillColor = DEFAULT_BORDER_COLOR
    private var backgroundGradient: GradientDrawable? = GradientDrawable()

    var radius: Float = 0f
        set(value) {
            field = value
            backgroundGradient?.apply {
                cornerRadius = value
            }
        }

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedView, 0, 0)
        try {
            radius = attributes.getDimension(R.styleable.RoundedView_radius, 0f)
            fillColor = attributes.getColor(R.styleable.RoundedView_fillColor, DEFAULT_BORDER_COLOR)
            gradientStartColor = attributes.getColor(R.styleable.RoundedView_start_color, DEFAULT_BORDER_COLOR)
            gradientCenterColor = attributes.getColor(R.styleable.RoundedView_center_color, DEFAULT_BORDER_COLOR)
            gradientEndColor = attributes.getColor(R.styleable.RoundedView_end_color, DEFAULT_BORDER_COLOR)
        } finally {
            attributes.recycle()
        }

        if (gradientEndColor != DEFAULT_BORDER_COLOR && gradientEndColor != DEFAULT_BORDER_COLOR && gradientCenterColor != DEFAULT_BORDER_COLOR) {
            backgroundGradient?.apply {
                colors = intArrayOf(gradientStartColor, gradientCenterColor, gradientEndColor)
            }
        } else {
            backgroundGradient?.apply {
                setColor(fillColor)
            }
        }

        background = backgroundGradient
    }

    fun setGradientColorList(startColor: Int, centerColor: Int, endColor: Int) {
        backgroundGradient?.clearColorFilter()
        backgroundGradient?.apply {
            colors = intArrayOf(startColor, centerColor, endColor)
        }
        background = backgroundGradient
    }

    companion object {

        private val DEFAULT_BORDER_COLOR = Color.WHITE
    }
}
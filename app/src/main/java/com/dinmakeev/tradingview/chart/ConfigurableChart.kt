package com.dinmakeev.tradingview.chart

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.dinmakeev.tradingview.R

open class ConfigurableChart(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    /**
     * Эти параметры можно менять в .xml
     */
    var mCandleBodySize: Float = AbstractChart.dpToPx(10).toFloat()
    var mCandleStringSize: Float = AbstractChart.dpToPx(3).toFloat()

    var highColor: Int = Color.parseColor("#2962FF")
    var lowColor: Int = Color.parseColor("#FF2929")

    var innerCircleColor: Int = highColor
    var outerCircleColor: Int = Color.parseColor("#552962FF")

    var innerCircleSize: Float = mCandleStringSize
    var outerCircleSize: Float = mCandleBodySize

    var mTextSize: Float = AbstractChart.spToPx(12f).toFloat()

    var xStep: Float = AbstractChart.dpToPx(20).toFloat()//50f

    var verticalTextStep: Float = AbstractChart.dpToPx(15).toFloat()
    var horizontalTextStep: Float = AbstractChart.dpToPx(2).toFloat()

    /**
     * Получение цвета по текущей теме
     */
    private fun getColor(id: Int): Int {
        val tv = TypedValue()
        context.theme.resolveAttribute(id, tv, true)
        return tv.data
    }

    /**
     * Поулчение цвета текста по текущей теме
     */
    private var mTextColor: Int = getColor(R.attr.colorOnPrimary)

    /**
     * Инстанс свечи
     */
    private var candlePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        this.strokeJoin = Paint.Join.MITER
        isAntiAlias = true
    }

    /**
     * Возвращает тело свечу при повышении
     */
    fun candleHighPaint() = candlePaint.apply {
        strokeWidth = mCandleBodySize
        color = Color.parseColor("#2962FF")
    }


    /**
     * Возвращает нить свечи при повышении
     */
    fun candleHighPaintSmall() = candlePaint.apply {
        strokeWidth = mCandleStringSize
        color = Color.parseColor("#2962FF")
    }


    /**
     * Вовзращает тело свечи при понижении
     */
    fun candleLowPaint() = candlePaint.apply {
        strokeWidth = mCandleBodySize
        color = Color.parseColor("#FF2929")
    }

    /**
     * Вовзращает нить свечи при понижении
     */
    fun candleLowPaintSmall() = candlePaint.apply {
        strokeWidth = mCandleStringSize
        color = Color.parseColor("#FF2929")
    }

    /**
     * Pain для горизонтального и вертикального текста
     */
    fun textPaint() = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        textSize = mTextSize
        color = mTextColor
    }


    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.dmChart, 0, 0).apply {
            try {
                mCandleBodySize =
                    getDimension(R.styleable.dmChart_candle_body_size, mCandleBodySize)
                mCandleStringSize =
                    getDimension(R.styleable.dmChart_candle_string_size, mCandleStringSize)

                highColor =
                    getColor(R.styleable.dmChart_high_color, highColor)
                lowColor =
                    getColor(R.styleable.dmChart_low_color, lowColor)

                innerCircleColor =
                    getColor(R.styleable.dmChart_inner_circle_color, innerCircleColor)
                outerCircleColor =
                    getColor(R.styleable.dmChart_outer_circle_color, outerCircleColor)

                innerCircleSize =
                    getDimension(R.styleable.dmChart_inner_circle_size, innerCircleSize)
                outerCircleSize =
                    getDimension(R.styleable.dmChart_outer_circle_size, outerCircleSize)

                mTextSize = getDimension(R.styleable.dmChart_text_size, mTextSize)

                xStep = getDimension(R.styleable.dmChart_step_x, xStep)

                verticalTextStep =
                    getDimension(R.styleable.dmChart_step_vertical_text, verticalTextStep)
                horizontalTextStep =
                    getDimension(R.styleable.dmChart_step_horizontal_text, horizontalTextStep)


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                recycle()
            }
        }
    }
}
package com.dinmakeev.tradingview.chart

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.dinmakeev.tradingview.R
import com.dinmakeev.tradingview.network.models.stocks.Data
import com.dinmakeev.tradingview.presentation.watchlist.WatchListItemModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

open class AbstractChart(context: Context, val attrs: AttributeSet?) : View(context, attrs) {

    companion object {

        /**
         * Конвертация пикселей в DensityPixel
         */
        fun pxToDp(px: Int) = (px / Resources.getSystem().displayMetrics.density).toInt()

        /**
         * Конвертация DensityPixel в пиксель
         */
        fun dpToPx(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()

        fun spToPx(spValue: Float): Int {
            val fontScale = Resources.getSystem().displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }
    }

    /**
     * Эти параметры можно менять в .xml
     */
    var mCandleBodySize: Float = dpToPx(10).toFloat()
    var mCandleStringSize: Float = dpToPx(3).toFloat()
    var mTextSize: Float = spToPx(12f).toFloat()
    var xStep: Float = 50f
    var yStep: Float = dpToPx(20).toFloat()
    var verticalTextStep: Float = dpToPx(2).toFloat()

    /**
     * Параметр хранит перемещение по X,Y
     */
    var scrolledY = 0
    var scrolledX = 0

    /**
     * Scale по X,Y
     */
    var mScaleX = 1.0
    var mScaleY = 1.0

    /**
     * Общий Scale
     */
    private var scaleFactor = 1.0

    var maxY: Float = 0f
    var minY: Float = 0f

    open var data: MutableList<Data> = mutableListOf()
    open fun addData(_d: WatchListItemModel) {
        val d = Data(_d)
        data.add(d)
        calculateMinMax(data)
        invalidate()
    }

    /**
     * После апдейта целого списка - надо заново найти минимальные и максимальные координаты
     */
    open fun update(list: List<Data>) {
        calculateMinMax(list)
        scrollX = 0
        scrollY = 0
        scrollBy((list.size * xStep  - dpToPx(width/4)).toInt(), 0)
        scrolledX = scrollX+width
        scrolledY = height
    }

    fun calculateMinMax(list: List<Data>) {
        maxY = list.map { it.max() }.maxOrNull()?.toFloat()!!
        minY = list.map { it.max() }.minOrNull()?.toFloat()!!
    }

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
    var mTextColor: Int = getColor(R.attr.colorOnPrimary)

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


    /**
     * Конвертация координат View в декартовы
     */
    fun getY(y: Double) =
        (height - ((height) / maxY * y)).toFloat() * mScaleY.toFloat()

    fun getX(x: Float) = x * mScaleX.toFloat()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        scrolledX = width+scrollX
        scrolledY = height
    }


    /**
     * Слушатель жестов масштабирования
     */
    private var mScaleDetector = ScaleGestureDetector(context, ScaleListener())

    /**
     * Слушатель жестов движений
     */
    private var mMoveDetector = GestureDetector(context, MoveListener())

    private inner class MoveListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            scrolledY = scrollY + height
            scrolledX = scrollX + width
            scrollBy(distanceX.toInt(), distanceY.toInt())
            return true
        }

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            this@AbstractChart.scaleFactor *= detector.scaleFactor / dpToPx(80)
            this@AbstractChart.mScaleX += (detector.currentSpanX - detector.previousSpanX) / max(scrolledX,dpToPx(data.size))
            this@AbstractChart.mScaleY += (detector.currentSpanY - detector.previousSpanY) / dpToPx(110)


            scrollBy((data.size*xStep*(detector.currentSpanX - detector.previousSpanX) / max(scrolledX,dpToPx(data.size))).toInt(),0)




            scrollBy((data.size*xStep*(detector.currentSpanX - detector.previousSpanX) / scrolledX).toInt(),0)

            this@AbstractChart.mScaleX = Math.max(0.2, Math.min(this@AbstractChart.mScaleX, 8.0))
            this@AbstractChart.mScaleY = Math.max(0.2, Math.min(this@AbstractChart.mScaleY, 8.0))


            invalidate()
            return true
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(event)
        mMoveDetector.onTouchEvent(event)
        return true
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.dmChart, 0, 0).apply {
            try {
                mCandleBodySize =
                    getDimension(R.styleable.dmChart_candleBodySize, mCandleBodySize)
                mCandleStringSize =
                    getDimension(R.styleable.dmChart_candleStringSize, mCandleStringSize)
                mTextSize = getDimension(R.styleable.dmChart_mTextSize, mTextSize)
                xStep = getDimension(R.styleable.dmChart_step_x, xStep)
                yStep = getDimension(R.styleable.dmChart_step_y, yStep)
                verticalTextStep =
                    getDimension(R.styleable.dmChart_step_vertical_text, verticalTextStep)


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                recycle()
            }
        }
    }



    fun drawPriceText(canvas: Canvas) {
        for (i in minY.toInt() until maxY.toInt() step (yStep / mScaleY).toInt()) {
            canvas.drawText(
                "${(i.toFloat()).round(2)}",
                (scrolledX.toFloat() - dpToPx(20)),
                getY(i.toDouble()),
                textPaint()
            )
        }
    }


     fun drawDate(canvas: Canvas) {
         var x = (this.data.size - data.size)*xStep
        for (i in data.indices step (verticalTextStep * mScaleX).toInt()) {
            if (getX(x)>scrolledX-width && getX(x)<scrolledX+width) {
                val d = data[i]
                canvas.drawText(
                    "${d.date.toFormat()}",
                    getX(x),
                    -dpToPx(5).toFloat() + scrolledY,
                    textPaint()
                )
            }
            x += xStep * (verticalTextStep * mScaleX).toInt()
        }
    }



    fun String.toFormat(format: String = "HH:mm"): String {
        val inputFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormat = SimpleDateFormat(format)
        val d: Date = inputFormat.parse(this)
        val formattedDate: String = outputFormat.format(d)
        return formattedDate
    }


    /**
     * Округление Float
     * @param decimals количество знаков после запятой
     */
    fun Float.round(decimals: Int): Float {
        var multiplier = 1f
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }


}
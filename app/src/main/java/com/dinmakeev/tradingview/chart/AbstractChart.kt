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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class AbstractChart(context: Context, attrs: AttributeSet?) : View(context, attrs) {

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

    var highColor: Int = Color.parseColor("#2962FF")
    var lowColor: Int = Color.parseColor("#FF2929")

    var innerCircleColor: Int = highColor
    var outerCircleColor: Int = Color.parseColor("#552962FF")

    var innerCircleSize: Float = mCandleStringSize
    var outerCircleSize: Float = mCandleBodySize

    private var mTextSize: Float = spToPx(12f).toFloat()

    var xStep: Float = dpToPx(20).toFloat()//50f

    var verticalTextStep: Float = dpToPx(15).toFloat()
    var horizontalTextStep: Float = dpToPx(2).toFloat()


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

    /**
     * Максимальные и минимальные значения цен
     */
    private var maxY: Float = 0f
    private var minY: Float = 0f

    private var track: Boolean = true

    open var data: MutableList<Data> = mutableListOf()
    open fun addData(_d: WatchListItemModel) {
        val d = Data(_d)
        data.add(d)
        calculateMinMax(data)
        if (track)
            scrollToLast()
        invalidate()
    }

    /**
     * После апдейта целого списка - надо заново найти минимальные и максимальные координаты
     */
    open fun update(list: List<Data>) {
        if (list.isNullOrEmpty())
            return

        calculateMinMax(list)
        scrollToLast(list)
    }

    private fun scrollToLast(list: List<Data>? = null) {
        mScaleY = 8.0 / (maxY / minY).toDouble()
        scrollX = 0
        scrollY = 0
        val y = list?.lastOrNull()?.open ?: data.lastOrNull()?.open ?: 0.0

        scrollBy(
            ((list?.size ?: data.size) * xStep - dpToPx(width / 4)).toInt(),
            getY(y).toInt() - height / 2
        )
        scrolledX = scrollX + width
        scrolledY = scrollY + height
    }

    private fun calculateMinMax(list: List<Data>) {
        if (list.isNullOrEmpty())
            return
        maxY = list.map { it.max() }.maxOrNull()?.toFloat()!!
        minY = list.map { it.min() }.minOrNull()?.toFloat()!!
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
    private fun textPaint() = Paint().apply {
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
        scrolledX = width + scrollX
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

    private val TAG = "AbstractChart"

    val plotWidth: Int
        get() = (this.data.size * xStep).toInt()
    val plotHeight: Int
        get() = abs(getY(minY.toDouble()) - getY(maxY.toDouble())).toInt()

    private inner class MoveListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            track = false
            scrolledY = scrollY + height
            scrolledX = scrollX + width
            
            val toScrollOnX = when {
                (scrollX + width / 2 > plotWidth && distanceX > 0) -> 0
                (scrollX + width / 2 < 0 && distanceX < 0) -> 0
                else -> distanceX.toInt()
            }
            val toScrollOnY = when {
                (scrollY + height / 2 < 0 && distanceY < 0) -> 0
                (scrollY + height / 2 > plotHeight && distanceY > 0) -> 0
                else -> distanceY.toInt()
            }


            scrollBy(toScrollOnX, toScrollOnY)
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            track = true
            scrollToLast()
            return super.onDoubleTap(e)
        }

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            track = false
            val scale = dpToPx(110)
            this@AbstractChart.scaleFactor *= detector.scaleFactor / scale
            this@AbstractChart.mScaleX += (detector.currentSpanX - detector.previousSpanX) / scrolledX
            this@AbstractChart.mScaleY += (detector.currentSpanY - detector.previousSpanY) / scale


            this@AbstractChart.mScaleX = max(0.5, min(this@AbstractChart.mScaleX, 8.0))
            this@AbstractChart.mScaleY = max(0.2, min(this@AbstractChart.mScaleY, 8.0))

            //После скейлинга двигаем наш view чтобы график не убегал
            val amountY =
                -(verticalTextStep * (detector.currentSpanY - detector.previousSpanY) / (dpToPx(110))).toInt()
            scrollBy(0, amountY)

            val amountX =
                (data.size * xStep * (detector.currentSpanX - detector.previousSpanX) / scrolledX / width)
            scrollBy(amountX.toInt(), 0)

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


    /**
     * Рисуем цены учитывая отступ. Ужасный способ, но по-другому не додумались :(
     * */
    fun drawPriceText(canvas: Canvas) {

        if (data.isNullOrEmpty())
            return
        var yMargin = 0f
        var prevY = getY(minY.toInt().toDouble())


        for (i in minY.toInt() until maxY.toInt() step 1) {
            val y = getY(i.toDouble())
            if (yMargin > verticalTextStep) {
                yMargin = 0f
                canvas.drawText(
                    "${(i.toFloat()).round(2)}",
                    (scrolledX.toFloat() - dpToPx(20)),
                    y,
                    textPaint()
                )
            }
            yMargin += abs(y - prevY)
            prevY = y
        }
    }


    /**
     * Тут рисуем дату таким образом, чтобы она как и цены не наезжала друг на друга
     *
     * Нужно взять максимальный шаг между стандартным и масштабируемым
     *
     * Соответственно по иксу мы должны идти со стандартным шагом, умноженным на новый шаг
     */
    fun drawDate(canvas: Canvas) {
        if (data.isNullOrEmpty())
            return
        var x = 0f
        val step = max(horizontalTextStep.toInt(), (horizontalTextStep * mScaleX).toInt())
        for (i in data.indices step step.toInt()) {
            if (getX(x) > scrolledX - width && getX(x) < scrolledX + width) {
                val d = data[i]
                canvas.drawText(
                    d.date.toFormat(),
                    getX(x),
                    -dpToPx(5).toFloat() + scrolledY,
                    textPaint()
                )
            }
            x += xStep * (step).toInt()
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun String.toFormat(format: String = "HH:mm"): String {
        val inputFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormat = SimpleDateFormat(format)
        val d: Date? = inputFormat.parse(this)
        return outputFormat.format(d ?: return "")
    }


    /**
     * Округление Float
     * @param decimals количество знаков после запятой
     */
    private fun Float.round(decimals: Int): Float {
        var multiplier = 1f
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }


}
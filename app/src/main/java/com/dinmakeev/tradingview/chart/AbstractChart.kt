package com.dinmakeev.tradingview.chart

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.dinmakeev.tradingview.network.models.stocks.Data
import com.dinmakeev.tradingview.presentation.chart.ChartViewModel
import com.dinmakeev.tradingview.presentation.watchlist.WatchListItemModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class AbstractChart(context: Context, attrs: AttributeSet?) :
    ConfigurableChart(context, attrs) {

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
     * Scale по X,Y
     */
    var mScaleX = 1.0
    var mScaleY = 1.0

    /**
     * Общий Scale
     */
    var scaleFactor = 1.0


    /**
     * Максимальные и минимальные значения цен
     */
    private var maxY: Float = 0f
    private var minY: Float = 0f

    var track: Boolean = true

    var data: MutableList<Data> = mutableListOf()
    /**
     * Параметр хранит перемещение по X,Y
     */
    val scrolledY: Int
        get() = scrollY + height
    val scrolledX: Int
        get() = scrollX + width
    var isUpdating = true

    /**
     * Добавление данных, которые приходят через веб-сокет
     */
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
        if (list.isNullOrEmpty()) {
            isUpdating = false
            return
        }
        data = if (ChartViewModel.offset.value == 0)
            list.toMutableList()
        else
            list.toMutableList().apply { addAll(data) }
        calculateMinMax(data)
        if (ChartViewModel.offset.value != 0)
            scrollToLast(list)
        else scrollToLast()
        isUpdating = false
        invalidate()
    }

    private fun scrollToLast() {
        mScaleY = 8.0 / (maxY / minY).toDouble()
        scrollX = 0
        scrollY = 0
        val y = data.lastOrNull()?.open ?: 0.0
        val x = (plotWidth - width / 2).toInt()
        val yAddition = -height / 2
        scrollBy(x, getY(y).toInt() + yAddition)
    }

    private fun scrollToLast(list: List<Data>) {
        val x = (list.size * xStep).toInt()
        scrollBy(x, 0)
    }


    /**
     * Тут мы получаем позицию элемента, который находится в центре экрана
     * Либо 0 или data.size если мы вышли за границы
     */
    val currentElement: Int
        get() {
            var elem = (scrollX / (plotWidth - width / 2.0) * data.size).toInt()
            elem = min(data.size, elem)
            elem = max(elem, 0)
            return elem
        }


    private fun calculateMinMax(list: List<Data>) {
        if (list.isNullOrEmpty())
            return
        maxY = list.map { it.max() }.maxOrNull()?.toFloat()!!
        minY = list.map { it.min() }.minOrNull()?.toFloat()!!
    }


    /**
     * Конвертация координат View в декартовы
     */
    fun getY(y: Double) =
        (height - ((height) / maxY * y)).toFloat() * mScaleY.toFloat()

    fun getX(x: Float) = x * mScaleX.toFloat()

    fun isXInView(x:Float) = getX(x) > scrolledX - width - xStep && getX(x) < scrolledX + width + xStep

    /**
     * Слушатель жестов масштабирования
     */
    private var mScaleDetector = ScaleGestureDetector(context, ScaleListener())

    /**
     * Слушатель жестов движений
     */
    private var mMoveDetector = GestureDetector(context, MoveListener())

    private val TAG = "AbstractChart"

    /**
     * Настоящая ширина и высота графика
     */
    val plotWidth: Int
        get() = (data.size * xStep*mScaleX).toInt()
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
            //Проверям, чтоб мы не вышли за границы X
            val toScrollOnX = when {
                (scrollX + width / 2 > plotWidth && distanceX > 0) -> 0
                (scrollX + width / 2 < 0 && distanceX < 0) -> 0
                else -> distanceX.toInt()
            }
            //Проверям, чтоб мы не вышли за границы Y
            val toScrollOnY = when {
                (scrollY + height / 2 < 0 && distanceY < 0) -> 0
                (scrollY + height / 2 > plotHeight && distanceY > 0) -> 0
                else -> distanceY.toInt()
            }

            val offset = ChartViewModel.offset.value ?: 0
            if (distanceX < 0 && currentElement < min(15, data.size)) {
                if (!isUpdating)
                    ChartViewModel.offset.value = offset + 100
                isUpdating = true
            }
            scrollBy(toScrollOnX, toScrollOnY)
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            reset()
            return super.onDoubleTap(e)
        }

    }
    fun reset(){

        track = true
        if (ChartViewModel.offset.value == 0)
            scrollToLast()
        if (ChartViewModel.offset.value != 0) {
            if (!isUpdating) {
                ChartViewModel.offset.value = 0
                isUpdating = true
            }
        }
    }
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            track = false
            val scale = dpToPx(60)
            scaleFactor *= detector.scaleFactor / scale
            mScaleX += (detector.currentSpanX - detector.previousSpanX) / scrolledX
            mScaleY += (detector.currentSpanY - detector.previousSpanY) / scale


            mScaleX = max(0.5, min(mScaleX, 8.0))
            mScaleY = max(0.2, min(mScaleY, 8.0))

            //После скейлинга двигаем наш view чтобы график не убегал
            val amountY =
                -(verticalTextStep * (detector.currentSpanY - detector.previousSpanY) / (dpToPx(60))).toInt()
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
        println("MinMax=${minY} ${maxY}")


        for (i in minY.toInt() - (maxY / minY).toInt() until maxY.toInt() + (maxY / minY).toInt() step 1) {
            val y = getY(i.toDouble())
            if (yMargin >= verticalTextStep) {
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
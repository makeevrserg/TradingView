package com.dinmakeev.tradingview.chart


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.dinmakeev.tradingview.presentation.watchlist.WatchListItemModel


class KChart(context: Context, _attrs: AttributeSet?) : AbstractChart(context, _attrs) {


    override fun addData(_d: WatchListItemModel) {}
    private val TAG = "KChart"


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPlot(canvas)
        drawPriceText(canvas)
        drawDate(canvas)

    }

    private fun drawPlot(canvas: Canvas) {
        var x = 0f
        for (i in data.indices) {
            if (isXInView(x)) {
                val d = data[i]
                val candlePaint = if (d.isHigh()) candleHighPaint() else candleLowPaint()
                val smallPaint = if (d.isHigh()) candleHighPaintSmall() else candleLowPaintSmall()
                canvas.drawLine(getX(x), getY(d.close), getX(x), getY(d.open), candlePaint)
                canvas.drawLine(
                    getX(x),
                    getY(d.low),
                    getX(x),
                    getY(d.high),
                    smallPaint
                )
            }
            x += xStep
        }
    }


}
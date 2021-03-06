package com.dinmakeev.tradingview.chart


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.dinmakeev.tradingview.network.models.watchlists.WatchListItemModel


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

            if (getX(x) > scrolledX - width-xStep && getX(x) < scrolledX + width+xStep) {
                val d = data[i]
                if (d.isHigh()) {
                    canvas.drawLine(
                        getX(x),
                        getY(d.close),
                        getX(x),
                        getY(d.open),
                        candleHighPaint()
                    )
                    canvas.drawLine(
                        getX(x),
                        getY(d.low),
                        getX(x),
                        getY(d.high),
                        candleHighPaintSmall()
                    )
                } else {
                    canvas.drawLine(getX(x), getY(d.close), getX(x), getY(d.open), candleLowPaint())
                    canvas.drawLine(
                        getX(x),
                        getY(d.low),
                        getX(x),
                        getY(d.high),
                        candleLowPaintSmall()
                    )
                }
            }
            x += xStep
        }
    }


}
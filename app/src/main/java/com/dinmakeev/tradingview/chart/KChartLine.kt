package com.dinmakeev.tradingview.chart


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.dinmakeev.tradingview.network.models.stocks.Data
import com.dinmakeev.tradingview.presentation.chart.ChartViewModel
import com.dinmakeev.tradingview.presentation.watchlist.WatchListItemModel


class KChartLine(context: Context, _attrs: AttributeSet?) : AbstractChart(context, _attrs) {
    var isRealTime = false


    override fun addData(_d: WatchListItemModel) {
        if (ChartViewModel.offset.value!=0)
            return
        if (_d.data.price == null)
            return
        isRealTime = true
        super.addData(_d)
    }



    private val TAG = "KChart"


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPlot(canvas)
        drawPriceText(canvas)
        drawDate(canvas)



    }


    private fun drawPlot(canvas: Canvas) {

        if (data.isNullOrEmpty())
            return
        var x = 0f
        for (i in 0 until data.size - 1) {

            if (getX(x)>scrolledX-width-xStep && getX(x)<scrolledX+width+xStep) {
                val d = data[i]
                val nd = data[i + 1]
                canvas.drawLine(
                    getX(x),
                    getY(d.open),
                    getX(x + xStep),
                    getY(nd.open),
                    candleHighPaintSmall()
                )
            }
            x += xStep

        }
        val last = data.last()
        val yCoord = if (!isRealTime) getY(last.open) else getY(last.open)
        canvas.drawCircle(
            getX(x),
            yCoord,
            mCandleStringSize,
            candleLowPaintSmall().apply { color = Color.parseColor("#2962FF") })
        canvas.drawCircle(getX(x), yCoord, mCandleBodySize, candleLowPaintSmall().apply {
            color = Color.parseColor("#552962FF")
            style = Paint.Style.FILL
        })
    }


}
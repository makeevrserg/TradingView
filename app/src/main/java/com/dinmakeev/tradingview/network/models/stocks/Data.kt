package com.dinmakeev.tradingview.network.models.stocks

import com.dinmakeev.tradingview.presentation.watchlist.WatchListItemModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

private fun getDate() = Calendar.getInstance().time
private fun formatDate(): String {
    val date = getDate()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return format.format(date).toString()
}

data class Data(
    val close: Double,
    val date: String = formatDate(),
    val high: Double = close,
    val last: Double = close,
    val low: Double = close,
    val `open`: Double = close,
    val symbol: String,
    val volume: Int = 0
) {

    constructor(d:WatchListItemModel):this(close = (d.data.price?.toDouble())?:0.0,symbol = d.symbol)
    fun isLow() = open > close
    fun isHigh() = open < close

    fun max() = listOf(close,high,low,open).maxOrNull()?:high
    fun min() = listOf(close,high,low,open).minOrNull()?:low

    companion object{
        fun createList(s:Int = 200): List<Data> {
            var close:Double? = null
            return (0 until s).map { i ->
                val d = Data.random(close,i)
                close = d.close
                d
            }
        }
        fun random(close:Double?,i:Int = 0): Data {
            val date = "2021-09-21T17:00:00.000Z"
            val open = close?:Random.nextDouble(130.0,150.0)
            val low = Random.nextDouble(130.0,open)
            val high = Random.nextDouble(open,150.0)
            val close = Random.nextDouble(low,high)
            return Data(
                close,date,high,0.0,low,open,"AAPL",0)
        }
    }
}
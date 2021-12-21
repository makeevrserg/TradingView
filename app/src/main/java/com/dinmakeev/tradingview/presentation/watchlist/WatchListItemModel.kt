package com.dinmakeev.tradingview.presentation.watchlist

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import kotlin.math.abs

data class WatchListItemModel(
    @SerializedName("data")
    val data: Data,
    var type: String?=null
) {
    val symbol: String
        get() = data.symbol

    data class Data(
        var description: String?=null,
        var icon: String?=null,
        var symbol: String,
        var bitmap:Bitmap?=null,
        var price: Float?=null,
        var oldPrice: Float? = price
    )
    fun getPriceOrEmpty():String = data.price?.round(2)?.toString()?:""

    fun isPositive() = (data.price ?: 0f) >= (data.oldPrice ?: 1f)
    fun changeToString(): String? {
        data.price ?: return null
        val diff = abs(data.price?.minus(data.oldPrice?:0f)?:return "")
        return if (!isPositive()) "-${diff.round(2)}"
        else "+${diff.round(2)}"
    }

    fun percentChangeToString(): String? {
        if (data.oldPrice == null)
            data.oldPrice = data.price
        val change = (data.price?.div(data.oldPrice!!))?.round(2) ?: return null
        return if (!isPositive()) "-${change}%"
        else "+$change%"
    }
    fun changeAndPercentToString() = if (data?.price==null) "" else "${changeToString()} ${percentChangeToString()}"

    /**
     * Только для тестинга
     */
    fun Float.round(decimals: Int): Float {
        var multiplier = 1f
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    fun updateData(watchListItem: WatchListItemModel?) {

        type = watchListItem?.type?:type
        data.oldPrice = data.price?:data.oldPrice
        data.price = watchListItem?.data?.price?:data.price
        data.description = watchListItem?.data?.description?:data.description
        data.icon = watchListItem?.data?.icon?:data.icon
        data.symbol = watchListItem?.data?.symbol?:data.symbol
    }
}
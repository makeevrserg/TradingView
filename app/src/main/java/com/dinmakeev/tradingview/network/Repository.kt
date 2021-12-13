package com.dinmakeev.tradingview.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.utils.MessageHandler

class Repository {
    private var api: Api = Api()
    suspend fun fetchWatchListsList() = suspendCatching {
        api.fetchWatchListsList()
    }

    suspend fun fetchWatchList(id: Int) = suspendCatching {
        api.fetchWatchList(id)
    }

    suspend fun fetchIntraDayStock(symbolName: String, offset: Int = 1) = suspendCatching {
        api.fetchIntraDayStock(symbolName, offset)
    }


}

suspend inline fun <T> suspendCatching(block: suspend () -> T?): T? {
    return try {
        val result = block()
        result
    } catch (e: Throwable) {
        App._notifyMessage.postValue(MessageHandler("Не удалось подключиться к серверу"))
        e.printStackTrace()
        null
    }
}

inline fun <T> catching(block: () -> T?): T? {
    return try {
        val result = block()
        result
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}
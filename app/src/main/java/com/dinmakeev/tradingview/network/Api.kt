package com.dinmakeev.tradingview.network

import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.network.models.stocks.Stock
import com.dinmakeev.tradingview.network.models.watchlists.Watchlists
import com.dinmakeev.tradingview.network.models.watchlists.WatchlistsItem
import com.dinmakeev.tradingview.utils.MessageHandler
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class Api {

    private fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10L, TimeUnit.SECONDS)
        .connectTimeout(10L, TimeUnit.SECONDS)
        .readTimeout(10L, TimeUnit.SECONDS)
        .writeTimeout(10L, TimeUnit.SECONDS)
        .build()


    private fun retrofit(): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient())
            .baseUrl("https://alex9.fvds.ru/")
            .build()



    fun createTradingApi(): TradingApi? = retrofit()?.create(TradingApi::class.java)


    private var api = createTradingApi()


    suspend fun fetchWatchListsList(): Watchlists? = safeCall(api?.fetchWatchListsList())
    suspend fun fetchWatchList(id: Int): WatchlistsItem? = safeCall(api?.fetchWatchList(id))
    suspend fun fetchIntraDayStock(symbolName: String, offset: Int): Stock? =
        safeCall(api?.fetchIntraDayStock(symbolName, offset))



    private suspend inline fun <T> safeCall(
        call: Call<T>?,
        onError: (Exception) -> Unit = { exception -> exception.printStackTrace() }
    ): T? {
        val req = call?.awaitResponse() ?: return null
        return when {
            req.isSuccessful -> req.body()
            else -> throw IOException()
        }
    }


}
package com.dinmakeev.tradingview.network

import com.dinmakeev.tradingview.network.models.stocks.Stock
import com.dinmakeev.tradingview.network.models.watchlists.Watchlists
import com.dinmakeev.tradingview.network.models.watchlists.WatchlistsItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TradingApi {

    @GET("watchlists")
    fun fetchWatchListsList(): Call<Watchlists>

    @GET("watchlists/{id}")
    fun fetchWatchList(
        @Path("id") id:Int
    ): Call<WatchlistsItem>

    @GET("/stocks/intraday/{symbolName}")
    fun fetchIntraDayStock(
        @Path("symbolName") symbolName: String,
        @Query("offset") offset: Int = 1
    ):Call<Stock>

}


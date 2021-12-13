package com.dinmakeev.tradingview.network.models.watchlists

data class SingleWatchList(
    val id: Int,
    val name: String,
    val symbols: List<String>
)
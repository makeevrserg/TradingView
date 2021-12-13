package com.dinmakeev.tradingview.network.models.watchlists

data class WatchlistsItem(
    val id: Int,
    val name: String,
    val symbols: List<String>
)
package com.dinmakeev.tradingview.network.models.stocks

data class Stock(
    val `data`: List<Data>,
    val page: Page
)
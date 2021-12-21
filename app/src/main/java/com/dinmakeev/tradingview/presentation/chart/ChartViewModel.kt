package com.dinmakeev.tradingview.presentation.chart

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.network.WebSocketClient
import com.dinmakeev.tradingview.network.catching
import com.dinmakeev.tradingview.network.models.stocks.Data
import com.dinmakeev.tradingview.network.models.watchlists.WatchListItemModel
import com.google.gson.Gson

class ChartViewModel(application: Application) : AndroidViewModel(application) {
    val toolbarTitle = MutableLiveData<String>()
    var connection: WebSocketClient? = null
    var data = MutableLiveData<List<Data>>()

    //    //Для теста графика
//    var data = MutableLiveData<List<Data>>(Data.createList(100))
    val newData = MutableLiveData<WatchListItemModel>()
    val repository = (application as App).repository
    val stockItem: MutableLiveData<WatchListItemModel> = MutableLiveData()
    val loading = MutableLiveData<Boolean>(true)

    companion object {
        var offset = MutableLiveData<Int>(0)
    }

    val messageHandler = object : WebSocketClient.MessageHandler {
        override suspend fun handleMessage(message: String?) {
            Log.d("Repository", "handleMessage: ${stockItem.value?.symbol}: ${message}")
            if (message?.contains("Pong is not received") == true) {
                connection?.openConnection()
                connection?.subscribe(stockItem.value?.symbol ?: return)
                return
            }
            catching {
                val watchListItem =
                    Gson().fromJson<WatchListItemModel>(message, WatchListItemModel::class.java)
                watchListItem.data.bitmap = repository.fetchIcon(watchListItem.symbol)
                val item = stockItem.value ?: return
                item.updateData(watchListItem)
                newData.postValue(item)
                stockItem.postValue(item)
            }
        }
    }

    fun create(symbol: String) {
        val item = WatchListItemModel(WatchListItemModel.Data(symbol = symbol))
        stockItem.value = item
        toolbarTitle.value = symbol
        offset.value = 0
    }

    private suspend fun connectWebSocket() {
        connection = WebSocketClient.createConnection(
            stockItem.value?.symbol ?: return,
            messageHandler
        )
    }


    suspend fun loadData(offset: Int = 0) {
        var offset = offset
        if (offset != 0 && data.value.isNullOrEmpty()) {
            offset = 0
            ChartViewModel.offset.postValue(0)
        }
        loading.postValue(true)
        stockItem.value?.symbol?.let { symbol ->
            val fetched = repository.fetchIntraDayStock(symbol, offset = offset)?.data ?: listOf()
            data.postValue(fetched.filter { it.volume != null }.reversed())
            if (!fetched.isNullOrEmpty() && connection==null)
                connectWebSocket()
        }
        loading.postValue(false)
    }

    override fun onCleared() {
        connection?.closeConnection()
        super.onCleared()
    }
}
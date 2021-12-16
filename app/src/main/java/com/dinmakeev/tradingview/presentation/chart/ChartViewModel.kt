package com.dinmakeev.tradingview.presentation.chart

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.network.WebSocketClient
import com.dinmakeev.tradingview.network.catching
import com.dinmakeev.tradingview.network.models.stocks.Data
import com.dinmakeev.tradingview.presentation.watchlist.WatchListItemModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChartViewModel(application: Application) : AndroidViewModel(application) {
    val toolbarTitle = MutableLiveData<String>()
    var connection: WebSocketClient? = null
//    var data = MutableLiveData<List<Data>>()
//    //Для теста графика
    var data = MutableLiveData<List<Data>>(Data.createList(100))
    val newData = MutableLiveData<WatchListItemModel>()
    val repository = (application as App).repository
    val stockItem: MutableLiveData<WatchListItemModel> = MutableLiveData()

    val messageHandler = object : WebSocketClient.MessageHandler {
        override fun handleMessage(message: String?) {
            Log.d("Repository", "handleMessage: ${stockItem.value?.symbol}: ${message}")
            if (message?.contains("Pong is not received") == true || message?.contains("error") == true) {
                connection?.openConnection()
                connection?.sendMessage(
                    WebSocketClient.getMessage(
                        stockItem.value?.symbol ?: return
                    )
                )
                return
            }
            catching {
                val watchListItem =
                    Gson().fromJson<WatchListItemModel>(message, WatchListItemModel::class.java)
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
        viewModelScope.launch(Dispatchers.IO) {
            val fetched=repository.fetchIntraDayStock(stockItem.value?.symbol?:return@launch)?.data?: listOf()
            data.postValue(fetched.filter { it.volume!=null })
            connection = WebSocketClient.createConnection(
                stockItem.value?.symbol ?: return@launch,
                messageHandler
            )
        }
    }

    override fun onCleared() {
        connection?.closeConnection()
        super.onCleared()
    }
}
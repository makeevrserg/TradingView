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
import java.text.SimpleDateFormat
import java.util.*

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
    companion object{
        var offset = MutableLiveData<Int>(0)
    }

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
        offset.value = 0
        loadData()

        viewModelScope.launch(Dispatchers.IO) {
            connection = WebSocketClient.createConnection(
                stockItem.value?.symbol ?: return@launch,
                messageHandler
            )
        }
    }

    fun loadData(offset:Int = 0){
        loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            stockItem.value?.symbol?.let {symbol->
                val fetched=repository.fetchIntraDayStock(symbol,offset = offset)?.data?: listOf()
                data.postValue(fetched.filter { it.volume!=null }.reversed())
//                    .sortedBy {
//                    val inputFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//                    val d: Date? = inputFormat.parse(it.date)
//                    d?.time
//                })
            }
            loading.postValue(false)
        }

    }

    override fun onCleared() {
        connection?.closeConnection()
        super.onCleared()
    }
}
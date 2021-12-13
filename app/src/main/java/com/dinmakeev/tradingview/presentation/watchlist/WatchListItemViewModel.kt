package com.dinmakeev.tradingview.presentation.watchlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.network.WebSocketClient
import com.dinmakeev.tradingview.network.catching
import com.dinmakeev.tradingview.utils.MessageHandler
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WatchListItemViewModel(
    application: Application,
    _trackItem: WatchListItemModel,
    private val onTrackClicked: (WatchListItemModel) -> Unit,
) : AndroidViewModel(application) {

    val app = application as App
    val repository = app.repository

    val trackItem: MutableLiveData<WatchListItemModel> = MutableLiveData(_trackItem)

    var connection: WebSocketClient? = null
    var listener: Any? = null


    val messageHandler = object : WebSocketClient.MessageHandler {
        override fun handleMessage(message: String?) {
            Log.d("Repository", "handleMessage: ${trackItem.value?.symbol}: ${message}")
            if (message?.contains("Pong is not received") == true) {
                connection?.openConnection()
                connection?.sendMessage(WebSocketClient.getMessage(trackItem.value?.symbol?:return))
                return
            }

            catching {
                val watchListItem =
                    Gson().fromJson<WatchListItemModel>(message, WatchListItemModel::class.java)
                val item = trackItem.value ?: return
                item.updateData(watchListItem)
                trackItem.postValue(item)
            }


        }

    }

    fun clicked(){
        val item = trackItem.value
        if (item==null){
            App._notifyMessage.value = MessageHandler("Данные еще не загрузились")
            return
        }
        onTrackClicked(trackItem.value?:return)
    }

    fun connectWebSocket() =
        viewModelScope.launch(Dispatchers.IO) {
            if (connection==null)
            connection = WebSocketClient.createConnection(trackItem.value?.symbol?:return@launch, messageHandler)
            else {
                connection?.openConnection()
                connection?.sendMessage(WebSocketClient.getMessage(trackItem.value?.symbol?:return@launch))
            }
        }


    init {
        Log.d("Repository", ": Init")
        connectWebSocket()

    }
}
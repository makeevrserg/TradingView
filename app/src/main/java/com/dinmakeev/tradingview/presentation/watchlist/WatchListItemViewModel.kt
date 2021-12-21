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

    val stockItem: MutableLiveData<WatchListItemModel> = MutableLiveData(_trackItem)

    var connection: WebSocketClient? = null

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
                val item = stockItem.value ?: return@catching
                item.updateData(watchListItem)
                stockItem.postValue(item)
            }


        }

    }

    fun clicked() {
        val item = stockItem.value
        if (item == null) {
            App._notifyMessage.value = MessageHandler("Данные еще не загрузились")
            return
        }
        onTrackClicked(stockItem.value ?: return)
    }

    /**
     * Создаем не новое соединение, а пытаемся получить старое, потому что при фильтре будут создаваться новые viewModel с новыми подключениями.
     *
     * Таким образом избегаем этого
     */
    fun connectWebSocket() =
        viewModelScope.launch(Dispatchers.IO) {
            connection = WebSocketClient.getOrCreateConnection(
                stockItem.value?.symbol ?: return@launch,
                messageHandler
            )
            WebSocketClient.addToCache(stockItem.value?.symbol, connection)

        }


    override fun onCleared() {
        super.onCleared()
        connection?.closeConnection()
    }

    init {
        connectWebSocket()
    }
}
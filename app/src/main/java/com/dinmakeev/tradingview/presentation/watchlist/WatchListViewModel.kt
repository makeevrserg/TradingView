package com.dinmakeev.tradingview.presentation.watchlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.network.Repository
import com.dinmakeev.tradingview.network.WebSocketClient
import com.dinmakeev.tradingview.network.models.watchlists.WatchListItemModel
import com.dinmakeev.tradingview.network.models.watchlists.Watchlists
import com.dinmakeev.tradingview.network.models.watchlists.WatchlistsItem
import com.dinmakeev.tradingview.utils.MessageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WatchListViewModel(application: Application) : AndroidViewModel(application) {


    val app = (application as App)

    /**
     * Репозиторий для взаимодействия с сетью
     */
    protected val repository: Repository by lazy { app.repository }

    /**
     * Список котировок из текущего выбранного вотчлиста
     */
    private val _watchListItems = MutableLiveData<MutableList<WatchListItemModel>>()
    public val watchListItem: LiveData<MutableList<WatchListItemModel>>
        get() = _watchListItems
    val isLoading = MutableLiveData(true)
    val toolbarTitle = MutableLiveData<String>()

    /**
     * Список всех вотчлистов
     */
    private var watchLists: Watchlists? = null

    /**
     * Текущий выбранный вотчлист
     */
    private var currentWatchList: WatchlistsItem? = null
    private val TAG = "WatchListViewModel"


    /**
     * Получаем список тайтлов загруженных вотчлистов чтобы пользователь мог выбрать конкретный
     */
    fun getWatchListTitles(): List<String>? {
        if (watchLists == null || watchLists!!.isEmpty()) {
            App._notifyMessage.value = MessageHandler("Нет доступных списков")
            return null
        }
        return watchLists?.map { it.name }
    }

    /**
     * Загружаем доступные вотчлисты
     *
     * Чтобы предотвратить ошибки связанные с подключением к серверу - просто добавляем общий Exception
     */
    private fun loadWatchLists() {
        Log.d(TAG, "loadWatchLists: Loading")
        viewModelScope.launch(Dispatchers.IO) {
            watchLists = repository.fetchWatchListsList()
            onWatchListSelected(0)
            isLoading.postValue(false)
        }
    }

    /**
     * Наш View сообщает нам о том, что пользователь выбрал конкретную позицию в списке вотчлистов
     */
    fun onWatchListSelected(position: Int) {
        currentWatchList = watchLists?.getOrNull(position)
        currentWatchList?.let { watchListItem ->
            toolbarTitle.postValue(watchListItem.name)
            val list = watchListItem.symbols.filterIndexed { i, symbol -> !app.DataManager().isSymbolRemoved(watchListItem.name,symbol,i) }.map { WatchListItemModel(
                WatchListItemModel.Data(symbol = it)) }.toMutableList()
            _watchListItems.postValue(list)
        }
    }



    fun onSwiped(adapterPosition: Int) {
        val watchListItem = _watchListItems.value?.elementAt(adapterPosition)
        _watchListItems.value?.removeAt(adapterPosition)
        viewModelScope.launch(Dispatchers.IO) {
            WebSocketClient.closeCachedConnection(watchListItem?.symbol)
        }
        app.DataManager().removeSymbolFromList(currentWatchList?.name ?: return, watchListItem?.symbol ?: return)
    }


    init {
        loadWatchLists()
    }
}
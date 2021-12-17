package com.dinmakeev.tradingview.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.dinmakeev.tradingview.R
import com.dinmakeev.tradingview.network.Repository
import com.dinmakeev.tradingview.utils.MessageHandler

class App : Application() {
    lateinit var repository: Repository

    companion object {
        val _notifyMessage = MutableLiveData(MessageHandler<String>(null))
        val isLoading = MutableLiveData(true)
    }

    /**
     * Вообще такие штуки наверное лучше сделать через ROOM
     */
    inner class DataManager {
        private fun getSharedPref(): SharedPreferences? = try {
            getSharedPreferences(
                getString(R.string.preferences),
                Context.MODE_PRIVATE
            )
        } catch (e: Exception) {
            null
        }

        private fun getRemovePath(watchlist: String, symbol: String) = "${watchlist}_$symbol"
        fun isSymbolRemoved(watchlist: String, symbol: String, currentPosition: Int): Boolean {
            val path = getRemovePath(watchlist,symbol)
            return (getSharedPref() ?: return false).getInt(path, currentPosition)==-1
        }
        fun removeSymbolFromList(watchlist:String,symbol:String){
            val path = getRemovePath(watchlist,symbol)
            with((getSharedPref() ?: return).edit()) {
                putInt(path, -1)
                apply()
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        repository = Repository()
    }
}
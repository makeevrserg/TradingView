package com.dinmakeev.tradingview.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.dinmakeev.tradingview.R
import com.dinmakeev.tradingview.network.Repository
import com.dinmakeev.tradingview.utils.MessageHandler


@BindingAdapter("android:bitmap")
fun setImageViewResource(imageView: ImageView, bitmap: Bitmap?) {
    imageView.setImageBitmap(bitmap)

}
class App : Application() {
    lateinit var repository: Repository

    companion object {
        val _notifyMessage = MutableLiveData(MessageHandler<String>(null))
        val isLoading = MutableLiveData(true)
        private lateinit var assetManager:AssetManager
        fun getBitmapAsset(name:String):Bitmap? = assetManager.open("${name.lowercase()}.png").use(BitmapFactory::decodeStream)
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
        assetManager = assets
        repository = Repository()
    }
}
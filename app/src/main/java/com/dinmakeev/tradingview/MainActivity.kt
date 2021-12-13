package com.dinmakeev.tradingview

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.network.WebSocketClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)


        App._notifyMessage.observe(this,{
            Snackbar.make(nav_host_fragment.view?:return@observe,it.getContent()?:return@observe,Snackbar.LENGTH_LONG).show()
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, null)
    }

    /**
     * Тут мы мониторим веб-сокеты. При сворачивании/разворачивании - паузим/вохобновляем все соединения
     */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            WebSocketClient.resumeAll()
        }
    }
    override fun onPause() {
        super.onPause()
        lifecycleScope.launch(Dispatchers.IO) {
            WebSocketClient.pauseAll()
        }
    }
}
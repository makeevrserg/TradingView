package com.dinmakeev.tradingview.presentation.chart

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dinmakeev.tradingview.R
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.databinding.ChartFragmentBinding
import com.dinmakeev.tradingview.network.models.stocks.Data
import com.google.gson.Gson
import kotlinx.android.synthetic.main.chart_fragment.*

class ChartFragment : Fragment() {

    private val viewModel: ChartViewModel by lazy {
        ViewModelProvider(this).get(ChartViewModel::class.java)
    }

    var loadedWebView:WebView?=null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Binding
        val binding: ChartFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.chart_fragment, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

//        val arguments = ChartFragmentArgs.fromBundle(requireArguments())
//        viewModel.create(arguments.symbol)
//        // Для теста
        viewModel.create("AAPL")
        //Тут пытались сделать с помощью WebView, но эвент resize в javaScript не работал, так что не получалось нормально отрисовать
//        binding.webView.settings.javaScriptEnabled = true
//        binding.webView.settings.useWideViewPort = true
//        binding.webView.settings.loadWithOverviewMode= true
//        binding.webView.settings.builtInZoomControls = true
//        binding.webView.settings.displayZoomControls = false
//        binding.webView.settings.setSupportZoom(true)
//        binding.webView.isHorizontalScrollBarEnabled = true
//        binding.webView.isVerticalScrollBarEnabled = true
//        binding.webView.isScrollbarFadingEnabled = true
//        binding.webView.setBackgroundColor(Color.TRANSPARENT)
//        binding.webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
//        binding.webView.webViewClient = object: WebViewClient(){
//            override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
//                Log.d("Repository", "onScaleChanged: oldScale = ${oldScale} newScale=${newScale}")
//                super.onScaleChanged(view, oldScale, newScale)
//            }
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                loadedWebView = view
//                loadData()
//            }
//        }
//        binding.webView.loadUrl("file:///android_asset/plot/graph.html")
//        binding.webView.webChromeClient = object :WebChromeClient(){
//            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
//
//                Log.d("Repository", "onConsoleMessage: ${consoleMessage?.message()}")
//                return super.onConsoleMessage(consoleMessage)
//            }
//        }


        viewModel.toolbarTitle.observe(viewLifecycleOwner, {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it
        })
        viewModel.data.observe(viewLifecycleOwner,{
            binding.kChart.update(it)
        })
        viewModel.newData.observe(viewLifecycleOwner,{
            binding.kChart.addData(it)
        })
        return binding.root
    }

//    fun loadData(_data:List<Data>?=null){
//        val data = _data?:viewModel.data.value
//        if (data.isNullOrEmpty())
//            return
//
//        loadedWebView?.evaluateJavascript("_loadData(${Gson().toJson(data)});", ValueCallback {
//            Log.d("Repository", "onevaluateJavascriptMessage: ${it}")
//        })
//    }


}
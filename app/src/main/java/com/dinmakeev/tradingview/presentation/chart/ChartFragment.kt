package com.dinmakeev.tradingview.presentation.chart

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dinmakeev.tradingview.R
import com.dinmakeev.tradingview.databinding.ChartFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChartFragment : Fragment() {

    private val viewModel: ChartViewModel by lazy {
        ViewModelProvider(this).get(ChartViewModel::class.java)
    }

    var loadedWebView:WebView?=null
    lateinit var binding:ChartFragmentBinding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Binding
        binding =
            DataBindingUtil.inflate(inflater, R.layout.chart_fragment, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

//        val arguments = ChartFragmentArgs.fromBundle(requireArguments())
//        viewModel.create(arguments.symbol)
//        // Для теста
        viewModel.create("AAPL")
        ChartViewModel.offset.observe(viewLifecycleOwner,{
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.loadData(it)
            }
        })
        setHasOptionsMenu(true)


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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_go_last -> {

                binding.kChart.reset()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}
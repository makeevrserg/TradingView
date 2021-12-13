package com.dinmakeev.tradingview.presentation.watchlist

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dinmakeev.tradingview.R
import com.dinmakeev.tradingview.application.App
import com.dinmakeev.tradingview.databinding.WatchListFragmentBinding
import com.dinmakeev.tradingview.network.WebSocketClient
import kotlinx.coroutines.launch


class WatchListFragment : Fragment() {


    private val viewModel: WatchListViewModel by lazy {
        ViewModelProvider(this).get(WatchListViewModel::class.java)
    }
    val adapter by lazy {
        WatchListAdapter(requireActivity(), this::itemFactory)
    }

    override fun onResume() {
        WebSocketClient.resumeAll()
        super.onResume()
    }

    override fun onPause() {
        WebSocketClient.pauseAll()
        super.onPause()
    }

    /**
     * Указываем на lifecycleOwner целую активити потому что после onPause/onResume ссылка на lifecycleOwner меняется и данные перестают обновляться
     *
     * делаем кучу отдельных viewModel для каждого элемента потому что если сдеалть одно соединение, то эдементы recyclerView будут некрасиво обновляться
     */
    private fun itemFactory(trackItemModel: WatchListItemModel) = WatchListItemViewModel(
        requireActivity().application,
        trackItemModel
    ) { watchListItemModel ->
        findNavController().navigate(
            WatchListFragmentDirections.actionWatchListFragmentToChartFragment(
                watchListItemModel.symbol
            )
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Binding
        val binding: WatchListFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.watch_list_fragment, container, false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        //RecyclerView
        setHasOptionsMenu(true)
        val itemTouchHelper = ItemTouchHelper(dragCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.adapter = adapter
        //viewModel.observe
        viewModel.watchListItem.observe(viewLifecycleOwner, {
            it?.let {
                lifecycleScope.launch {
                    WebSocketClient.closeConnectionsIfNotInList(it.map { s -> s.symbol })
                }

                adapter.submitList(it)
            }
        })

        viewModel.toolbarTitle.observe(viewLifecycleOwner, {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it
        })
        viewModel.isLoading.observe(viewLifecycleOwner, {
            binding.pbLoading.visibility = if (it) View.VISIBLE else View.GONE
        })

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.watchlist_menu, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                adapter.filter.filter(p0 ?: return false)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                adapter.filter.filter(p0 ?: return false)
                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Выберите список")
                builder.setItems(
                    viewModel.getWatchListTitles()?.toTypedArray()
                        ?: return super.onOptionsItemSelected(item)
                ) { dialog, which ->
                    viewModel.onWatchListSelected(which)
                }

                val dialog = builder.create()
                dialog.show()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    var dragCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
        ItemTouchHelper.LEFT
    ) {
        /**
         * Перемещение не делали
         */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
//            val fromPosition = viewHolder.adapterPosition
//            val toPosition = target.adapterPosition
//            viewModel.onListReorganized(fromPosition,toPosition)
//            recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.onSwiped(viewHolder.adapterPosition)
            adapter.notifyItemRemoved(viewHolder.adapterPosition)
        }
    }


}
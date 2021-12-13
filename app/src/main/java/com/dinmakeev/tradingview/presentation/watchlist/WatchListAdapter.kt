package com.dinmakeev.tradingview.presentation.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dinmakeev.tradingview.databinding.WatchItemBinding


class WatchListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val factory: (WatchListItemModel) -> WatchListItemViewModel
) : ListAdapter<WatchListItemModel, WatchListAdapter.ViewHolder>(DIFF_CALLBACK),Filterable {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WatchListItemModel>() {
            override fun areItemsTheSame(
                oldItem: WatchListItemModel,
                newItem: WatchListItemModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: WatchListItemModel,
                newItem: WatchListItemModel
            ): Boolean {
                return oldItem.symbol == newItem.symbol
            }
        }
    }


    inner class ViewHolder (private val binding: WatchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WatchListItemModel) {
            binding.viewModel = factory(item)
            binding.lifecycleOwner = this@WatchListAdapter.lifecycleOwner
            binding.executePendingBindings()
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = WatchItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    var refList = mutableListOf<WatchListItemModel>()
    var constList = listOf<WatchListItemModel>()
    override fun submitList(list: MutableList<WatchListItemModel>?) {
        super.submitList(list)
        refList = list ?: return
        constList = refList.toList()
    }

    override fun getFilter(): Filter =
        object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
                refList.clear()
                refList.addAll(filterResults.values as MutableList<WatchListItemModel>)
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                return FilterResults().apply {
                    values = constList.filter { it ->
                        it.symbol.contains(charSequence ?: "")  || (it.data.description?.contains(charSequence ?: "")==true)
                    }
                }
            }
        }
}

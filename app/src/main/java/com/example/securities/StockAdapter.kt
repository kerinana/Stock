package com.example.securities

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.securities.databinding.ItemStockBinding

class StockAdapter(private val onItemClick: (StockUiData) -> Unit) : ListAdapter<StockUiData, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

     class StockViewHolder(
        private val binding: ItemStockBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: StockUiData, onItemClick: (StockUiData) -> Unit) {
            val context = binding.root.context
            // 取得解析後的 Color Int (注意：此處型態為 Int)
            val stockColor = ContextCompat.getColor(context, item.changeColorRes)
            binding.apply {
                tvCode.text = "(${item.code})"
                tvName.text = "(${item.name})"

                tvOpenPrice.text = "(${item.openPrice})"
                tvClosingPrice.text = "(${item.closingPrice})"
                tvHighestPrice.text = "(${item.highestPrice})"
                tvLowestPrice.text = "(${item.lowestPrice})"
                tvChange.text = "(${item.change})"
                tvMonthlyAverage.text = "(${item.monthlyAveragePrice})"

                tvTransactionCount.text = "(${item.transactionCount})"
                tvTradeVolume.text = "(${item.volume})"
                tvTradeValue.text = "(${item.tradeValue})"

                //  套用 changeColorRes 至需要顯示顏色的欄位
                tvOpenPrice.setTextColor(stockColor)
                tvClosingPrice.setTextColor(stockColor)
                tvChange.setTextColor(stockColor)

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    //ListAdapter
    private class StockDiffCallback : DiffUtil.ItemCallback<StockUiData>() {
        override fun areItemsTheSame(oldItem: StockUiData, newItem: StockUiData): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: StockUiData, newItem: StockUiData): Boolean {
            return oldItem == newItem
        }
    }
}
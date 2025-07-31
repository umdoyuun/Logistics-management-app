package com.example.logisticsmanagement.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.logisticsmanagement.databinding.ItemTopDistributorBinding
import com.example.logisticsmanagement.data.model.DistributorSummaryData

class TopDistributorAdapter : ListAdapter<DistributorSummaryData, TopDistributorAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopDistributorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class ViewHolder(private val binding: ItemTopDistributorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(distributorData: DistributorSummaryData, rank: Int) {
            binding.apply {
                tvRank.text = "${rank}위"
                tvDistributorName.text = distributorData.name
                tvPallets.text = "${distributorData.totalPallets} 파렛트"
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DistributorSummaryData>() {
            override fun areItemsTheSame(oldItem: DistributorSummaryData, newItem: DistributorSummaryData): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: DistributorSummaryData, newItem: DistributorSummaryData): Boolean {
                return oldItem == newItem
            }
        }
    }
}
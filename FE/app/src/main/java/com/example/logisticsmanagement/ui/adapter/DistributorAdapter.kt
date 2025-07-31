package com.example.logisticsmanagement.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.logisticsmanagement.databinding.ItemDistributorBinding
import com.example.logisticsmanagement.data.model.Distributor

class DistributorAdapter(
    private val onEditClick: (Distributor) -> Unit,
    private val onDeleteClick: (Distributor) -> Unit
) : ListAdapter<Distributor, DistributorAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDistributorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDistributorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(distributor: Distributor) {
            binding.apply {
                tvName.text = distributor.name
                tvCode.text = "코드: ${distributor.code}"
                tvCategory.text = "분류: ${distributor.mainCategory}"
                tvContact.text = "담당자: ${distributor.contactPerson}"
                tvPhone.text = "연락처: ${distributor.phone}"

                btnEdit.setOnClickListener { onEditClick(distributor) }
                btnDelete.setOnClickListener { onDeleteClick(distributor) }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Distributor>() {
            override fun areItemsTheSame(oldItem: Distributor, newItem: Distributor): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Distributor, newItem: Distributor): Boolean {
                return oldItem == newItem
            }
        }
    }
}
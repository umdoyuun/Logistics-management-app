package com.example.logisticsmanagement.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.logisticsmanagement.databinding.ItemWorkRecordBinding
import com.example.logisticsmanagement.data.model.WorkRecord
import com.example.logisticsmanagement.utils.getFormattedDateTime

class WorkRecordAdapter(
    private val onEditClick: (WorkRecord) -> Unit,
    private val onDeleteClick: (WorkRecord) -> Unit
) : ListAdapter<WorkRecord, WorkRecordAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemWorkRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workRecord: WorkRecord) {
            binding.apply {
                tvDistributor.text = workRecord.distributorName
                tvPallets.text = "${workRecord.totalPallets} 파렛트"
                tvDateTime.text = workRecord.getFormattedDateTime()
                tvCreatedBy.text = "작성자: ${workRecord.createdByName}"
                tvNotes.text = if (workRecord.notes.isNotEmpty()) workRecord.notes else "비고 없음"

                btnEdit.setOnClickListener { onEditClick(workRecord) }
                btnDelete.setOnClickListener { onDeleteClick(workRecord) }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<WorkRecord>() {
            override fun areItemsTheSame(oldItem: WorkRecord, newItem: WorkRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: WorkRecord, newItem: WorkRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}
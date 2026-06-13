package com.fittrack.ui.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fittrack.data.model.DiaryEntryResponse
import com.fittrack.databinding.ItemDiaryBinding

class DiaryAdapter(
    private val onItemClick: (DiaryEntryResponse) -> Unit
) : ListAdapter<DiaryEntryResponse, DiaryAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemDiaryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(entry: DiaryEntryResponse) {
            b.tvProductName.text = entry.productName
            b.tvQuantity.text    = "${entry.quantityG.toInt()} g"
            b.tvKcal.text        = "${entry.kcal.toInt()} kcal"
            b.tvMacros.text      = "B:${entry.proteinG.toInt()} T:${entry.fatG.toInt()} W:${entry.carbsG.toInt()}"
            b.root.setOnClickListener { onItemClick(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DiaryEntryResponse>() {
            override fun areItemsTheSame(a: DiaryEntryResponse, b: DiaryEntryResponse) = a.id == b.id
            override fun areContentsTheSame(a: DiaryEntryResponse, b: DiaryEntryResponse) = a == b
        }
    }
}

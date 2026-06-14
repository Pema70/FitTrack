package com.fittrack.ui.diary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fittrack.data.model.ProductResponse
import com.fittrack.databinding.ItemProductBinding

class ProductAdapter(
    private val onPick: (ProductResponse) -> Unit
) : ListAdapter<ProductResponse, ProductAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: ProductResponse) {
            b.tvProductName.text = p.name
            b.tvProductKcal.text = "${p.kcalPer100g.toInt()} kcal / 100g"
            b.tvProductMacros.text = "B: ${p.proteinPer100g}g  T: ${p.fatPer100g}g  W: ${p.carbsPer100g}g"
            b.root.setOnClickListener { onPick(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ProductResponse>() {
            override fun areItemsTheSame(a: ProductResponse, b: ProductResponse) = a.id == b.id
            override fun areContentsTheSame(a: ProductResponse, b: ProductResponse) = a == b
        }
    }
}
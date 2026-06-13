package com.fittrack.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fittrack.R
import com.fittrack.data.model.RecipeResponse
import com.fittrack.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onItemClick: (RecipeResponse) -> Unit,
    private val onFavoriteClick: (RecipeResponse) -> Unit,
    private val onDeleteClick: (RecipeResponse) -> Unit
) : ListAdapter<RecipeResponse, RecipeAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemRecipeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: RecipeResponse) {
            b.tvTitle.text    = r.title
            b.tvKcal.text     = "${r.kcalPerServing.toInt()} kcal"
            b.tvTags.text     = r.tags.take(3).joinToString(" · ")
            b.tvTime.text     = r.prepTimeMin?.let { "⏱ $it min" } ?: ""

            Glide.with(b.root)
                .load(r.imageUrl)
                .placeholder(R.drawable.ic_recipe_placeholder)
                .centerCrop()
                .into(b.ivThumbnail)

            b.btnFavorite.setIconResource(
                if (r.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            // Przycisk usuń widoczny tylko jeśli przepis należy do użytkownika
            b.btnDelete.isVisible = r.isOwner

            b.root.setOnClickListener      { onItemClick(r) }
            b.btnFavorite.setOnClickListener { onFavoriteClick(r) }
            b.btnDelete.setOnClickListener   { onDeleteClick(r) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<RecipeResponse>() {
            override fun areItemsTheSame(a: RecipeResponse, b: RecipeResponse) = a.id == b.id
            override fun areContentsTheSame(a: RecipeResponse, b: RecipeResponse) = a == b
        }
    }
}

package com.fittrack.ui.recipes

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fittrack.R
import com.fittrack.databinding.FragmentRecipeDetailBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class RecipeDetailFragment : Fragment(R.layout.fragment_recipe_detail) {

    private val args: RecipeDetailFragmentArgs by navArgs()
    private val vm: RecipeViewModel by viewModels()
    private var _b: FragmentRecipeDetailBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentRecipeDetailBinding.bind(view)

        val r = args.recipe

        b.tvTitle.text    = r.title
        b.tvDescription.text = r.description ?: ""

        val kcalStr = formatNutrition(r.kcalPerServing ?: 0.0)
        val pStr = formatNutrition(r.proteinG ?: 0.0)
        val fStr = formatNutrition(r.fatG ?: 0.0)
        val cStr = formatNutrition(r.carbsG ?: 0.0)

        b.tvKcal.text     = "$kcalStr kcal / porcja"
        b.tvMacros.text   = "B: ${pStr}g  •  T: ${fStr}g  •  W: ${cStr}g"
        b.tvTime.text     = r.prepTimeMin?.let { "⏱ $it min" } ?: ""
        b.tvServings.text = "Porcji: ${r.servings ?: 1}"
        b.tvTags.text     = r.tags?.joinToString(" · ") ?: ""

        Glide.with(this)
            .load(r.imageUrl)
            .placeholder(R.drawable.ic_recipe_placeholder)
            .into(b.ivHero)

        updateFavoriteIcon(r.isFavorite == true)
        b.btnFavorite.setOnClickListener {
            vm.toggleFavorite(r)
            val nowFav = !(r.isFavorite == true)
            updateFavoriteIcon(nowFav)
            val msg = if (nowFav) "Dodano do ulubionych" else "Usunięto z ulubionych"
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
        }

        b.btnDelete.isVisible = r.isOwner == true
        b.btnDelete.setOnClickListener {
            vm.deleteRecipe(r.id)
            findNavController().popBackStack()
        }
    }

    private fun formatNutrition(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    private fun updateFavoriteIcon(isFav: Boolean) {
        b.btnFavorite.setIconResource(
            if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
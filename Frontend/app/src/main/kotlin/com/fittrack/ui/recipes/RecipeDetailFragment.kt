package com.fittrack.ui.recipes

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.fittrack.R
import com.fittrack.databinding.FragmentRecipeDetailBinding
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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
        b.tvKcal.text     = "${r.kcalPerServing.toInt()} kcal / porcja"
        b.tvMacros.text   = "B: ${r.proteinG.toInt()}g  •  T: ${r.fatG.toInt()}g  •  W: ${r.carbsG.toInt()}g"
        b.tvTime.text     = r.prepTimeMin?.let { "⏱ $it min" } ?: ""
        b.tvServings.text = "Porcji: ${r.servings}"
        b.tvTags.text     = r.tags.joinToString(" · ")

        Glide.with(this)
            .load(r.imageUrl)
            .placeholder(R.drawable.ic_recipe_placeholder)
            .into(b.ivHero)

        // Ulubione
        updateFavoriteIcon(r.isFavorite)
        b.btnFavorite.setOnClickListener {
            vm.toggleFavorite(r)
            val nowFav = !r.isFavorite
            updateFavoriteIcon(nowFav)
            val msg = if (nowFav) "Dodano do ulubionych" else "Usunięto z ulubionych"
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
        }

        // Usuń (tylko autor)
        b.btnDelete.isVisible = true   // widoczność kontroluje backend (403 jeśli nie autor)
        b.btnDelete.setOnClickListener {
            vm.deleteRecipe(r.id)
            findNavController().popBackStack()
        }
    }

    private fun updateFavoriteIcon(isFav: Boolean) {
        b.btnFavorite.setIconResource(
            if (isFav) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

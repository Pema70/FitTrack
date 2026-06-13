package com.fittrack.ui.recipes

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fittrack.R
import com.fittrack.databinding.FragmentRecipesBinding
import com.fittrack.util.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipesFragment : Fragment(R.layout.fragment_recipes) {

    private val vm: RecipeViewModel by viewModels()
    private var _b: FragmentRecipesBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: RecipeAdapter

    private val tags = listOf("wege", "bez_laktozy", "bezglutenowe", "< 400 kcal", "wysokobiałkowe")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentRecipesBinding.bind(view)

        adapter = RecipeAdapter(
            onItemClick    = { recipe ->
                val action = RecipesFragmentDirections.mainToRecipeDetail(recipe)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe -> vm.toggleFavorite(recipe) },
            onDeleteClick   = { recipe ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Usuń przepis")
                    .setMessage("Czy na pewno chcesz usunąć \"${recipe.title}\"?")
                    .setPositiveButton("Usuń") { _, _ -> vm.deleteRecipe(recipe.id) }
                    .setNegativeButton("Anuluj", null)
                    .show()
            }
        )
        b.rvRecipes.adapter = adapter
        b.rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        // Zakładki: Wszystkie / Moje / Ulubione
        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> vm.loadAll()
                    1 -> vm.loadMine()
                    2 -> vm.loadFavorites()
                }
                b.chipGroupFilters.isVisible = tab.position == 0
                b.etSearch.isVisible         = tab.position == 0
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Filtry tagami
        tags.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag
                isCheckable = true
                setOnCheckedChangeListener { _, checked ->
                    vm.loadAll(tag = if (checked) tag else null)
                }
            }
            b.chipGroupFilters.addView(chip)
        }

        // Wyszukiwarka
        b.etSearch.addTextChangedListener { editable ->
            val q = editable.toString()
            if (q.length >= 3 || q.isEmpty()) vm.loadAll(q)
        }

        lifecycleScope.launch {
            vm.recipes.collect { state ->
                b.progressBar.isVisible = state is Resource.Loading
                if (state is Resource.Success) adapter.submitList(state.data)
            }
        }

        lifecycleScope.launch {
            vm.opState.collect { state ->
                if (state is Resource.Error)
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

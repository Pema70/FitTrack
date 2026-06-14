package com.fittrack.ui.diary

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fittrack.R
import com.fittrack.data.model.DiaryEntryRequest
import com.fittrack.data.model.ProductResponse
import com.fittrack.databinding.FragmentDiaryBinding
import com.fittrack.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DiaryFragment : Fragment(R.layout.fragment_diary) {

    private val vm: DiaryViewModel by viewModels()
    private val productVm: ProductViewModel by viewModels()
    private var _b: FragmentDiaryBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: DiaryAdapter
    private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentDiaryBinding.bind(view)

        setupRecyclerView()
        setupSwipeToDelete()
        setupDateNavigation()
        setupFab()

        vm.loadDay(LocalDate.now())

        lifecycleScope.launch {
            vm.entries.collect { state ->
                val bRef = _b ?: return@collect
                bRef.progressBar.isVisible = state is Resource.Loading
                bRef.swipeRefresh.isRefreshing = false
                when (state) {
                    is Resource.Success -> adapter.submitList(state.data)
                    is Resource.Error   -> Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            vm.summary.collect { state ->
                if (state is Resource.Success) {
                    val s = state.data
                    b.tvKcalConsumed.text  = "${s.kcalConsumed.toInt()} kcal"
                    b.tvKcalRemaining.text = "${s.kcalRemaining.toInt()} pozostalo"
                    b.progressKcal.max      = s.kcalGoal
                    b.progressKcal.progress = s.kcalConsumed.toInt()
                    b.tvDate.text = vm.currentDate.format(dateFmt)
                }
            }
        }

        b.swipeRefresh.setOnRefreshListener { vm.loadDay(vm.currentDate) }
    }

    private fun setupRecyclerView() {
        adapter = DiaryAdapter { entry ->
            val input = EditText(requireContext()).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(entry.quantityG.toString())
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Zmien ilosc (g)")
                .setView(input)
                .setPositiveButton("Zapisz") { _, _ ->
                    val q = input.text.toString().toDoubleOrNull()
                    if (q != null && q > 0) vm.updateEntryQuantity(entry.id, q)
                    else Snackbar.make(b.root, "Podaj prawidlowa wartosc", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }
        b.rvDiary.adapter = adapter
        b.rvDiary.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSwipeToDelete() {
        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val entry = adapter.currentList[viewHolder.adapterPosition]
                vm.deleteEntry(entry.id)
                Snackbar.make(b.root, "Wpis usuniety", Snackbar.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(b.rvDiary)
    }

    private fun setupDateNavigation() {
        b.btnPrevDay.setOnClickListener { vm.previousDay() }
        b.btnNextDay.setOnClickListener { vm.nextDay() }
    }

    private fun setupFab() {
        b.fab.setOnClickListener { showProductSearchDialog() }
    }

    private fun showProductSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_product_search, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchViewProducts)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvProducts)

        var searchDialog: AlertDialog? = null

        val productAdapter = ProductAdapter { product ->
            searchDialog?.dismiss()
            showAddEntryDialog(product)
        }
        recyclerView.adapter = productAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { productVm.search(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if ((newText?.length ?: 0) >= 2) productVm.search(newText!!)
                return true
            }
        })

        searchDialog = AlertDialog.Builder(requireContext())
            .setTitle("Szukaj produktu")
            .setView(dialogView)
            .setNegativeButton("Anuluj", null)
            .show()

        lifecycleScope.launch {
            productVm.products.collect { state ->
                if (state is Resource.Success) productAdapter.submitList(state.data)
            }
        }
    }

    private fun showAddEntryDialog(product: ProductResponse) {
        val mealTypes  = arrayOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
        val mealLabels = arrayOf("Sniadanie", "Obiad", "Kolacja", "Przekaska")
        var selectedMeal = 0

        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Ilosc (g)"
            setText("100")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(product.name)
            .setSingleChoiceItems(mealLabels, 0) { _, which -> selectedMeal = which }
            .setView(input)
            .setPositiveButton("Dodaj") { _, _ ->
                val q = input.text.toString().toDoubleOrNull() ?: 100.0
                vm.addEntry(DiaryEntryRequest(
                    productId = product.id,
                    quantityG = q,
                    mealType  = mealTypes[selectedMeal],
                    entryDate = vm.currentDate.toString()
                ))
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
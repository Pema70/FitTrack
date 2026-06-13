package com.fittrack.ui.diary

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fittrack.R
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
                    is Resource.Error   -> {
                        val viewRef = view ?: return@collect
                        Snackbar.make(viewRef, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            vm.summary.collect { state ->
                if (state is Resource.Success) {
                    val s = state.data
                    b.tvKcalConsumed.text  = "${s.kcalConsumed.toInt()} kcal"
                    b.tvKcalRemaining.text = "${s.kcalRemaining.toInt()} pozostało"
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
            // Kliknięcie wpisu → edycja gramów
            val input = EditText(requireContext()).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(entry.quantityG.toString())
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Zmień ilość (g)")
                .setView(input)
                .setPositiveButton("Zapisz") { _, _ ->
                    val q = input.text.toString().toDoubleOrNull()
                    if (q != null && q > 0) vm.updateEntryQuantity(entry.id, q)
                    else Snackbar.make(b.root, "Podaj prawidłową wartość", Snackbar.LENGTH_SHORT).show()
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
                Snackbar.make(b.root, "Wpis usunięty", Snackbar.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(b.rvDiary)
    }

    private fun setupDateNavigation() {
        b.btnPrevDay.setOnClickListener { vm.previousDay() }
        b.btnNextDay.setOnClickListener { vm.nextDay() }
        b.tvDate.setOnClickListener {
            // TODO: opcjonalnie otwórz DatePickerDialog
        }
    }

    private fun setupFab() {
        b.fab.setOnClickListener {
            findNavController().navigate(R.id.main_to_food_photo)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

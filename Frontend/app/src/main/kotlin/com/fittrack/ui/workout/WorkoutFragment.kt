package com.fittrack.ui.workout

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fittrack.R
import com.fittrack.data.model.WorkoutRequest
import com.fittrack.data.model.WorkoutResponse
import com.fittrack.databinding.FragmentWorkoutBinding
import com.fittrack.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class WorkoutFragment : Fragment(R.layout.fragment_workout) {

    private val vm: WorkoutViewModel by viewModels()
    private var _b: FragmentWorkoutBinding? = null
    private val b get() = _b!!
    private lateinit var adapter: WorkoutAdapter

    private val activityTypes = arrayOf("GYM", "RUNNING", "CYCLING", "SWIMMING", "WALKING", "YOGA", "OTHER")
    private val activityLabels = arrayOf("Siłownia", "Bieganie", "Rower", "Pływanie", "Spacer", "Joga", "Inne")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentWorkoutBinding.bind(view)

        setupRecyclerView()
        vm.loadToday()

        b.btnAddWorkout.setOnClickListener { showWorkoutDialog(null) }

        lifecycleScope.launch {
            vm.workouts.collect { state ->
                b.progressBar.isVisible = state is Resource.Loading
                when (state) {
                    is Resource.Success -> {
                        adapter.submitList(state.data)
                        val totalBurned = state.data.sumOf { it.kcalBurned }
                        b.tvKcalBurned.text = "Spalone dziś: $totalBurned kcal"
                    }
                    is Resource.Error -> Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            vm.opState.collect { state ->
                if (state is Resource.Error)
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter(
            onEdit = { workout -> showWorkoutDialog(workout) },
            onDelete = { workout ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Usuń trening")
                    .setMessage("Czy na pewno chcesz usunąć ten wpis?")
                    .setPositiveButton("Usuń") { _, _ -> vm.deleteWorkout(workout.id) }
                    .setNegativeButton("Anuluj", null)
                    .show()
            }
        )
        b.rvWorkouts.adapter = adapter
        b.rvWorkouts.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showWorkoutDialog(existing: WorkoutResponse?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_workout, null)
        val spinner     = dialogView.findViewById<Spinner>(R.id.spinnerActivity)
        val etDuration  = dialogView.findViewById<EditText>(R.id.etDuration)
        val etKcal      = dialogView.findViewById<EditText>(R.id.etKcalManual)
        val switchManual = dialogView.findViewById<SwitchCompat>(R.id.switchManualKcal)

        spinner.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, activityLabels).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Wypełnij istniejące dane przy edycji
        existing?.let {
            val idx = activityTypes.indexOf(it.activityType).coerceAtLeast(0)
            spinner.setSelection(idx)
            etDuration.setText(it.durationMin.toString())
            etKcal.setText(it.kcalBurned.toString())
            switchManual.isChecked = true
        }

        switchManual.setOnCheckedChangeListener { _, checked ->
            etKcal.isVisible = checked
        }
        etKcal.isVisible = switchManual.isChecked

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Dodaj trening" else "Edytuj trening")
            .setView(dialogView)
            .setPositiveButton("Zapisz") { _, _ ->
                val type     = activityTypes[spinner.selectedItemPosition]
                val duration = etDuration.text.toString().toIntOrNull() ?: 0
                val kcal     = if (switchManual.isChecked)
                    etKcal.text.toString().toIntOrNull()
                else null
                if (duration <= 0) {
                    Snackbar.make(b.root, "Podaj czas trwania", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val req = WorkoutRequest(
                    activityDate = vm.currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    activityType = type,
                    durationMin  = duration,
                    kcalBurned   = kcal,
                    notes        = null
                )
                if (existing == null) vm.logWorkout(req)
                else vm.updateWorkout(existing.id, req)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

package com.fittrack.ui.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fittrack.data.model.WorkoutResponse
import com.fittrack.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private val onEdit: (WorkoutResponse) -> Unit,
    private val onDelete: (WorkoutResponse) -> Unit
) : ListAdapter<WorkoutResponse, WorkoutAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemWorkoutBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(w: WorkoutResponse) {
            b.tvActivityType.text = w.activityType
            b.tvDuration.text     = "${w.durationMin} min"
            b.tvKcalBurned.text         = "${w.kcalBurned} kcal"
            b.btnEdit.setOnClickListener   { onEdit(w) }
            b.btnDelete.setOnClickListener { onDelete(w) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<WorkoutResponse>() {
            override fun areItemsTheSame(a: WorkoutResponse, b: WorkoutResponse) = a.id == b.id
            override fun areContentsTheSame(a: WorkoutResponse, b: WorkoutResponse) = a == b
        }
    }
}

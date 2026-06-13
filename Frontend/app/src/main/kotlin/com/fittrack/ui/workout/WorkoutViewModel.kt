package com.fittrack.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.model.WorkoutRequest
import com.fittrack.data.model.WorkoutResponse
import com.fittrack.data.repository.WorkoutRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repo: WorkoutRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<Resource<List<WorkoutResponse>>>(Resource.Loading)
    val workouts: StateFlow<Resource<List<WorkoutResponse>>> = _workouts

    private val _opState = MutableStateFlow<Resource<Unit>?>(null)
    val opState: StateFlow<Resource<Unit>?> = _opState

    var currentDate: LocalDate = LocalDate.now()
        private set

    fun loadToday() = load(LocalDate.now())

    fun load(date: LocalDate) = viewModelScope.launch {
        currentDate = date
        _workouts.value = Resource.Loading
        _workouts.value = repo.getForDate(date.toString())
    }

    fun logWorkout(req: WorkoutRequest) = viewModelScope.launch {
        _opState.value = when (val r = repo.log(req)) {
            is Resource.Success -> { loadToday(); Resource.Success(Unit) }
            is Resource.Error   -> Resource.Error(r.message)
            else -> null
        }
    }

    fun updateWorkout(id: Long, req: WorkoutRequest) = viewModelScope.launch {
        _opState.value = when (val r = repo.update(id, req)) {
            is Resource.Success -> { loadToday(); Resource.Success(Unit) }
            is Resource.Error   -> Resource.Error(r.message)
            else -> null
        }
    }

    fun deleteWorkout(id: Long) = viewModelScope.launch {
        _opState.value = when (val r = repo.delete(id)) {
            is Resource.Success -> { loadToday(); Resource.Success(Unit) }
            is Resource.Error   -> Resource.Error(r.message)
            else -> null
        }
    }
}
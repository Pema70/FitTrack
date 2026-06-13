package com.fittrack.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.model.*
import com.fittrack.data.repository.DiaryRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repo: DiaryRepository
) : ViewModel() {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val _entries = MutableStateFlow<Resource<List<DiaryEntryResponse>>>(Resource.Loading)
    val entries: StateFlow<Resource<List<DiaryEntryResponse>>> = _entries

    private val _summary = MutableStateFlow<Resource<DailySummaryResponse>>(Resource.Loading)
    val summary: StateFlow<Resource<DailySummaryResponse>> = _summary

    var currentDate: LocalDate = LocalDate.now()
        private set

    fun loadDay(date: LocalDate) {
        currentDate = date
        val dateStr = date.format(fmt)
        viewModelScope.launch {
            _entries.value = Resource.Loading
            _entries.value = repo.getEntries(dateStr)
        }
        viewModelScope.launch {
            _summary.value = Resource.Loading
            _summary.value = repo.getSummary(dateStr)
        }
    }

    fun previousDay() = loadDay(currentDate.minusDays(1))
    fun nextDay()     = loadDay(currentDate.plusDays(1))

    fun addEntry(req: DiaryEntryRequest) = viewModelScope.launch {
        val result = repo.addEntry(req)
        if (result is Resource.Success) {
            loadDay(currentDate)
        } else if (result is Resource.Error) {
            _entries.value = Resource.Error(result.message)
        }
    }

    fun deleteEntry(id: Long) = viewModelScope.launch {
        repo.deleteEntry(id)
        loadDay(currentDate)
    }

    fun updateEntryQuantity(id: Long, quantityG: Double) = viewModelScope.launch {
        val result = repo.updateEntry(id, quantityG)
        if (result is Resource.Success) loadDay(currentDate)
    }
}

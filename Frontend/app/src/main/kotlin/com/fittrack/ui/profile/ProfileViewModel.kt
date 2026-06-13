package com.fittrack.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.model.ProfileResponse
import com.fittrack.data.model.ProfileUpdateRequest
import com.fittrack.data.repository.AuthRepository
import com.fittrack.data.repository.ProfileRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<ProfileResponse>>(Resource.Loading)
    val profile: StateFlow<Resource<ProfileResponse>> = _profile

    private val _saveState = MutableStateFlow<Resource<Unit>?>(null)
    val saveState: StateFlow<Resource<Unit>?> = _saveState

    private val _passwordState = MutableStateFlow<Resource<Unit>?>(null)
    val passwordState: StateFlow<Resource<Unit>?> = _passwordState

    init { loadProfile() }

    fun loadProfile() = viewModelScope.launch {
        _profile.value = Resource.Loading
        _profile.value = profileRepo.get()
    }

    fun updateProfile(req: ProfileUpdateRequest) = viewModelScope.launch {
        _saveState.value = Resource.Loading
        _saveState.value = when (val r = profileRepo.update(req)) {
            is Resource.Success<*> -> { loadProfile(); Resource.Success(Unit) }
            is Resource.Error   -> Resource.Error(r.message)
            else -> null
        }
    }

    fun changePassword(old: String, new: String) = viewModelScope.launch {
        _passwordState.value = Resource.Loading
        _passwordState.value = when (val r = authRepo.changePassword(old, new)) {
            is Resource.Success<*> -> Resource.Success(Unit)
            is Resource.Error   -> Resource.Error(r.message)
            else -> null
        }
    }

    fun logout() = viewModelScope.launch { authRepo.logout() }
}
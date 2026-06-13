package com.fittrack.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.repository.AuthRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Unit>?>(null)
    val authState: StateFlow<Resource<Unit>?> = _authState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Podaj email i hasło")
            return@launch
        }
        _authState.value = Resource.Loading
        val result = repo.login(email, password)
        _authState.value = when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error   -> Resource.Error(result.message)
            Resource.Loading    -> Resource.Loading
        }
    }

    fun register(email: String, password: String) = viewModelScope.launch {
        _authState.value = Resource.Loading
        val result = repo.register(email, password)
        _authState.value = when (result) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error   -> Resource.Error(result.message)
            Resource.Loading    -> Resource.Loading
        }
    }

    fun logout() = viewModelScope.launch {
        repo.logout()
        _isLoggedIn.value = false
    }
}
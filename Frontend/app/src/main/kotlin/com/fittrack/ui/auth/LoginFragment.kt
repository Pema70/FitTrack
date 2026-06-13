package com.fittrack.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fittrack.R
import com.fittrack.databinding.FragmentLoginBinding
import com.fittrack.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val vm: AuthViewModel by viewModels()
    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentLoginBinding.bind(view)

        b.btnLogin.setOnClickListener {
            val email    = b.etEmail.text.toString().trim()
            val password = b.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Snackbar.make(view, "Wypełnij wszystkie pola", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.login(email, password)
        }

        b.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.to_register)
        }

        lifecycleScope.launch {
            vm.authState.collect { state ->
                b.progressBar.isVisible = state is Resource.Loading
                when (state) {
                    is Resource.Success ->
                        findNavController().navigate(R.id.login_to_main)
                    is Resource.Error ->
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

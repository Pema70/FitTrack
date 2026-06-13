package com.fittrack.ui.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fittrack.R
import com.fittrack.databinding.FragmentRegisterBinding
import com.fittrack.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val vm: AuthViewModel by viewModels()
    private var _b: FragmentRegisterBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentRegisterBinding.bind(view)

        b.btnRegister.setOnClickListener {
            val email = b.etEmail.text.toString().trim()
            val pass  = b.etPassword.text.toString()
            val pass2 = b.etPasswordConfirm.text.toString()

            if (email.isBlank() || pass.isBlank()) {
                Snackbar.make(view, "Wypełnij wszystkie pola", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                b.etEmail.error = "Podaj poprawny adres e-mail"
                return@setOnClickListener
            }
            if (pass.length < 8 || !pass.any { it.isDigit() }) {
                b.etPassword.error = "Min. 8 znaków i jedna cyfra"
                return@setOnClickListener
            }
            if (pass != pass2) {
                b.etPasswordConfirm.error = "Hasła nie są zgodne"
                return@setOnClickListener
            }
            vm.register(email, pass)
        }

        lifecycleScope.launch {
            vm.authState.collect { state ->
                b.progressBar.isVisible = state is Resource.Loading
                when (state) {
                    is Resource.Success ->
                        findNavController().navigate(R.id.register_to_main)
                    is Resource.Error ->
                        Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

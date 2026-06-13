package com.fittrack.ui.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fittrack.R
import com.fittrack.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private var _b: FragmentMainBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentMainBinding.bind(view)

        val navHost = childFragmentManager
            .findFragmentById(R.id.innerNavHostFragment) as NavHostFragment
        val navController = navHost.navController

        // Bottom Navigation Bar z 4 zakładkami: Dziennik, Przepisy, Trening, Profil
        b.bottomNav.setupWithNavController(navController)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

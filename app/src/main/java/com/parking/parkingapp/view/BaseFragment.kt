package com.parking.parkingapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.parking.parkingapp.R
import com.parking.parkingapp.view.drawer_menu.DrawerMenuFragment

abstract class BaseFragment<VB: ViewBinding>: Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initActions()
        intiData()
        obverseFromViewModel(viewLifecycleOwner.lifecycleScope)
    }

    abstract fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VB

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun getDrawerMenu(): DrawerMenuFragment? {
        return ((activity as? MainActivity)?.supportFragmentManager
            ?.findFragmentById(R.id.drawer_menu) as? NavHostFragment)
            ?.childFragmentManager
            ?.primaryNavigationFragment as? DrawerMenuFragment
    }

    abstract fun initViews()
    abstract fun initActions()
    abstract fun intiData()
    abstract fun obverseFromViewModel(scope: LifecycleCoroutineScope)
}
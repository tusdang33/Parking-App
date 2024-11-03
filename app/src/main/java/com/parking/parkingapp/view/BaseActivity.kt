package com.parking.parkingapp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB: ViewBinding>: AppCompatActivity() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
    }

    abstract fun inflateBinding(): VB

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
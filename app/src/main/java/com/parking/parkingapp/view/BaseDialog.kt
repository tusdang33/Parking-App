package com.parking.parkingapp.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<VB : ViewBinding> : DialogFragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    abstract fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VB

    abstract fun initView()
    abstract fun initActions()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun shows(fm: FragmentManager): BaseDialog<VB>
    open fun onCreateCustomAnimation(): Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initActions()
        dialog?.window?.attributes?.windowAnimations = onCreateCustomAnimation()
    }
}
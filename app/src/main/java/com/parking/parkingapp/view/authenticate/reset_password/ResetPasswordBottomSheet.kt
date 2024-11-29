package com.parking.parkingapp.view.authenticate.reset_password

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.FragmentResetPasswordDialogBinding
import com.parking.parkingapp.view.BaseDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ResetPasswordBottomSheet : BaseDialog<FragmentResetPasswordDialogBinding>() {
    private val viewModel: ResetPasswordViewModel by viewModels()

    var onSuccess: (() -> Unit)? = null
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentResetPasswordDialogBinding =
        FragmentResetPasswordDialogBinding.inflate(inflater, container, false)

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.colorTransparent)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun initView() {
        lifecycleScope.launch {
            viewModel.singleEvent.collect { state ->
                loadingVisible(state is State.Loading)
                when (state) {
                    is State.Error -> handleResetError(state.error as ResetPasswordError)
                    State.Idle -> TODO()
                    is State.Loading -> loadingVisible(true)
                    is State.Success -> {
                        dismiss()
                        onSuccess?.invoke()
                    }
                }
            }
        }
    }

    override fun initActions() {
        binding.resetBtn.setOnClickListener {
            viewModel.resetPassword(binding.resetEdtAccount.text.toString())
        }
    }

    private fun handleResetError(resetError: ResetPasswordError) {
        val (emailError, commonError) = resetError
        binding.resetErrorEmail.apply {
            text = emailError
            hasVisible = emailError != null
        }

        binding.resetErrorCommon.apply {
            text = commonError
            hasVisible = commonError != null
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        binding.resetLoading.hasVisible = isLoading
        binding.resetBtn.hasVisible = !isLoading
    }

    override fun shows(fm: FragmentManager): BaseDialog<FragmentResetPasswordDialogBinding> {
        kotlin.runCatching {
            if (!fm.isStateSaved) {
                show(fm, ResetPasswordBottomSheet::class.java.name)
            }
        }
        return this
    }
}
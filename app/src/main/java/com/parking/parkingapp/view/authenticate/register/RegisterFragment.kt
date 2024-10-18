package com.parking.parkingapp.view.authenticate.register

import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentRegisterBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.authenticate.login.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment: BaseFragment<FragmentRegisterBinding>() {
    private val viewModel: RegisterViewModel by viewModels()
    private var isShowingPassword = false
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.registerBtn.setOnClickListener {
            viewModel.register(
                binding.registerEdtAccount.text.toString(),
                binding.registerEdtPassword.text.toString(),
                binding.registerEdtRetypePassword.text.toString()
            )
        }
        binding.registerGoLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.registerEyeButton.setOnClickListener {
            with(it as ImageView) {
                if (isShowingPassword) {
                    setImageResource(R.drawable.eye_on_fill)
                    binding.registerEdtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.registerEdtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    setImageResource(R.drawable.eye_off_fill)
                    binding.registerEdtPassword.inputType = InputType.TYPE_CLASS_TEXT
                    binding.registerEdtPassword.transformationMethod = null
                }
                isShowingPassword = !isShowingPassword
                binding.registerEdtPassword.setSelection(binding.registerEdtPassword.text.length)
            }
        }

        binding.registerEyeRetype.setOnClickListener {
            with(it as ImageView) {
                if (isShowingPassword) {
                    setImageResource(R.drawable.eye_on_fill)
                    binding.registerEdtRetypePassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.registerEdtRetypePassword.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    setImageResource(R.drawable.eye_off_fill)
                    binding.registerEdtRetypePassword.inputType = InputType.TYPE_CLASS_TEXT
                    binding.registerEdtRetypePassword.transformationMethod = null
                }
                isShowingPassword = !isShowingPassword
                binding.registerEdtRetypePassword.setSelection(binding.registerEdtRetypePassword.text.length)
            }
        }

        lifecycleScope.launch {
            viewModel.singleEvent.collect { state ->
                when (state) {
                    is State.Error -> {
                        visibleTextError(true)
                        loadingVisible(false)
                    }

                    State.Idle -> {
                        //suppress
                    }
                    State.Loading -> {
                        loadingVisible(true)
                    }

                    State.Success -> {
                        visibleTextError(false)
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        loadingVisible(false)
                    }
                }
            }
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        if (isLoading) {
            binding.registerLoading.visibility = VISIBLE
            binding.registerBtn.visibility = INVISIBLE
        } else {
            binding.registerLoading.visibility = INVISIBLE
            binding.registerBtn.visibility = VISIBLE
        }

    }

    private fun visibleTextError(visible: Boolean) {
        binding.registerTextError.visibility = if (visible) VISIBLE else GONE
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRegisterBinding = FragmentRegisterBinding.inflate(inflater, container, false)
}
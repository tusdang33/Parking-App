package com.parking.parkingapp.view.authenticate.login

import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentLoginBinding
import com.parking.parkingapp.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment: BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by viewModels()
    private var isShowingPassword = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginBtn.setOnClickListener {
            viewModel.login(
                binding.loginEdtAccount.text.toString(),
                binding.loginEdtPassword.text.toString()
            )
        }

        binding.loginGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        lifecycleScope.launch {
            viewModel.singleEvent.collect { state ->
                when (state) {
                    is State.Error -> {
                        handleLoginError(true)
                        loadingVisible(false)
                    }
                    State.Idle -> {
                        //suppress
                    }

                    State.Loading -> {
                        loadingVisible(true)
                    }

                    State.Success -> {
                        navigateToHomeScreen()
                        handleLoginError(false)
                        loadingVisible(false)
                    }
                }
            }
        }

        binding.loginEyeButton.setOnClickListener {
            with(it as ImageView) {
                if (isShowingPassword) {
                    setImageResource(R.drawable.eye_on_fill)
                    binding.loginEdtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.loginEdtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    setImageResource(R.drawable.eye_off_fill)
                    binding.loginEdtPassword.inputType = InputType.TYPE_CLASS_TEXT
                    binding.loginEdtPassword.transformationMethod = null
                }
                isShowingPassword = !isShowingPassword
                binding.loginEdtPassword.setSelection(binding.loginEdtPassword.text.length)
            }
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        if (isLoading) {
            binding.loginLoading.visibility = VISIBLE
            binding.loginBtn.visibility = View.INVISIBLE
        } else {
            binding.loginLoading.visibility = View.INVISIBLE
            binding.loginBtn.visibility = VISIBLE
        }

    }

    private fun handleLoginError(isVisible: Boolean) {
        binding.loginTextError.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun navigateToHomeScreen() {
        val navOption = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment, null, navOption)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)
}
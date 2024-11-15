package com.parking.parkingapp.view.authenticate.login

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.FragmentLoginBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.authenticate.reset_password.ResetPasswordBottomSheet
import com.parking.parkingapp.view.profile.ChangeProfileSuccessDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment: BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by viewModels()
    private var isShowingPassword = false

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
        }
    }

    override fun initActions() {
        binding.loginGoReset.setOnClickListener {
            ResetPasswordBottomSheet().apply {
                onSuccess = { ChangeProfileSuccessDialog().shows(parentFragmentManager) }
            }.shows(parentFragmentManager)
        }

        binding.loginBtn.setOnClickListener {
            viewModel.login(
                binding.loginEdtAccount.text.toString(),
                binding.loginEdtPassword.text.toString()
            )
        }

        binding.loginGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginEyeButton.setOnClickListener { eye ->
            with(binding.loginEdtPassword) {
                if (isShowingPassword) {
                    (eye as ImageView).setImageResource(com.parking.parkingapp.R.drawable.eye_on_fill)
                    inputType =
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                } else {
                    (eye as ImageView).setImageResource(com.parking.parkingapp.R.drawable.eye_off_fill)
                    inputType = android.text.InputType.TYPE_CLASS_TEXT
                    transformationMethod = null
                }
                isShowingPassword = !isShowingPassword
                setSelection(this.text.length)
            }
        }
    }

    override fun intiData() {
        //suppress
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.singleEvent.collect { state ->
                when (state) {
                    is State.Error -> {
                        handleLoginError(state.error as FormatLoginError)
                        loadingVisible(false)
                    }
                    State.Idle -> {
                        //suppress
                    }

                    State.Loading -> {
                        loadingVisible(true)
                    }

                    is State.Success -> {
                        navigateToHomeScreen()
                        handleLoginError(FormatLoginError())
                        loadingVisible(false)
                    }
                }
            }
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        binding.loginLoading.hasVisible = isLoading
        binding.loginBtn.hasVisible = !isLoading
    }

    private fun handleLoginError(loginError: FormatLoginError) {
        val (emailError, passwordError, commonError, isAllowResetPassword) = loginError
        binding.loginErrorEmail.apply {
            hasVisible = emailError != null
            text = emailError
        }

        binding.loginErrorPassword.apply {
            hasVisible = passwordError != null
            text = passwordError
        }

        binding.loginErrorCommon.apply {
            hasVisible = commonError != null
            text = commonError
        }

        binding.loginGoReset.hasVisible = isAllowResetPassword
    }

    private fun navigateToHomeScreen() {
        val navOption = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController().navigate(R.id.action_loginFragment_to_mapboxFragment, null, navOption)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

}
package com.parking.parkingapp.view.authenticate.register

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.FragmentRegisterBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.authenticate.login.FormatRegisterError
import com.parking.parkingapp.view.authenticate.login.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment: BaseFragment<FragmentRegisterBinding>() {
    private val viewModel: RegisterViewModel by viewModels()
    private var isShowingPassword = false
    private var isShowingRetypePassword = false

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
        }
    }

    override fun initActions() {
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

        binding.registerEyeButton.setOnClickListener { eye ->
            with(binding.registerEdtPassword) {
                if (isShowingPassword) {
                    (eye as ImageView).setImageResource(R.drawable.eye_on_fill)
                    inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    (eye as ImageView).setImageResource(R.drawable.eye_off_fill)
                    inputType = InputType.TYPE_CLASS_TEXT
                    transformationMethod = null
                }
                isShowingPassword = !isShowingPassword
                setSelection(this.text.length)
            }
        }

        binding.registerEyeRetype.setOnClickListener { eye ->
            with(binding.registerEdtRetypePassword) {
                if (isShowingRetypePassword) {
                    (eye as ImageView).setImageResource(R.drawable.eye_on_fill)
                    inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    (eye as ImageView).setImageResource(R.drawable.eye_off_fill)
                    inputType = InputType.TYPE_CLASS_TEXT
                    transformationMethod = null
                }
                isShowingRetypePassword = !isShowingRetypePassword
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
                        handleRegisterError(state.error as FormatRegisterError)
                        loadingVisible(false)
                    }

                    State.Idle -> {
                        //suppress
                    }
                    State.Loading -> {
                        loadingVisible(true)
                    }

                    State.Success -> {
                        handleRegisterError(FormatRegisterError())
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

    private fun handleRegisterError(registerError: FormatRegisterError) {
        val (emailError, passwordError, retypeError, commonError) = registerError
        binding.registerErrorEmail.apply {
            hasVisible = emailError != null
            text = emailError
        }

        binding.registerErrorPassword.apply {
            hasVisible = passwordError != null
            text = passwordError
        }

        binding.registerErrorRetypePassword.apply {
            hasVisible = retypeError != null
            text = retypeError
        }

        binding.registerErrorCommon.apply {
            hasVisible = commonError != null
            text = commonError
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRegisterBinding = FragmentRegisterBinding.inflate(inflater, container, false)
}
package com.parking.parkingapp.view.profile.change_password

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.BottomsheetChangePasswordBinding
import com.parking.parkingapp.view.BaseDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordBottomSheet : BaseDialog<BottomsheetChangePasswordBinding>() {
    private val viewModel: ChangePasswordViewModel by viewModels()

    private var isShowingOldPassword = false
    private var isShowingNewPassword = false
    private var isShowingRetypePassword = false
    var onSuccess: (() -> Unit)? = null
    var email: String? = null

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomsheetChangePasswordBinding = BottomsheetChangePasswordBinding.inflate(inflater, container, false)

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
                when (state) {
                    is State.Error -> {
                        loadingVisible(false)
                        handleChangePasswordError(state.error as FormatChangePasswordError)
                    }

                    State.Idle -> TODO()
                    is State.Loading -> loadingVisible(true)
                    is State.Success -> {
                        loadingVisible(false)
                        dismiss()
                        onSuccess?.invoke()
                    }
                }

            }
        }
    }

    private fun handleChangePasswordError(changePasswordError: FormatChangePasswordError) {
        val (emailError, passwordError, retypeError, commonError) = changePasswordError
        binding.profileErrorOldPassword.apply {
            hasVisible = emailError != null
            text = emailError
        }

        binding.profileErrorNewPassword.apply {
            hasVisible = passwordError != null
            text = passwordError
        }

        binding.profileErrorRetypePassword.apply {
            hasVisible = retypeError != null
            text = retypeError
        }

        binding.profileErrorCommon.apply {
            hasVisible = commonError != null
            text = commonError
        }
    }

    override fun initActions() {
        binding.changeEyeButtonOld.setOnClickListener { eye ->
            with(binding.changeEdtOldPassword) {
                if (isShowingOldPassword) {
                    (eye as ImageView).setImageResource(R.drawable.eye_on_fill)
                    inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    (eye as ImageView).setImageResource(R.drawable.eye_off_fill)
                    inputType = InputType.TYPE_CLASS_TEXT
                    transformationMethod = null
                }
                isShowingOldPassword = !isShowingOldPassword
                setSelection(this.text.length)
            }
        }

        binding.changeEyeButtonNew.setOnClickListener { eye ->
            with(binding.changeEdtNewPassword) {
                if (isShowingNewPassword) {
                    (eye as ImageView).setImageResource(R.drawable.eye_on_fill)
                    inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    transformationMethod = PasswordTransformationMethod.getInstance()
                } else {
                    (eye as ImageView).setImageResource(R.drawable.eye_off_fill)
                    inputType = InputType.TYPE_CLASS_TEXT
                    transformationMethod = null
                }
                isShowingNewPassword = !isShowingNewPassword
                setSelection(this.text.length)
            }
        }

        binding.changeEyeButtonRetype.setOnClickListener { eye ->
            with(binding.changeEdtRetypePassword) {
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

        binding.saveChangePassword.setOnClickListener {
            viewModel.submitPassword(
                email = email ?: "",
                oldPassword = binding.changeEdtOldPassword.text.toString(),
                newPassword = binding.changeEdtNewPassword.text.toString(),
                retypePassword = binding.changeEdtRetypePassword.text.toString(),
            )
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        binding.changePassLoading.hasVisible = isLoading
        binding.saveChangePassword.hasVisible = !isLoading
    }

    override fun onCreateCustomAnimation(): Int = R.style.BottomSheetDialogAnim
    override fun shows(fm: FragmentManager): BaseDialog<BottomsheetChangePasswordBinding> {
        kotlin.runCatching {
            if (!fm.isStateSaved) {
                show(fm, ChangePasswordBottomSheet::class.java.name)
            }
        }
        return this
    }
}
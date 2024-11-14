package com.parking.parkingapp.view.profile

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.grantReadPermissionToUri
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.FragmentProfileBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.profile.change_password.ChangePasswordBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment: BaseFragment<FragmentProfileBinding>() {
    private val viewModel: ProfileViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
        if (result != null) {
            viewModel.pickedImage = result
            grantReadPermissionToUri(requireContext(), result)
            Glide
                .with(requireContext())
                .load(result)
                .centerCrop()
                .placeholder(R.drawable.man)
                .into(binding.profileAvatar)
        }
    }

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(true)
            setOnHeaderBack { findNavController().popBackStack() }
            setHeaderTitle(applicationContext.getString(R.string.password))
        }
    }

    override fun initActions() {
        binding.profileUsername.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                //suppress
            }

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
                p0?.let { query ->
                    handleAllowChangeInfo(query.toString())
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                //suppress
            }
        })

        binding.profileImagePick.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.profileChangePassword.setOnClickListener {
            ChangePasswordBottomSheet().apply {
                onSuccess = {
                    ChangeProfileSuccessDialog().shows(parentFragmentManager)
                }
                email = viewModel.userData.value.email
            }.shows(parentFragmentManager)
        }

        binding.profileChange.setOnClickListener {
            viewModel.updateUserData(binding.profileUsername.text.toString())
        }
    }

    private fun handleAllowChangeInfo(username: String) {
        with(binding.profileChange) {
            isEnabled = username != viewModel.userData.value.username
            backgroundTintList = ColorStateList.valueOf(
                if (isEnabled) requireContext().getColor(R.color.blue)
                else requireContext().getColor(R.color.gray)
            )
        }
    }

    override fun intiData() {
        viewModel.fetchUserData()
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.userData.collect {
                Glide
                    .with(requireContext())
                    .load(it.image)
                    .centerCrop()
                    .placeholder(R.drawable.man)
                    .into(binding.profileAvatar)

                binding.profileUsername.setText(it.username)
                binding.profileEmail.setText(it.email)
            }
        }
        scope.launch {
            viewModel.singleEvent.collect { state ->
                when (state) {
                    is State.Error -> {
                        loadingVisible(false)
                    }

                    State.Idle -> {
                        loadingVisible(false)
                    }

                    State.Loading -> {
                        loadingVisible(true)
                    }

                    is State.Success -> {
                        loadingVisible(false)
                        ChangeProfileSuccessDialog().shows(parentFragmentManager)
                    }
                }
            }
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        binding.profileLoading.hasVisible = isLoading
        binding.profileChange.hasVisible = !isLoading
    }
}
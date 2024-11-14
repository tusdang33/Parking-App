package com.parking.parkingapp.view.profile

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
import com.parking.parkingapp.databinding.FragmentProfileBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
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
        binding.profileImagePick.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.profileChangePassword.setOnClickListener {
            ChangePasswordBottomSheet().shows(parentFragmentManager)
        }

        binding.profileChange.setOnClickListener {
            viewModel.updateUserData(binding.profileUsername.text.toString())
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
                    is State.Error -> TODO()
                    State.Idle -> TODO()
                    State.Loading -> TODO()
                    State.Success -> {
                        ChangePasswordBottomSheet().shows(parentFragmentManager)
                    }
                }
            }
        }
    }
}
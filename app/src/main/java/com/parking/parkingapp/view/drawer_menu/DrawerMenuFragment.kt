package com.parking.parkingapp.view.drawer_menu

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentDrawerMenuBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DrawerMenuFragment: BaseFragment<FragmentDrawerMenuBinding>() {
    private val viewModel: DrawerMenuViewModel by viewModels()
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDrawerMenuBinding = FragmentDrawerMenuBinding.inflate(inflater, container, false)

    override fun initViews() {
        close()
    }

    override fun initActions() {
        binding.drawerRightSideContainer.setOnClickListener {
            close()
        }
        binding.menuLogoutBtn.setOnClickListener {
            viewModel.logout()
        }
        binding.drawerEditProfile.setOnClickListener {
            (activity as? MainActivity)?.apply {
                mainNavController().navigate(R.id.profileFragment)
            }
            close()
        }
    }

    override fun intiData() {
        viewModel.fetchUserData()
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.singleEvent.collect {
                when (it) {
                    is State.Error -> {
                        //suppress
                    }

                    State.Idle -> {
                        //suppress
                    }

                    State.Loading -> {
                        //suppress
                    }

                    is State.Success -> {
                        close() {
                            (activity as? MainActivity)?.apply {
                                mainNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                            }
                        }
                    }
                }
            }
        }
        scope.launch {
            viewModel.userData.collect {
                Glide
                    .with(requireContext())
                    .load(it.image)
                    .centerCrop()
                    .placeholder(R.drawable.man)
                    .into(binding.drawerAvatar)

                it.username?.let { binding.drawerUsername.text = it }
            }
        }
    }

    fun open() {
        binding.drawerContainer.visibility = VISIBLE
        binding.drawerContainer.post {
            binding.drawerLeftSideContainer.let { container ->
                ObjectAnimator.ofFloat(
                    container,
                    View.TRANSLATION_X,
                    -(container.measuredWidth.toFloat()),
                    0f
                ).apply {
                    duration = 300L
                    doOnStart {
                        binding.drawerBackground.visibility = VISIBLE
                        container.alpha = 1f
                    }
                    start()
                }
            }

            binding.drawerRightSideContainer.let { container ->
                ObjectAnimator.ofFloat(
                    container,
                    View.TRANSLATION_X,
                    (container.measuredWidth.toFloat()),
                    0f
                ).apply {
                    duration = 300L
                    doOnStart {
                        container.alpha = 1f
                    }
                    start()
                }
            }
        }
    }

    private fun close(onDone: (() -> Unit)? = null) {
        binding.drawerLeftSideContainer.let { container ->
            ObjectAnimator.ofFloat(
                container,
                View.TRANSLATION_X,
                0f,
                -(container.measuredWidth.toFloat()),
            ).apply {
                duration = 300L
                doOnEnd {
                    binding.drawerContainer.visibility = GONE
                    container.alpha = 0f
                    onDone?.invoke()
                    binding.drawerBackground.visibility = GONE
                }
                start()
            }
        }

        binding.drawerRightSideContainer.let { container ->
            ObjectAnimator.ofFloat(
                container,
                View.TRANSLATION_X,
                0f,
                (container.measuredWidth.toFloat()),
            ).apply {
                duration = 300L
                doOnEnd {
                    container.alpha = 0f
                }
                start()
            }
        }
    }
}
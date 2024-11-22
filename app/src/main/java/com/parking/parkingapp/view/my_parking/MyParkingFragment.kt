package com.parking.parkingapp.view.my_parking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.parking.parkingapp.databinding.FragmentMyParkingBinding
import com.parking.parkingapp.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyParkingFragment: BaseFragment<FragmentMyParkingBinding>() {
    private val viewModel: MyParkingViewModel by viewModels()

    private val myParkingAdapter by lazy { MyParkingAdapter() }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMyParkingBinding = FragmentMyParkingBinding.inflate(inflater, container, false)

    override fun initViews() {
        binding.myParkingRcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myParkingAdapter
        }
    }

    override fun initActions() {
    }

    override fun intiData() {
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.myPark.collect {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    myParkingAdapter.updateData(it)
                }
            }
        }
    }

}
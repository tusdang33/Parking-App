package com.parking.parkingapp.view.my_parking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.databinding.FragmentMyParkingBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
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
        (activity as? MainActivity)?.apply {
            isShowHeader(true)
            setOnHeaderBack()
            setHeaderTitle("My Parking")
            isShowMenu(true)
        }
        binding.myParkingRcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myParkingAdapter.apply {
                onParkClick = {
                    findNavController().navigate(
                        R.id.action_myParkingFragment_to_myParkDetailFragment,
                        Bundle().apply {
                            putParcelable(MyRentedPark::class.java.name, it)
                        })
                }
            }
        }
    }

    override fun initActions() {
        //suppress
    }

    override fun intiData() {
        viewModel.fetchData()
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.myPark.collect {
                myParkingAdapter.updateData(it)
            }
        }
    }

}
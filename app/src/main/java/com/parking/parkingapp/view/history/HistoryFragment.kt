package com.parking.parkingapp.view.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.databinding.FragmentHistoryBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.my_parking.MyParkingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment: BaseFragment<FragmentHistoryBinding>() {
    private val viewModel: HistoryViewModel by viewModels()
    private val myParkingAdapter by lazy { MyParkingAdapter() }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater,container,false)

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(true)
            setOnHeaderBack()
            setHeaderTitle("History")
            isShowMenu(true)
        }
        binding.historyRcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myParkingAdapter
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
            viewModel.historyPark.collect {
                myParkingAdapter.updateData(it)
            }
        }
    }

}
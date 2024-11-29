package com.parking.parkingapp.view.park

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.databinding.FragmentParkReservationBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.formatCurrency

class ParkReservationFragment: BaseFragment<FragmentParkReservationBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentParkReservationBinding = FragmentParkReservationBinding.inflate(inflater, container, false)

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
            setOnHeaderBack()
        }
        (arguments?.getParcelable(MyRentedPark::class.java.name) as? MyRentedPark)?.let {
            binding.reservationParkName.text = it.park.name
            binding.reservationParkAddress.text = it.park.address
            binding.reservationParkBill.text = formatCurrency(it.totalPay.toDouble()).uppercase()
            binding.reservationParkId.text = getString(R.string.booking_id, it.id)
        }
    }

    override fun initActions() {
        binding.doneButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_parkReservationFragment_to_mapHolderFragment
            )
        }
    }

    override fun intiData() {
        //suppress
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        //suppress
    }
}
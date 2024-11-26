package com.parking.parkingapp.view.my_parking

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.databinding.FragmentMyParkDetailBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.calculateDecimalTimeDifference
import com.parking.parkingapp.view.map.convertDecimalTimeToCalendar
import com.parking.parkingapp.view.map.formatTime
import com.parking.parkingapp.view.map.isCurrentTimeInRange
import com.parking.parkingapp.view.map.roundToOneDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyParkDetailFragment: BaseFragment<FragmentMyParkDetailBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMyParkDetailBinding = FragmentMyParkDetailBinding.inflate(inflater, container, false)

    private var currentPark: MyRentedPark? = null

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(true)
            setOnHeaderBack()
        }
        currentPark = arguments?.getParcelable(MyRentedPark::class.java.name) as? MyRentedPark
        currentPark?.let { myPark ->
            (activity as? MainActivity)?.apply {
                setHeaderTitle(myPark.park.name)
            }
            Glide
                .with(requireContext())
                .load(myPark.park.image)
                .centerCrop()
                .placeholder(R.drawable.parking_placeholder)
                .into(binding.parkImage)
            binding.parkName.text = myPark.park.name
            binding.parkAddress.text = myPark.park.address
            binding.detailAddress.text = myPark.park.detailAddress
            binding.pickedStartTime.text = myPark.startTime
            binding.pickedEndTime.text = myPark.endTime
            val (hourStart, minuteStart) = currentPark!!.startTime.split(":").map { it.toInt() }
            val (hourEnd, minuteEnd) = currentPark!!.endTime.split(":").map { it.toInt() }
            binding.totalTime.text = calculateDecimalTimeDifference(
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourStart)
                    set(Calendar.MINUTE, minuteStart)
                },
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourEnd)
                    set(Calendar.MINUTE, minuteEnd)
                }
            ).roundToOneDecimal().toString()
            val startTime = formatTime(myPark.park.openTime)
            val closeTime = formatTime(myPark.park.closeTime)
            binding.detailTime.apply {
                val isInTime = isCurrentTimeInRange(startTime, closeTime)
                text = getString(
                    if (isInTime) R.string.open_time
                    else R.string.closed_time,
                    "$startTime - $closeTime"
                )
                setTextColor(
                    if (isInTime) requireContext().resources.getColor(R.color.green, null)
                    else requireContext().resources.getColor(R.color.red, null)
                )
            }
        }
    }

    override fun initActions() {
        binding.myParkAddTimeButton.setOnClickListener {
            AddTimeBottomSheet().apply {
                maxTimeCanAdd = calculateDecimalTimeDifference(
                    Calendar.getInstance(),
                    currentPark!!.park.closeTime.convertDecimalTimeToCalendar()
                ).roundToOneDecimal().toInt()
                onAdd = {

                }
            }.shows(parentFragmentManager)
        }
    }

    override fun intiData() {

    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
    }

}
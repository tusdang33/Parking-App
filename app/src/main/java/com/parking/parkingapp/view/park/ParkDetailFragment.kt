package com.parking.parkingapp.view.park

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.databinding.FragmentParkDetailBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.MapboxFragment
import com.parking.parkingapp.view.map.calculateDecimalTimeDifference
import com.parking.parkingapp.view.map.formatCurrency
import com.parking.parkingapp.view.map.formatTime
import com.parking.parkingapp.view.map.isCurrentTimeInRange
import com.parking.parkingapp.view.map.roundToOneDecimal
import java.util.Calendar

class ParkDetailFragment: BaseFragment<FragmentParkDetailBinding>() {

    companion object {
        private const val TIME_FORMAT = "%02d:%02d"
    }
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentParkDetailBinding = FragmentParkDetailBinding.inflate(inflater, container, false)

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(true)
            setOnHeaderBack()
        }
        val park = arguments?.getParcelable(ParkModel::class.java.name) as? ParkModel
        park?.let {
            (activity as? MainActivity)?.apply {
                setHeaderTitle(it.name)
            }
            Glide
                .with(requireContext())
                .load(it.image)
                .centerCrop()
                .placeholder(R.drawable.man)
                .into(binding.parkImage)
            binding.parkName.text = it.name
            binding.parkAddress.text = it.address
            binding.detailAddress.text = it.address
            binding.parkPrice.text = formatCurrency(it.pricePerHour)
            binding.parkDistance.text = arguments?.getString(ParkModel::class.java.name + "distance")
            val startTime = formatTime(it.openTime)
            val closeTime = formatTime(it.closeTime)
            binding.detailTime.apply {
                val isInTime = isCurrentTimeInRange(startTime, closeTime)
                text = getString(
                    if (isInTime) R.string.open_time
                    else R.string.closed_time,
                    "$startTime - $closeTime"
                )
                setTextColor(
                    if (isInTime) Color.GREEN
                    else Color.RED
                )
            }
        }
    }

    override fun initActions() {
        binding.parkDirectionButton.setOnClickListener {
            parentFragment?.setFragmentResult(MapboxFragment::class.java.name, Bundle().apply {
                putBoolean(ParkDetailFragment::class.java.name, true)
            })
            findNavController().popBackStack()
        }
        handlePickTime()
    }

    private fun handlePickTime() {
        var startTime: Calendar? = null
        var endtime: Calendar? = null
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        binding.detailPickStartTime.setOnClickListener {
            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                startTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val time = String.format(TIME_FORMAT, selectedHour, selectedMinute)
                binding.pickedStartTime.text = time
                if (startTime != null && endtime != null) {
                    binding.totalTime.text = getString(
                        R.string.total_time,
                        calculateDecimalTimeDifference(startTime!!, endtime!!).roundToOneDecimal().toString()
                    )
                }
            }, hour, minute, true)
            timePicker.show()
        }

        binding.detailPickEndTime.setOnClickListener {
            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                endtime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val time = String.format(TIME_FORMAT, selectedHour, selectedMinute)
                binding.pickedEndTime.text = time
                binding.totalTime.text = getString(
                    R.string.total_time,
                    calculateDecimalTimeDifference(startTime!!, endtime!!).roundToOneDecimal().toString()
                )
            }, hour, minute, true)
            timePicker.show()
        }
    }

    override fun intiData() {

    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {

    }

}
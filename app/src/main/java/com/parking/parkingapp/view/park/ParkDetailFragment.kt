package com.parking.parkingapp.view.park

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
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
import com.parking.parkingapp.view.map.formatCurrencyPerHour
import com.parking.parkingapp.view.map.formatTime
import com.parking.parkingapp.view.map.isCurrentTimeInRange
import com.parking.parkingapp.view.map.roundToOneDecimal
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class ParkDetailFragment: BaseFragment<FragmentParkDetailBinding>() {
    private val viewModel: ParkDetailViewModel by viewModels()

    private var currentPark: ParkModel? = null

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
        setTotalTime(null, null, 0)
        currentPark = arguments?.getParcelable(ParkModel::class.java.name) as? ParkModel
        currentPark?.let {
            (activity as? MainActivity)?.apply {
                setHeaderTitle(it.name)
            }
            Glide
                .with(requireContext())
                .load(it.image)
                .centerCrop()
                .placeholder(R.drawable.parking_placeholder)
                .into(binding.parkImage)
            binding.parkName.text = it.name
            binding.parkAddress.text = it.address
            binding.detailAddress.text = it.address
            binding.parkPrice.text = formatCurrencyPerHour(it.pricePerHour)
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
                    if (isInTime) requireContext().resources.getColor(R.color.green, null)
                    else requireContext().resources.getColor(R.color.red, null)
                )
            }
        }
    }

    @SuppressLint("NewApi")
    override fun initActions() {
        binding.parkDirectionButton.setOnClickListener {
            parentFragment?.setFragmentResult(MapboxFragment::class.java.name, Bundle().apply {
                putBoolean(ParkDetailFragment::class.java.name, true)
            })
            findNavController().popBackStack()
        }
        handlePickTime()
        binding.checkoutButton.setOnClickListener {
            viewModel.submitCheckout(
                currentPark!!,
                binding.pickedStartTime.text.toString(),
                binding.pickedEndTime.text.toString(),
                binding.totalTime.text.split(" ").first().toDouble()
            )
        }
    }

    private fun handlePickTime() {
        var startTime: Calendar? = null
        var endTime: Calendar? = null
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        binding.detailPickStartTime.setOnClickListener {
            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                startTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }.validatePickedTime(
                    startTime = startTime,
                    endTime = endTime,
                    parkModel = currentPark!!,
                    isStartTime = true
                )
                val time =
                    String.format(TIME_FORMAT, startTime!!.get(Calendar.HOUR_OF_DAY), startTime!!.get(Calendar.MINUTE))
                binding.pickedStartTime.text = time
                setTotalTime(startTime, endTime, currentPark!!.pricePerHour)
            }, hour, minute, true)
            timePicker.show()
        }

        binding.detailPickEndTime.setOnClickListener {
            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                endTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }.validatePickedTime(
                    startTime = startTime,
                    endTime = endTime,
                    parkModel = currentPark!!,
                    isStartTime = false
                )
                val time =
                    String.format(TIME_FORMAT, endTime!!.get(Calendar.HOUR_OF_DAY), endTime!!.get(Calendar.MINUTE))
                binding.pickedEndTime.text = time
                setTotalTime(startTime, endTime, currentPark!!.pricePerHour)
            }, hour, minute, true)
            timePicker.show()
        }
    }

    private fun setTotalTime(startTime: Calendar?, endTime: Calendar?, price: Int) {
        if (startTime != null && endTime != null) {
            val totalTime = calculateDecimalTimeDifference(startTime, endTime).roundToOneDecimal()
            binding.checkoutButton.apply {
                text = getString(
                    R.string.detail_checkout,
                    formatCurrency(totalTime * price)
                ).uppercase()
                isEnabled = true
                backgroundTintList = resources.getColorStateList(R.color.black, null)
            }
            binding.totalTime.apply {
                text = getString(
                    R.string.total_time,
                    totalTime.toString()
                )
                backgroundTintList = resources.getColorStateList(R.color.main_yellow, null)
            }
        } else {
            binding.checkoutButton.apply {
                isEnabled = false
                backgroundTintList = resources.getColorStateList(R.color.gray, null)
            }
            binding.totalTime.apply {
                backgroundTintList = resources.getColorStateList(R.color.gray, null)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun Calendar.validatePickedTime(
        startTime: Calendar?,
        endTime: Calendar?,
        parkModel: ParkModel,
        isStartTime: Boolean
    ): Calendar {
        val formatter = DateTimeFormatter.ofPattern("h:mma")
        val start = LocalTime.parse(formatTime(parkModel.openTime), formatter)
        val close = LocalTime.parse(formatTime(parkModel.closeTime), formatter)
        val pickedLocalTime = LocalTime.of(this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE))
        val pickedStartTime = startTime?.let { LocalTime.of(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE)) }
        val pickedEndTime = endTime?.let { LocalTime.of(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE)) }
        val isInTime = isCurrentTimeInRange(formatTime(parkModel.openTime), formatTime(parkModel.closeTime))
        val now = LocalTime.now()
        return if (isStartTime) {
            if (pickedLocalTime.isBefore(start)) {
                Calendar.getInstance().apply {
                    set(
                        Calendar.HOUR_OF_DAY, if (isInTime) now.hour
                        else start.hour
                    )
                    set(
                        Calendar.MINUTE, if (isInTime) now.minute
                        else start.minute
                    )
                }
            } else if (pickedLocalTime.isBefore(now) || pickedLocalTime.isAfter(close)) {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, now.hour)
                    set(Calendar.MINUTE, now.minute)
                }
            } else if (pickedEndTime != null && pickedLocalTime.isAfter(pickedEndTime)) {
                Calendar.getInstance().apply {
                    val nowPlusHour = pickedEndTime.hour + 1
                    val hour = if (nowPlusHour < close.hour) nowPlusHour else close.hour
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, now.minute)
                }
            } else {
                this
            }
        } else {
            if (pickedLocalTime.isAfter(close)) {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, close.hour)
                    set(Calendar.MINUTE, close.minute)
                }
            } else if (pickedLocalTime.isBefore(now)
                || pickedLocalTime.isBefore(start)
            ) {
                Calendar.getInstance().apply {
                    val nowPlusHour = now.hour + 1
                    val hour = if ((nowPlusHour) < close.hour) {
                        if (pickedStartTime != null && nowPlusHour <= pickedStartTime.hour) {
                            pickedStartTime.hour + 1
                        } else {
                            nowPlusHour
                        }
                    } else {
                        close.hour
                    }
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, now.minute)
                }
            } else if (pickedStartTime != null && pickedLocalTime <= pickedStartTime) {
                Calendar.getInstance().apply {
                    val hour = if ((pickedStartTime.hour + 1) < close.hour) pickedStartTime.hour + 1 else close.hour
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, now.minute)
                }
            } else {
                this
            }
        }
    }

    override fun intiData() {

    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {

    }

}
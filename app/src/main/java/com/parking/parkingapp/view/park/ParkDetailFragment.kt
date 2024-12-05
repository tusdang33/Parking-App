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
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.databinding.FragmentParkDetailBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.MapboxFragment
import com.parking.parkingapp.view.map.calculateDecimalTimeDifference
import com.parking.parkingapp.view.map.dateFormatter
import com.parking.parkingapp.view.map.formatCurrency
import com.parking.parkingapp.view.map.formatCurrencyPerHour
import com.parking.parkingapp.view.map.formatTime
import com.parking.parkingapp.view.map.isCurrentTimeInRange
import com.parking.parkingapp.view.map.roundToOneDecimal
import com.parking.parkingapp.view.park.ParkDetailViewModel.Companion.OUT_OF_SLOT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar

@AndroidEntryPoint
class ParkDetailFragment: BaseFragment<FragmentParkDetailBinding>() {
    private val viewModel: ParkDetailViewModel by viewModels()

    private var currentParkDetail: ParkModel? = null

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
        currentParkDetail = arguments?.getParcelable(ParkModel::class.java.name) as? ParkModel
        currentParkDetail?.let {
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
            binding.detailAddress.text = it.detailAddress
            binding.parkSlot.text =
                resources.getString(R.string.quantity_empty_slot, (it.maxSlot - it.currentSlot).toString())
            binding.detailPrice.text = formatCurrencyPerHour(it.pricePerHour)
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
                parkModel = currentParkDetail!!,
                startTime = binding.pickedStartTime.text.toString(),
                endTime = binding.pickedEndTime.text.toString(),
                totalHour = binding.totalTime.text.split(" ").first().toDouble(),
                isSubmitInOpenTime = run {
                    val endTime = LocalTime.parse(formatTime(currentParkDetail!!.closeTime), dateFormatter)
                    LocalTime.now().isBefore(endTime)
                }
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
            val timePicker = TimePickerDialog(
                requireContext(),
                R.style.CustomTimePickerTheme, { _, selectedHour, selectedMinute ->
                startTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }.validatePickedTime(
                    startTime = startTime,
                    endTime = endTime,
                    parkModel = currentParkDetail!!,
                    isStartTime = true
                )
                if (startTime == null) {
                    binding.parkError.apply {
                        text = getString(R.string.start_time_not_valid)
                        hasVisible = true
                    }
                    return@TimePickerDialog
                }
                binding.parkError.hasVisible = false
                val time =
                    String.format(TIME_FORMAT, startTime!!.get(Calendar.HOUR_OF_DAY), startTime!!.get(Calendar.MINUTE))
                binding.pickedStartTime.text = time
                setTotalTime(startTime, endTime, currentParkDetail!!.pricePerHour)
            }, hour, minute, true)
            timePicker.show()
        }

        binding.detailPickEndTime.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                R.style.CustomTimePickerTheme, { _, selectedHour, selectedMinute ->
                endTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }.validatePickedTime(
                    startTime = startTime,
                    endTime = endTime,
                    parkModel = currentParkDetail!!,
                    isStartTime = false
                )
                if (endTime == null) {
                    binding.parkError.apply {
                        text = getString(R.string.end_time_not_valid)
                        hasVisible = true
                    }
                    return@TimePickerDialog
                }
                binding.parkError.hasVisible = false
                val time =
                    String.format(TIME_FORMAT, endTime!!.get(Calendar.HOUR_OF_DAY), endTime!!.get(Calendar.MINUTE))
                binding.pickedEndTime.text = time
                setTotalTime(startTime, endTime, currentParkDetail!!.pricePerHour)
            }, hour, minute, true)
            timePicker.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    override fun intiData() {
        //suppress
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.singleEvent.collect { state ->
                loadingVisible(state is State.Loading)
                handleError((state as? State.Error)?.error as? RentError)
                when (state) {
                    is State.Error -> {
                        handleError(state.error as RentError)
                    }

                    State.Idle -> {
                        //suppress
                    }

                    is State.Loading -> {
                        //suppress
                    }

                    is State.Success -> findNavController().navigate(
                        R.id.action_parkDetailFragment_to_parkReservationFragment,
                        Bundle().apply {
                            putParcelable(
                                MyRentedPark::class.java.name,
                                (state.success as RentSuccess).result
                            )
                        }
                    )
                }
            }
        }
    }

    private fun loadingVisible(isLoading: Boolean) {
        binding.parkLoading.hasVisible = isLoading
        binding.checkoutButton.hasVisible = !isLoading
    }

    private fun handleError(rentError: RentError?) {
        if (rentError != null) {
            binding.parkError.apply {
                text =
                    if (rentError.errorMessage == OUT_OF_SLOT) resources.getString(R.string.out_of_slot)
                    else rentError.errorMessage
                hasVisible = true
            }
        } else {
            binding.parkError.hasVisible = false
        }

    }
}
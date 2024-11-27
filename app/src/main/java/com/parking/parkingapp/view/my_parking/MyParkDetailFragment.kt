package com.parking.parkingapp.view.my_parking

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.parking.parkingapp.data.model.RentStatus
import com.parking.parkingapp.databinding.FragmentMyParkDetailBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.MapboxFragment
import com.parking.parkingapp.view.map.calculateDecimalTimeDifference
import com.parking.parkingapp.view.map.convertDecimalTimeToCalendar
import com.parking.parkingapp.view.map.formatTime
import com.parking.parkingapp.view.map.isCurrentTimeInRange
import com.parking.parkingapp.view.map.roundToOneDecimal
import com.parking.parkingapp.view.profile.ChangeProfileSuccessDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class MyParkDetailFragment: BaseFragment<FragmentMyParkDetailBinding>() {
    private val viewModel: MyParkingViewModel by viewModels()
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
            binding.parkCheckinLoading.indeterminateTintList = resources.getColorStateList(
                when (myPark.status) {
                    RentStatus.RENTING -> R.color.green
                    RentStatus.CHECKED_IN -> R.color.black
                    else -> R.color.green
                },
                null
            )
            binding.checkinButton.apply {
                backgroundTintList = resources.getColorStateList(
                    when (myPark.status) {
                        RentStatus.RENTING -> R.color.green
                        RentStatus.CHECKED_IN -> R.color.black
                        else -> R.color.green
                    },
                    null
                )
                text = resources.getString(
                    when (myPark.status) {
                        RentStatus.RENTING -> R.string.start_rent
                        RentStatus.CHECKED_IN -> R.string.stop_rent
                        else -> R.string.start_rent
                    }
                )
            }
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
        binding.parkDirectionButton.setOnClickListener {
            parentFragment?.setFragmentResult(MapboxFragment::class.java.name, Bundle().apply {
                putParcelable(MyParkDetailFragment::class.java.name, currentPark!!)
            })
            findNavController().navigate(R.id.mapHolderFragment)
        }
        binding.myParkAddTimeButton.setOnClickListener {
            AddTimeBottomSheet().apply {
                parkPrice = currentPark!!.park.pricePerHour
                maxTimeCanAdd = calculateDecimalTimeDifference(
                    Calendar.getInstance().apply {
                        set(
                            Calendar.HOUR_OF_DAY,
                            currentPark!!.endTime.split(":").first().toInt()
                        )
                        set(
                            Calendar.MINUTE,
                            currentPark!!.endTime.split(":").last().toInt()
                        )
                    },
                    currentPark!!.park.closeTime.convertDecimalTimeToCalendar()
                ).roundToOneDecimal().toInt()
                onAdd = {
                    viewModel.addTime(it, binding.pickedEndTime.text.toString(), currentPark!!)
                }
            }.shows(parentFragmentManager)
        }
        binding.checkinButton.setOnClickListener {
            if (currentPark!!.status == RentStatus.RENTING) {
                viewModel.checkin(currentPark!!)
            } else {
                viewModel.stopRent(currentPark!!)
            }

        }
        binding.cancelButton.setOnClickListener {
            viewModel.cancel(currentPark!!)
        }
    }

    override fun intiData() {
        //suppress
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.singleEvent.collect { state ->
                handleAddTimeLoading((state as? State.Loading)?.loading is UpdateAddTimeLoading)
                handleCheckinLoading((state as? State.Loading)?.loading is CheckinLoading)
                handleCancelLoading((state as? State.Loading)?.loading is CancelLoading)
                when (state) {
                    is State.Error -> {
                        handleAddingTimeError(state.error as? UpdateAddTimeError)
                    }

                    State.Idle -> {
                        //suppress
                    }

                    is State.Loading -> {
                        //suppress
                    }

                    is State.Success -> {
                        handleAddingTimeSuccess(state.success as? UpdateAddTimeSuccess)
                        handleCheckinSuccess(state.success as? CheckinSuccess)
                        handleCancelSuccess(state.success as? CancelSuccess)
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleAddingTimeSuccess(success: UpdateAddTimeSuccess?) {
        success?.let {
            ChangeProfileSuccessDialog().shows(parentFragmentManager)
            parentFragment?.setFragmentResult(MyParkingFragment::class.java.name, Bundle().apply {
                putBoolean(MyParkDetailFragment::class.java.name, true)
            })
            binding.pickedEndTime.apply {
                val timeDelimiter = text.split(":")
                val newEndTime = "${
                    (timeDelimiter.first()
                        .toInt() + success.addedTime.toInt())
                }:${timeDelimiter.last()}"
                text = newEndTime
                currentPark = currentPark?.copy(endTime = newEndTime)
            }
        }
    }

    private fun handleCheckinSuccess(success: CheckinSuccess?) {
        success?.let {
            if (currentPark!!.status == RentStatus.RENTING) {
                currentPark = currentPark?.copy(status = RentStatus.CHECKED_IN)
                binding.checkinButton.apply {
                    text = resources.getString(R.string.stop_rent)
                    backgroundTintList = resources.getColorStateList(
                        R.color.black, null
                    )
                }
                binding.parkCheckinLoading.indeterminateTintList = resources.getColorStateList(
                    when (currentPark!!.status) {
                        RentStatus.RENTING -> R.color.green
                        RentStatus.CHECKED_IN -> R.color.black
                        else -> R.color.green
                    },
                    null
                )
            } else {
                parentFragment?.setFragmentResult(
                    MyParkingFragment::class.java.name,
                    Bundle().apply {
                        putBoolean(
                            MyParkDetailFragment::class.java.name,
                            true
                        )
                    })
                findNavController().popBackStack()
            }
        }
    }

    private fun handleCancelSuccess(success: CancelSuccess?) {
        success?.let {
            parentFragment?.setFragmentResult(MyParkingFragment::class.java.name, Bundle().apply {
                putBoolean(MyParkDetailFragment::class.java.name, true)
            })
            findNavController().popBackStack()
        }
    }

    private fun handleCheckinLoading(isLoading: Boolean) {
        binding.checkinButton.hasVisible = !isLoading
        binding.parkCheckinLoading.hasVisible = isLoading
    }

    private fun handleCancelLoading(isLoading: Boolean) {
        binding.cancelButton.hasVisible = !isLoading
        binding.parkCancelLoading.hasVisible = isLoading
    }

    private fun handleAddingTimeError(error: UpdateAddTimeError?) {
        error?.let {
            binding.parkError.text = it.errorMessage
        }
    }

    private fun handleAddTimeLoading(isLoading: Boolean) {
        binding.myParkAddTimeButton.setTextColor(
            resources.getColor(
                if (isLoading) R.color.main_yellow
                else R.color.white, null
            )
        )
        binding.parkAddTimeLoading.hasVisible = isLoading
    }
}
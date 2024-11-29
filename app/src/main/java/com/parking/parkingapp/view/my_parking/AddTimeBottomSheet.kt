package com.parking.parkingapp.view.my_parking

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parking.parkingapp.R
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.BottomSheetAddTimeBinding
import com.parking.parkingapp.view.BaseDialog
import com.parking.parkingapp.view.map.formatCurrency

class AddTimeBottomSheet: BaseDialog<BottomSheetAddTimeBinding>() {
    private var currentTime = 0.0
    var maxTimeCanAdd: Int = 0
    var parkPrice: Int = 0
    var onAdd: ((Double) -> Unit)? = null
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetAddTimeBinding = BottomSheetAddTimeBinding.inflate(inflater, container, false)

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.colorTransparent)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onCreateCustomAnimation(): Int = R.style.BottomSheetDialogAnim

    override fun initView() {
        binding.totalAddHour.text = getString(
            R.string.total_time,
            currentTime.toString()
        )
    }

    override fun initActions() {
        binding.plus.setOnClickListener {
            currentTime = (currentTime + 1.0).also {
                binding.addTimeError.hasVisible = it > maxTimeCanAdd
            }.coerceAtMost(maxTimeCanAdd.toDouble())
            binding.totalAddHour.text = getString(
                R.string.total_time,
                currentTime.toString()
            )
            handlePriceHour(currentTime)
        }

        binding.minus.setOnClickListener {
            binding.addTimeError.hasVisible = false
            currentTime = (currentTime - 1.0).coerceAtLeast(0.0)
            binding.totalAddHour.text = getString(
                R.string.total_time,
                currentTime.toString()
            )
            handlePriceHour(currentTime)
        }
        binding.myParkAddTime.setOnClickListener {
            onAdd?.invoke(currentTime)
            dismiss()
        }
    }

    private fun handlePriceHour(
        addingTime: Double
    ) {
        binding.myParkAddTime.apply {
            text = getString(
                R.string.detail_checkout,
                formatCurrency(currentTime * parkPrice)
            ).uppercase()
            isEnabled = addingTime != 0.0 && addingTime <= maxTimeCanAdd
            backgroundTintList = resources.getColorStateList(
                if (isEnabled) R.color.main_yellow
                else R.color.gray, null
            )
        }
    }

    override fun shows(fm: FragmentManager): BaseDialog<BottomSheetAddTimeBinding> {
        kotlin.runCatching {
            if (!fm.isStateSaved) {
                show(fm, BottomSheetAddTimeBinding::class.java.name)
            }
        }
        return this
    }
}
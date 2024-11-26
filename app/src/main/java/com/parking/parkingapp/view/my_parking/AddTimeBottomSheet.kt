package com.parking.parkingapp.view.my_parking

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.mapbox.maps.extension.style.expressions.dsl.generated.max
import com.parking.parkingapp.R
import com.parking.parkingapp.databinding.BottomSheetAddTimeBinding
import com.parking.parkingapp.view.BaseDialog

class AddTimeBottomSheet: BaseDialog<BottomSheetAddTimeBinding>() {
    private var currentTime = 0.0
    var maxTimeCanAdd: Int = 0
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
            Log.e("tudm", "initActions: ${maxTimeCanAdd} ", )
            currentTime = (currentTime + 1.0).coerceAtMost(maxTimeCanAdd.toDouble())
            binding.totalAddHour.text = getString(
                R.string.total_time,
                currentTime.toString()
            )
        }

        binding.minus.setOnClickListener {
            currentTime = (currentTime - 1.0).coerceAtLeast(0.0)
            binding.totalAddHour.text = getString(
                R.string.total_time,
                currentTime.toString()
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
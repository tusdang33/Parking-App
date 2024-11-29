package com.parking.parkingapp.view.my_parking

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parking.parkingapp.R
import com.parking.parkingapp.databinding.DialogCancelParkBinding
import com.parking.parkingapp.view.BaseDialog

class CancelParkDialog: BaseDialog<DialogCancelParkBinding>() {
    var onConfirm: (() -> Unit)? = null
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogCancelParkBinding = DialogCancelParkBinding.inflate(inflater, container, false)

    override fun initView() {
        //suppress
    }

    override fun initActions() {
        binding.confirm.setOnClickListener {
            onConfirm?.invoke()
            dismiss()
        }
        binding.cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.colorTransparent)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun shows(fm: FragmentManager): BaseDialog<DialogCancelParkBinding> {
        kotlin.runCatching {
            if (!fm.isStateSaved) {
                show(fm, CancelParkDialog::class.java.name)
            }
        }
        return this
    }
}
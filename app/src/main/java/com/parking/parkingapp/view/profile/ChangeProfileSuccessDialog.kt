package com.parking.parkingapp.view.profile

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parking.parkingapp.R
import com.parking.parkingapp.databinding.DialogProfileSuccessBinding
import com.parking.parkingapp.view.BaseDialog

class ChangeProfileSuccessDialog : BaseDialog<DialogProfileSuccessBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogProfileSuccessBinding =
        DialogProfileSuccessBinding.inflate(inflater, container, false)

    override fun initView() {
        //suppress
    }

    override fun initActions() {
        binding.profileSuccessChange.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.colorTransparent)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun shows(fm: FragmentManager): BaseDialog<DialogProfileSuccessBinding> {
        kotlin.runCatching {
            if (!fm.isStateSaved) {
                show(fm, ChangeProfileSuccessDialog::class.java.name)
            }
        }
        return this
    }
}
package com.parking.parkingapp.view.map.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.parking.parkingapp.databinding.ParkingMarkerBinding

class ParkingMarker private constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {
    companion object {
        enum class STATUS(val color: String) {
            GOOD("#00871d"),
            NORMAL("#b07700"),
            BAD("#770000")
        }

        enum class SLOT(val color: String) {
            S100("#ff5555"),
            S75("#ffcc55"),
            S50("#55e8ff"),
            S0("#55ff7b")
        }
    }

    private val binding: ParkingMarkerBinding = ParkingMarkerBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    class Builder(val context: Context) {
        private var outerColor: String = STATUS.GOOD.color
        private var middleColor: String = SLOT.S100.color
        fun suggestStatus(status: STATUS) = apply { outerColor = status.color }

        fun slotStatus(slot: SLOT) = apply { middleColor = slot.color }

        fun build(): ParkingMarker {
            return ParkingMarker(context).apply {
                setOuterColor(outerColor)
                setMiddleColor(middleColor)
            }
        }
    }

    fun setOuterColor(color: String) {
        binding.markerOuter.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    fun setMiddleColor(color: String) {
        binding.markerMiddle.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    fun setMarkerNumber(number: Int) {
        binding.markerText.text = number.toString()
    }
}
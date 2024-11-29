package com.parking.parkingapp.view.map.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.parking.parkingapp.R
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.ParkingMarkerBinding
import com.parking.parkingapp.view.map.model.SmartPrioritize
import com.parking.parkingapp.view.map.roundToOneDecimal

class ParkingMarker private constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_COLOR = "#000000"
        enum class SubStatus(val color: String) {
            GOOD("#00871d"),
            NORMAL("#b07700"),
            BAD("#770000")
        }

        enum class Status {
            EXCELLENT,
            GOOD,
            NORMAL,
            BAD
        }
    }

    private val binding: ParkingMarkerBinding = ParkingMarkerBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    class Builder(val context: Context) {
        private var priority = SmartPrioritize.SLOT
        private var markerImage = R.drawable.top1_marker
        private var currentSlot = 0
        private var maxSlot = 0
        private var distance = 0.0
        private var price = 0

        fun setPrice(price: Int) = apply { this.price = price }
        fun setDistance(distance: Double) = apply { this.distance = distance / 1000 }
        fun setSlot(
            currentSlot: Int,
            maxSlot: Int
        ) = apply {
            this.currentSlot = currentSlot
            this.maxSlot = maxSlot
        }

        fun setPriority(prioritize: SmartPrioritize) = apply { priority = prioritize }
        fun suggestStatus(status: Status) = apply {
            markerImage =
                when (status) {
                    Status.EXCELLENT -> R.drawable.top1_marker
                    Status.GOOD -> R.drawable.top2_marker
                    Status.NORMAL -> R.drawable.top3_marker
                    Status.BAD -> R.drawable.top4_marker
                }
        }

        fun build(): ParkingMarker {
            return ParkingMarker(context).apply {
                setMarkerImage(markerImage)
                setMakerPriority(priority)
                setPrice(price)
                setDistance(distance)
                setSlot(currentSlot, maxSlot)
                setMarkerSubStatusColor(
                    if (priority == SmartPrioritize.SLOT) {
                        val ratio = currentSlot / maxSlot.toDouble()
                        if (ratio <= 0.3) {
                            SubStatus.GOOD.color
                        } else if (ratio > 0.3 && ratio < 0.7) {
                            SubStatus.NORMAL.color
                        } else if (ratio >= 0.7) {
                            SubStatus.BAD.color
                        } else {
                            SubStatus.NORMAL.color
                        }
                    } else {
                        DEFAULT_COLOR
                    }
                )
            }
        }
    }

    private fun setPrice(price: Int) {
        binding.price.text = resources.getString(R.string.priority_price_format, (price/1000).toString())
    }

    private fun setDistance(distance: Double) {
        binding.distance.text = distance.roundToOneDecimal().toString()
    }

    private fun setSlot(
        currentSlot: Int,
        maxSlot: Int
    ) {
        binding.currentSlot.text = currentSlot.toString()
        binding.maxSlot.text = maxSlot.toString()
    }

    private fun setMarkerSubStatusColor(subStatusColor: String) {
        val color = Color.parseColor(subStatusColor)
        binding.currentSlot.setTextColor(ColorStateList.valueOf(color))
        binding.slotDivider.backgroundTintList = ColorStateList.valueOf(color)
        binding.maxSlot.setTextColor(ColorStateList.valueOf(color))
        binding.distance.setTextColor(ColorStateList.valueOf(color))
        binding.price.setTextColor(ColorStateList.valueOf(color))
        binding.unit.setTextColor(ColorStateList.valueOf(color))
    }

    private fun setMakerPriority(smartPrioritize: SmartPrioritize) {
        binding.slotContainer.hasVisible = smartPrioritize == SmartPrioritize.SLOT
        binding.distanceContainer.hasVisible = smartPrioritize == SmartPrioritize.DISTANCE
        binding.priceContainer.hasVisible = smartPrioritize == SmartPrioritize.PRICE
    }

    private fun setMarkerImage(markerImage: Int) {
        binding.statusMarker.setImageResource(markerImage)
    }
}
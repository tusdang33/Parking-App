package com.parking.parkingapp.view.park

import android.annotation.SuppressLint
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.view.map.dateFormatter
import com.parking.parkingapp.view.map.formatTime
import com.parking.parkingapp.view.map.isCurrentTimeInRange
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@SuppressLint("NewApi")
fun Calendar.validatePickedTime(
    startTime: Calendar?,
    endTime: Calendar?,
    parkModel: ParkModel,
    isStartTime: Boolean
): Calendar? {
    val start = LocalTime.parse(formatTime(parkModel.openTime), dateFormatter)
    val close = LocalTime.parse(formatTime(parkModel.closeTime), dateFormatter)
    val pickedLocalTime = LocalTime.of(this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE))
    val pickedStartTime = startTime?.let {
        LocalTime.of(
            it.get(Calendar.HOUR_OF_DAY), it.get(
                Calendar.MINUTE
            )
        )
    }
    val pickedEndTime = endTime?.let {
        LocalTime.of(
            it.get(Calendar.HOUR_OF_DAY),
            it.get(Calendar.MINUTE)
        )
    }
    val isInTime = isCurrentTimeInRange(
        formatTime(parkModel.openTime),
        formatTime(parkModel.closeTime)
    )
    val now = LocalTime.now()
    val isOpen = isCurrentTimeInRange(start, close)
    return if (isStartTime) {
        when {
            pickedLocalTime.isBefore(start) -> {
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
            }

            !isCurrentTimeInRange(now, close, pickedLocalTime) -> {
                if (isOpen) {
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, now.hour)
                        set(Calendar.MINUTE, now.minute)
                    }
                } else {
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, start.hour)
                        set(Calendar.MINUTE, start.minute)
                    }
                }
            }

            pickedEndTime != null && pickedLocalTime.isAfter(pickedEndTime) -> {
                null
            }

            else -> this
        }
    } else {
        when {
            pickedLocalTime.isAfter(close) -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, close.hour)
                    set(Calendar.MINUTE, close.minute)
                }
            }

            pickedLocalTime.isBefore(now) || pickedLocalTime.isBefore(start) -> null

            pickedStartTime != null && pickedLocalTime <= pickedStartTime -> null

            else -> this
        }
    }
}
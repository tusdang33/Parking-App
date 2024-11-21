package com.parking.parkingapp.view.map

import android.annotation.SuppressLint
import android.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.StyleExtensionImpl
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.turf.TurfMeasurement
import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

fun StyleExtensionImpl.Builder.lineLayerPreset(
    layerId: String,
    layerSource: String,
    lineCap: LineCap = LineCap.ROUND,
    lineJoin: LineJoin = LineJoin.ROUND,
    lineColor: String = "#6070F0",
    lineWidth: Double = 6.0
): LineLayer = lineLayer(layerId, layerSource) {
    lineCap(lineCap)
    lineJoin(lineJoin)
    lineColor(Color.parseColor(lineColor))
    lineWidth(lineWidth)
}

fun Point.distanceTo(destination: Point): Double =
    TurfMeasurement.distance(this, destination, "meters")

fun formatCurrency(amount: Int): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount)}đ/h"
}

fun formatTime(hour: Double): String {
    val hours = hour.toInt()
    val minutes = ((hour - hours) * 60).toInt()
    val isPM = hours >= 12
    val formattedHours = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
    val formattedMinutes = minutes.toString().padStart(2, '0')
    val period = if (isPM) "PM" else "AM"

    return "$formattedHours:$formattedMinutes$period"
}

@SuppressLint("NewApi")
fun isCurrentTimeInRange(
    startTime: String,
    endTime: String
): Boolean {
    val formatter = DateTimeFormatter.ofPattern("h:mma")
    val start = LocalTime.parse(startTime, formatter)
    val end = LocalTime.parse(endTime, formatter)
    val now = LocalTime.now()

    return if (start.isBefore(end)) {
        now.isAfter(start) && now.isBefore(end)
    } else {
        now.isAfter(start) || now.isBefore(end)
    }
}

fun calculateDecimalTimeDifference(startTime: Calendar, endTime: Calendar): Double {
    val diffInMillis = endTime.timeInMillis - startTime.timeInMillis
    return diffInMillis.toDouble() / (1000 * 60 * 60)
}

fun Double.roundToOneDecimal(): Double {
    val decimalFormat = DecimalFormat("#.#")
    return decimalFormat.format(this).toDouble()
}
package com.parking.parkingapp.view.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
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
import java.util.Locale

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

fun formatCurrencyPerHour(amount: Int): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount)}đ/h"
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount)}đ"
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
    val formatter = DateTimeFormatter.ofPattern("h:mma", Locale.US)
    val start = LocalTime.parse(startTime, formatter)
    val end = LocalTime.parse(endTime, formatter)
    val now = LocalTime.now()

    return if (start.isBefore(end)) {
        now.isAfter(start) && now.isBefore(end)
    } else {
        now.isAfter(start) || now.isBefore(end)
    }
}

@SuppressLint("NewApi")
fun isCurrentTimeInRange(
    startTime: LocalTime,
    endTime: LocalTime,
    currentTime: LocalTime? = null
): Boolean {
    val now = currentTime ?: LocalTime.now()
    return if (startTime.isBefore(endTime)) {
        now.isAfter(startTime) && now.isBefore(endTime)
    } else {
        now.isAfter(startTime) || now.isBefore(endTime)
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

fun checkLocationPermission(
    context: Context,
    activity: Activity
) {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MapboxFragment.LOCATION_PERMISSION_REQUEST_CODE
        )
    }
}

fun requestGPS(
    activity: Activity,
    requestLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateDistanceMeters(1000F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build())
        .build()

    LocationServices.getSettingsClient(activity)
        .checkLocationSettings(settingsRequest)
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution)
                        .build()
                    requestLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
}

fun createBitmapFromView(
    view: View,
    divScale: Double = 2.5
): Bitmap {
    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        view.measuredWidth,
        view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return Bitmap.createScaledBitmap(
        bitmap,
        (view.measuredWidth / divScale).toInt(),
        (view.measuredHeight / divScale).toInt(),
        true
    )
}
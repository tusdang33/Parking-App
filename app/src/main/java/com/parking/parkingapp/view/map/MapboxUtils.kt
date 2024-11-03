package com.parking.parkingapp.view.map

import android.graphics.Color
import com.mapbox.maps.extension.style.StyleExtensionImpl
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin

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
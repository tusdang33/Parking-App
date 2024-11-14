package com.parking.parkingapp.view.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.parking.parkingapp.R
import com.parking.parkingapp.common.BDX
import com.parking.parkingapp.common.hideKeyboard
import com.parking.parkingapp.databinding.FragmentMapboxBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.ui.PDivierItemDecoration
import com.parking.parkingapp.view.map.ui.ParkingMarker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapboxFragment: BaseFragment<FragmentMapboxBinding>() {
    private val viewModel: MapViewModel by viewModels()

    companion object {
        private const val ZOOM_15F = 15.0
        private const val ZOOM_10F = 10.0
        private val DEFAULT_LOCATION = Point.fromLngLat(
            105.83592789795485,
            21.029691926637206
        )
        const val ROUTE_FOUND_LAYER_ID = "route-found-layer-id"
        const val ROUTE_FOUND_SOURCE_ID = "route-found-source-id"
        const val LOCATION_PERMISSION_REQUEST_CODE = 113

    }

    private val placeAutocompleteAdapter: PlaceAutocompleteAdapter by lazy {
        PlaceAutocompleteAdapter()
    }

    private var currentLocation: Point? = null
    private var markedLocation: Point? = null

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMapboxBinding = FragmentMapboxBinding.inflate(inflater, container, false)

    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        Log.d("Navigation", "${routeUpdateResult.navigationRoutes} ")
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object: MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
            }
        }
    )

    override fun initViews() {
        checkLocationPermission()
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
        }
        binding.mapView.apply {
            compass.enabled = false
            logo.enabled = false
            attribution.enabled = false
            scalebar.position = Gravity.BOTTOM or Gravity.START
            location.apply {
                locationPuck = LocationPuck2D(
                    topImage = ImageHolder.Companion.from(R.drawable.location_puck),
                    scaleExpression = interpolate {
                        linear()
                        zoom()
                        stop {
                            literal(0.0)
                            literal(0.6)
                        }
                        stop {
                            literal(20.0)
                            literal(1.0)
                        }
                    }.toJson()
                )
                enabled = true
                addOnIndicatorPositionChangedListener { position ->
                    currentLocation = position
                }
            }
        }
        binding.mapView.mapboxMap.apply {
            loadStyle(
                style(Style.MAPBOX_STREETS) {
                    +lineLayerPreset(ROUTE_FOUND_LAYER_ID, ROUTE_FOUND_SOURCE_ID)
                    +geoJsonSource(ROUTE_FOUND_SOURCE_ID)
                }
            ) {
                dumbMarker()
                updateCamera(currentLocation ?: DEFAULT_LOCATION)
            }
        }

        binding.mapSuggestRcv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = PDivierItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.rcv_divider)!!
            )
            addItemDecoration(dividerItemDecoration)
            adapter = placeAutocompleteAdapter
        }
    }

    private fun dumbMarker() {
        addAnnotationToMap(
            Point.fromLngLat(BDX.BDX1.long, BDX.BDX1.lat),
            createBitmapFromView(
                ParkingMarker.Builder(requireContext())
                    .slotStatus(ParkingMarker.Companion.SLOT.S100)
                    .suggestStatus(ParkingMarker.Companion.STATUS.GOOD)
                    .build()
            )
        )
        addAnnotationToMap(
            Point.fromLngLat(BDX.BDX2.long, BDX.BDX2.lat),
            createBitmapFromView(
                ParkingMarker.Builder(requireContext())
                    .slotStatus(ParkingMarker.Companion.SLOT.S0)
                    .suggestStatus(ParkingMarker.Companion.STATUS.BAD)
                    .build()
            )
        )
        addAnnotationToMap(
            Point.fromLngLat(BDX.BDX3.long, BDX.BDX3.lat),
            createBitmapFromView(
                ParkingMarker.Builder(requireContext())
                    .slotStatus(ParkingMarker.Companion.SLOT.S50)
                    .suggestStatus(ParkingMarker.Companion.STATUS.NORMAL)
                    .build()
            )
        )
    }

    override fun initActions() {
        binding.mapSearchRightIcon.setOnClickListener {
            if (binding.mapSuggestRcv.visibility == View.VISIBLE) {
                binding.mapSuggestRcv.visibility = View.GONE
            } else {
                if (currentLocation == null || markedLocation == null) return@setOnClickListener
                direction(
                    currentLocation!!, markedLocation!!
                )
            }
            binding.mapSearchEdt.setText("")
            placeAutocompleteAdapter.updateList(listOf())
            hideKeyboard()
        }
        placeAutocompleteAdapter.apply {
            setOnItemClick { point ->
                markedLocation = point
                transformRightIcon(true)
                goToLocation(
                    point,
                    ZOOM_10F,
                    true
                )
                updateList(listOf())
                transformRcv()
            }
        }
        binding.mapTrack.setOnClickListener {
            currentLocation?.let {
                goToLocation(
                    it,
                    ZOOM_15F,
                )
            }
        }
        binding.mapSearchEdt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //suppress
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                s?.let { query ->
                    if (query.isNotEmpty()) {
                        viewModel.getPlacePredictions(query.toString())
                        transformRightIcon(false)
                    } else {
                        transformRightIcon(true)
                        viewModel.cancelSearch()
                        placeAutocompleteAdapter.updateList(listOf())

                    }
                    transformRcv()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //suppress
            }
        })
    }

    override fun intiData() {
        //suppress
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.searchSuggestion.collect {
                placeAutocompleteAdapter.updateList(it)
                transformRcv()
            }
        }
    }

    private fun direction(
        start: Point,
        end: Point
    ) {
        val routeOptions =
            RouteOptions.builder()
                .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_CYCLING)
                .coordinatesList(listOf(start, end))
                .build()

        mapboxNavigation.requestRoutes(routeOptions, object: NavigationRouterCallback {
            override fun onCanceled(
                routeOptions: RouteOptions,
                routerOrigin: String
            ) {
                //suppress
            }

            override fun onFailure(
                reasons: List<RouterFailure>,
                routeOptions: RouteOptions
            ) {
                //suppress
            }

            override fun onRoutesReady(
                routes: List<NavigationRoute>,
                routerOrigin: String
            ) {
                drawRoute(
                    binding.mapView.mapboxMap,
                    routes.firstOrNull()?.directionsRoute?.geometry() ?: ""
                )
            }
        })
    }

    private fun drawRoute(
        mapboxMap: MapboxMap,
        geometry: String,
    ) {
        mapboxMap.getStyle {
            it.getSourceAs<GeoJsonSource>(ROUTE_FOUND_SOURCE_ID)
                ?.feature(
                    Feature.fromGeometry(
                        LineString.fromPolyline(geometry, Constants.PRECISION_6)
                    )
                )
        }
    }

    private fun transformRcv() {
        if (placeAutocompleteAdapter.itemCount == 0) {
            binding.mapSuggestRcv.visibility = View.GONE
        } else {
            binding.mapSuggestRcv.visibility = View.VISIBLE
        }
    }

    private fun transformRightIcon(isPrimary: Boolean) {
        binding.mapSearchRightIcon.apply {
            if (isPrimary) {
                visibility = if (markedLocation != null) {
                    VISIBLE
                } else {
                    GONE
                }
                setImageResource(R.drawable.gps)
            } else {
                visibility = VISIBLE
                setImageResource(R.drawable.close_icon)
            }
        }
    }

    private fun goToLocation(
        point: Point,
        zoom: Double = ZOOM_10F,
        isNeedAddMarker: Boolean = false
    ) {
        updateCamera(point, zoom)
        if (isNeedAddMarker) addAnnotationToMap(point)
    }

    private fun addAnnotationToMap(
        point: Point,
        markerView: Bitmap? = null
    ) {
        val markerIcon = markerView ?: run {
            val redMarker = BitmapFactory.decodeResource(
                resources,
                R.drawable.red_marker
            )
            Bitmap.createScaledBitmap(
                redMarker,
                redMarker.width / 3,
                redMarker.height / 3,
                true
            )
        }
        val pointAnnotationManager = binding.mapView.annotations.createPointAnnotationManager(
            AnnotationConfig()
        )
//        pointAnnotationManager.deleteAll()
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(markerIcon)
        pointAnnotationManager.create(pointAnnotationOptions)
    }

    private fun updateCamera(
        point: Point,
        zoom: Double = ZOOM_10F
    ) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1000L).build()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(zoom)
                .pitch(45.0)
                .padding(EdgeInsets(100.0, 0.0, 0.0, 0.0)).build(), mapAnimationOptions
        )
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun createBitmapFromView(
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
}
package com.parking.parkingapp.view.map

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.LayerPosition
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
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
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
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.common.hideKeyboard
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.databinding.FragmentMapboxBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.map.model.SmartParkModel
import com.parking.parkingapp.view.map.ui.PDivierItemDecoration
import com.parking.parkingapp.view.map.ui.ParkingMarker
import com.parking.parkingapp.view.my_parking.MyParkDetailFragment
import com.parking.parkingapp.view.park.ParkDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        const val ROAD_LAYER = "road-label"
        const val SYMBOL_LAYER_ID = "symbol_layer_id"
        const val SYMBOL_SOURCE_ID = "symbol_source_id"
        const val LOCATION_PERMISSION_REQUEST_CODE = 113

    }

    private val placeAutocompleteAdapter: PlaceAutocompleteAdapter by lazy {
        PlaceAutocompleteAdapter()
    }

    private var currentLocation: Point? = null
    private var markedLocation: Point? = null
    private var currentShowingPark: ParkModel? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var isRouteHasBeenDraw = false
    private var currentPriorityBlockHeight: Int? = null
    private var isOpeningClassifyBlock = true

    private val gpsRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(requireContext(), getString(R.string.gps_is_on), Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.gps_is_off), Toast.LENGTH_SHORT)
                .show()
        }
    }

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
        checkLocationPermission(requireContext(), requireActivity())
        requestGPS(
            requireActivity(),
            gpsRequestLauncher
        )
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
        }
        parentFragment?.setFragmentResultListener(MapboxFragment::class.java.name) { _, bundle ->
            if (bundle.getBoolean(ParkDetailFragment::class.java.name)) {
                if (currentLocation == null || currentShowingPark == null) return@setFragmentResultListener
                direction(
                    currentLocation!!,
                    Point.fromLngLat(currentShowingPark!!.long, currentShowingPark!!.lat)
                )
            }
            (bundle.getParcelable(MyParkDetailFragment::class.java.name) as? MyRentedPark)?.let {
                direction(
                    currentLocation!!,
                    Point.fromLngLat(it.park.long, it.park.lat)
                )
            }
        }
        binding.mapView.mapboxMap.apply {
            loadStyle(
                style(Style.MAPBOX_STREETS) {
                    +lineLayerPreset(ROUTE_FOUND_LAYER_ID, ROUTE_FOUND_SOURCE_ID)
                    +geoJsonSource(ROUTE_FOUND_SOURCE_ID)
                }
            ) {
                it.apply {
                    moveStyleLayer(ROUTE_FOUND_LAYER_ID, LayerPosition(null, ROAD_LAYER, null))
                }
                updateCamera(currentLocation ?: DEFAULT_LOCATION)
                subscribeCameraChanged {
                    viewModel.getParkInRange(cameraState.center)
                }
            }
        }
        binding.mapView.apply {
            pointAnnotationManager = annotations.createPointAnnotationManager(
                AnnotationConfig(
                    layerId = SYMBOL_LAYER_ID,
                    sourceId = SYMBOL_SOURCE_ID
                )
            )
            compass.enabled = false
            logo.enabled = false
            attribution.enabled = false
            scalebar.position = Gravity.BOTTOM or Gravity.START
            scalebar.isMetricUnits = true
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
                layerAbove = SYMBOL_LAYER_ID
                addOnIndicatorPositionChangedListener { position ->
                    if (currentLocation == null) {
                        currentLocation = position
                    } else if (currentLocation!!.distanceTo(position) > 5) {
                        updateRidingRoute(position)
                        currentLocation = position
                    }
                }
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

    override fun initActions() {
        binding.mapMenu.setOnClickListener {
            getDrawerMenu()?.open()
        }
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
                binding.mapSearchEdt.setText("")
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

        pointAnnotationManager?.apply {
            addClickListener(
                OnPointAnnotationClickListener { pointAnnotation ->
                    handleOnMarkerClick(pointAnnotation)
                    isRouteHasBeenDraw = false
                    markedLocation = pointAnnotation.point
                    transformRightIcon(true)
                    false
                }
            )
        }

        binding.parkInfoContainer.setOnClickListener {
            (activity as MainActivity).mainNavController()
                .navigate(R.id.parkDetailFragment, Bundle().apply {
                    putParcelable(ParkModel::class.java.name, currentShowingPark)
                    putString(
                        ParkModel::class.java.name + "distance",
                        binding.parkDistance.text.toString()
                    )
                })
        }

        handlePriorityAction()
    }

    private fun handlePriorityAction() {
        binding.mapPriorityClassify.setOnClickListener {
            if (currentPriorityBlockHeight == null)
                currentPriorityBlockHeight = binding.mapPriority.measuredHeight

            val animator = if (isOpeningClassifyBlock)
                ValueAnimator.ofInt(currentPriorityBlockHeight!!, dpToPx(52))
            else ValueAnimator.ofInt(dpToPx(52), currentPriorityBlockHeight!!)
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                val layoutParams = binding.mapPriority.layoutParams
                layoutParams.height = value
                binding.mapPriority.layoutParams = layoutParams
            }
            animator.duration = 300
            if (isOpeningClassifyBlock)
                animator.doOnStart {
                    for (i in 1 until binding.mapPriority.childCount) {
                        val childView = binding.mapPriority.getChildAt(i)
                        Log.e("tudm", "doOnStart:$childView ", )
                        childView.hasVisible = false
                    }
                }
            if (!isOpeningClassifyBlock)
                animator.doOnEnd {
                    for (i in 1 until binding.mapPriority.childCount) {
                        val childView = binding.mapPriority.getChildAt(i)
                        Log.e("tudm", "doOnEnd:$childView ", )
                        childView.hasVisible = true
                    }
                }
            isOpeningClassifyBlock = !isOpeningClassifyBlock
            animator.start()
        }
    }

    private fun handleOnMarkerClick(pointAnnotation: PointAnnotation) {
        val parkModel = runCatching {
            Gson().fromJson(pointAnnotation.getData(), ParkModel::class.java)
        }.getOrNull()
        if (currentLocation == null || parkModel == null) return
        currentShowingPark = parkModel
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        getOnRoadDistance(
            currentLocation!!,
            Point.fromLngLat(parkModel.long, parkModel.lat)
        ) { distance ->
            parkModel.let {
                Glide
                    .with(requireContext())
                    .load(it.image)
                    .centerCrop()
                    .placeholder(R.drawable.parking_placeholder)
                    .into(binding.parkImage)
                binding.parkName.text = it.name
                binding.parkAddress.text = it.address
                binding.parkPrice.text = formatCurrencyPerHour(it.pricePerHour)
                binding.parkDistance.text = getString(
                    R.string.distance_meter,
                    distance.toInt().toString()
                )
            }
            binding.parkInfoContainer.apply {
                hasVisible = true
                startAnimation(animation)
            }
        }
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
        scope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.parkInRange.collect { listPark ->
                    val result = mutableListOf<SmartParkModel>()
                    listPark.map { parkModel ->
                        getOnRoadDistance(
                            currentLocation!!,
                            Point.fromLngLat(parkModel.long, parkModel.lat)
                        ) {
                            result.add(SmartParkModel(parkModel, it, 0.0))
                            if (result.size == listPark.size) {
                                viewModel.smartClassifyPark(result)
                            }
                        }
                    }
                }
            }
        }
        scope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.smartPark.collect { listPark ->
                    pointAnnotationManager?.deleteAll()
                    listPark.divideIntoGroups().forEachIndexed { index, group ->
                        group.forEach { smartParkModel ->
                            addAnnotationToMap(
                                Point.fromLngLat(smartParkModel.park.long, smartParkModel.park.lat),
                                createBitmapFromView(
                                    ParkingMarker.Builder(requireContext())
                                        .setPrice(smartParkModel.park.pricePerHour)
                                        .setDistance(smartParkModel.distance)
                                        .setSlot(
                                            smartParkModel.park.currentSlot,
                                            smartParkModel.park.maxSlot
                                        )
                                        .suggestStatus(
                                            when (index) {
                                                0 -> ParkingMarker.Companion.STATUS.EXCELLENT
                                                1 -> ParkingMarker.Companion.STATUS.GOOD
                                                2 -> ParkingMarker.Companion.STATUS.NORMAL
                                                3 -> ParkingMarker.Companion.STATUS.BAD
                                                else -> ParkingMarker.Companion.STATUS.NORMAL
                                            }
                                        )
                                        .build()
                                ),
                                parkModel = smartParkModel.park
                            )
                        }
                    }
                }
            }
        }
    }

    private var updateRidingJob: Job? = null
    private fun updateRidingRoute(position: Point) {
        if (updateRidingJob?.isCompleted == false) return
        updateRidingJob = viewLifecycleOwner.lifecycleScope.launch {
            if (isRouteHasBeenDraw && markedLocation != null) {
                direction(position, markedLocation!!)
            }
            delay(1000L)
        }
    }

    private fun direction(
        start: Point,
        end: Point
    ) {
        val routeOptions =
            RouteOptions.builder()
                .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING)
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
                isRouteHasBeenDraw = true
                drawRoute(
                    binding.mapView.mapboxMap,
                    routes.firstOrNull()?.directionsRoute?.geometry() ?: ""
                )
            }
        })
    }

    private fun getOnRoadDistance(
        start: Point,
        end: Point,
        onDistance: (Double) -> Unit
    ) {
        val routeOptions =
            RouteOptions.builder()
                .applyDefaultNavigationOptions(DirectionsCriteria.PROFILE_DRIVING)
                .coordinatesList(listOf(start, end))
                .build()

        mapboxNavigation.requestRoutes(routeOptions, object: NavigationRouterCallback {
            override fun onCanceled(
                routeOptions: RouteOptions,
                routerOrigin: String
            ) {
                onDistance.invoke(start.distanceTo(end))
            }

            override fun onFailure(
                reasons: List<RouterFailure>,
                routeOptions: RouteOptions
            ) {
                onDistance.invoke(start.distanceTo(end))
            }

            override fun onRoutesReady(
                routes: List<NavigationRoute>,
                routerOrigin: String
            ) {
                onDistance.invoke(routes.firstOrNull()?.directionsRoute?.distance() ?: 0.0)
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
        markerView: Bitmap? = null,
        clearOldMarker: Boolean = false,
        parkModel: ParkModel? = null
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
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(markerIcon)

        parkModel?.let { pointAnnotationOptions.withData(Gson().toJsonTree(parkModel)) }
        pointAnnotationManager?.create(pointAnnotationOptions)
    }

    private fun updateCamera(
        point: Point,
        zoom: Double = ZOOM_10F
    ) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(
            if ((activity as MainActivity).isFirstTimeLogin) 1000L
            else 0L
        ).build()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(zoom)
                .pitch(45.0)
                .padding(EdgeInsets(100.0, 0.0, 0.0, 0.0)).build(), mapAnimationOptions
        )
        (activity as MainActivity).isFirstTimeLogin = false
    }
}
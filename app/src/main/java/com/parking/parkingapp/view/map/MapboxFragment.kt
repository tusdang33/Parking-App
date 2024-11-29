package com.parking.parkingapp.view.map

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
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
import com.google.gson.JsonNull
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
import com.parking.parkingapp.view.map.model.SmartPrioritize
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
        private const val ZOOM_12F = 12.0
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

    private var currentLocation: Point = DEFAULT_LOCATION
    private var markedLocation: Point? = null
    private var currentShowingPark: ParkModel? = null
    private var pointAnnotationManager: PointAnnotationManager? = null
    private var isRouteHasBeenDraw = false
    private var currentPriorityBlockHeight: Int = 0

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
                if (currentShowingPark == null) return@setFragmentResultListener
                direction(
                    currentLocation,
                    Point.fromLngLat(currentShowingPark!!.long, currentShowingPark!!.lat)
                )
            }
            (bundle.getParcelable(MyParkDetailFragment::class.java.name) as? MyRentedPark)?.let {
                direction(
                    currentLocation,
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
                updateCamera(currentLocation)
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
                    if (currentLocation == DEFAULT_LOCATION) {
                        currentLocation = position
                    } else if (currentLocation.distanceTo(position) > 5) {
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
                if (markedLocation == null) return@setOnClickListener
                direction(
                    currentLocation, markedLocation!!
                )
            }
            binding.mapSearchEdt.setText("")
            placeAutocompleteAdapter.updateList(listOf())
            hideKeyboard()
        }
        placeAutocompleteAdapter.apply {
            setOnItemClick { id, point ->
                binding.mapSearchEdt.setText("")
                markedLocation = point
                transformRightIcon(true)
                handleOnLocationClick(
                    parkModel = viewModel.smartPark.value.firstOrNull { it.park.id == id }?.park
                )
                goToLocation(
                    point,
                    ZOOM_12F,
                    true
                )
                updateList(listOf())
                transformRcv()
            }
        }
        binding.mapTrack.setOnClickListener {
            goToLocation(
                currentLocation,
                ZOOM_15F,
            )
        }
        binding.mapSearchEdt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
                //suppress
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
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
                    handleOnLocationClick(pointAnnotation)
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
        binding.mapPrioritySmart.setOnClickListener {
            viewModel.updateMapPrioritize(
                isSmart = !viewModel.mapPrioritize.value.isSmart
            )
        }
        binding.mapPriorityCapacity.setOnClickListener {
            viewModel.updateMapPrioritize(
                priority = SmartPrioritize.SLOT
            )
        }
        binding.mapPriorityDistance.setOnClickListener {
            viewModel.updateMapPrioritize(
                priority = SmartPrioritize.DISTANCE
            )
        }
        binding.mapPriorityPrice.setOnClickListener {
            viewModel.updateMapPrioritize(
                priority = SmartPrioritize.PRICE
            )
        }
        binding.mapPriorityClassify.setOnClickListener {
            viewModel.updateMapPrioritize(
                isOpening = !viewModel.mapPrioritize.value.isOpening
            )
        }
    }

    private fun handleOnLocationClick(pointAnnotation: PointAnnotation? = null, parkModel: ParkModel? = null) {
        val inMarkerParkModel = runCatching {
            Gson().fromJson(pointAnnotation?.getData(), ParkModel::class.java)
        }.getOrNull() ?: parkModel ?: return
        currentShowingPark = inMarkerParkModel
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        getOnRoadDistance(
            currentLocation, Point.fromLngLat(inMarkerParkModel.long, inMarkerParkModel.lat)
        ) { distance ->
            inMarkerParkModel.let {
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
                    listPark.forEach { parkModel ->
                        result.add(
                            SmartParkModel(
                                parkModel,
                                currentLocation.distanceTo(Point.fromLngLat(parkModel.long, parkModel.lat)),
                                0.0
                            )
                        )
                        if (result.size == listPark.size) {
                            viewModel.smartClassifyPark(result)
                        }
                        // getOnRoadDistance(
                        //     currentLocation,
                        //     Point.fromLngLat(parkModel.long, parkModel.lat)
                        // ) {
                        //     result.add(SmartParkModel(parkModel, it, 0.0))
                        //     if (result.size == listPark.size) {
                        //         viewModel.smartClassifyPark(result)
                        //     }
                        // }
                    }
                }
            }
        }
        scope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                var isOpening = false
                viewModel.mapPrioritize.collect { mapPrioritize ->
                    handlePrioritizeSmart(mapPrioritize.isSmart)
                    handleItemPrioritizeClick(mapPrioritize.priority)
                    if (isOpening != mapPrioritize.isOpening) {
                        isOpening = mapPrioritize.isOpening
                        handlePrioritizeBlockAnim(mapPrioritize.isOpening)
                    }
                }
            }
        }
        scope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.smartPark.collect { listPark ->
                    pointAnnotationManager?.delete(
                        pointAnnotationManager?.annotations?.filter {
                            it.getData() != null && it.getData() !is JsonNull
                        } ?: listOf()
                    )
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
                                        .setPriority(viewModel.mapPrioritize.value.priority)
                                        .suggestStatus(
                                            when (index) {
                                                0 -> ParkingMarker.Companion.Status.EXCELLENT
                                                1 -> ParkingMarker.Companion.Status.GOOD
                                                2 -> ParkingMarker.Companion.Status.NORMAL
                                                3 -> ParkingMarker.Companion.Status.BAD
                                                else -> ParkingMarker.Companion.Status.NORMAL
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

    private fun handlePrioritizeSmart(isSmart: Boolean) {
        binding.mapPrioritySmart.apply {
            backgroundTintList = if (!isSmart) resources.getColorStateList(
                R.color.white, null
            ) else null
            setImageResource(
                if (isSmart) R.drawable.smart
                else R.drawable.black_smart
            )
        }
    }

    private fun handlePrioritizeBlockAnim(isSetOpen: Boolean) {
        if (currentPriorityBlockHeight == 0) currentPriorityBlockHeight = binding.mapPriority.measuredHeight

        val animator = if (!isSetOpen) ValueAnimator.ofInt(currentPriorityBlockHeight, dpToPx(52))
        else ValueAnimator.ofInt(dpToPx(52), currentPriorityBlockHeight)
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = binding.mapPriority.layoutParams
            layoutParams.height = value
            binding.mapPriority.layoutParams = layoutParams
        }
        animator.duration = 300
        if (!isSetOpen) animator.doOnStart {
            for (i in 1 until binding.mapPriority.childCount) {
                val childView = binding.mapPriority.getChildAt(i)
                childView.hasVisible = false
            }
        }
        if (isSetOpen) animator.doOnEnd {
            for (i in 1 until binding.mapPriority.childCount) {
                val childView = binding.mapPriority.getChildAt(i)
                childView.hasVisible = true
            }
        }
        animator.start()
    }

    private fun handleItemPrioritizeClick(prioritize: SmartPrioritize) {
        binding.mapPriorityCapacity.apply {
            setTextColor(
                resources.getColor(
                    if (prioritize == SmartPrioritize.SLOT) R.color.black
                    else R.color.gray, null
                )
            )
            setTypeface(
                typeface, if (prioritize == SmartPrioritize.SLOT) Typeface.BOLD
                else Typeface.NORMAL
            )
        }
        binding.mapPriorityDistance.apply {
            setTextColor(
                resources.getColor(
                    if (prioritize == SmartPrioritize.DISTANCE) R.color.black
                    else R.color.gray, null
                )
            )
            setTypeface(
                typeface, if (prioritize == SmartPrioritize.DISTANCE) Typeface.BOLD
                else Typeface.NORMAL
            )
        }
        binding.mapPriorityPrice.apply {
            setTextColor(
                resources.getColor(
                    if (prioritize == SmartPrioritize.PRICE) R.color.black
                    else R.color.gray, null
                )
            )
            setTypeface(
                typeface, if (prioritize == SmartPrioritize.PRICE) Typeface.BOLD
                else Typeface.NORMAL
            )
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

        parkModel?.also {
            pointAnnotationOptions.withData(Gson().toJsonTree(parkModel))
        } ?: run {
            pointAnnotationManager?.delete(
                pointAnnotationManager?.annotations?.filter {
                    it.getData() == null || it.getData() is JsonNull
                } ?: listOf()
            )
        }
        pointAnnotationManager?.create(pointAnnotationOptions)
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
}
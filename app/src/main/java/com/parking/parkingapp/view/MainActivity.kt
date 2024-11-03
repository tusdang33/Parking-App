package com.parking.parkingapp.view

import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.parking.parkingapp.R
import com.parking.parkingapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: BaseActivity<ActivityMainBinding>() {

    init {
        lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                MapboxNavigationApp.attach(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                MapboxNavigationApp.detach(owner)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(applicationContext).build()
            }
        }

    }

    fun mainNavController() = findNavController(R.id.nav_host_fragment)
    fun menuNavController() = findNavController(R.id.drawer_menu)

    override fun inflateBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
}
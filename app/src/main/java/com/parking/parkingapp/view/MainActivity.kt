package com.parking.parkingapp.view

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.parking.parkingapp.R
import com.parking.parkingapp.common.hasVisible
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
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!findNavController(R.id.nav_host_fragment).popBackStack()) {
                    finish()
                }
            }
        })
    }

    fun mainNavController() = findNavController(R.id.nav_host_fragment)
    fun menuNavController() = findNavController(R.id.drawer_menu)

    fun setOnHeaderBack(onBack: (() -> Unit)? = null) {
        binding.headerBack.setOnClickListener {
            onBack?.invoke() ?: findNavController(R.id.nav_host_fragment).popBackStack()
        }
    }

    fun setHeaderTitle(title: String) {
        binding.headerTitle.text = title
    }

    fun isShowHeader(isShow: Boolean) {
        binding.header.hasVisible = isShow
    }


    override fun inflateBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
}
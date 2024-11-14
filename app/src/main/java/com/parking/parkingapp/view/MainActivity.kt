package com.parking.parkingapp.view

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.parking.parkingapp.R
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.databinding.ActivityMainBinding
import com.parking.parkingapp.view.drawer_menu.DrawerMenuFragment
import com.parking.parkingapp.view.history.HistoryFragment
import com.parking.parkingapp.view.map.MapboxFragment
import com.parking.parkingapp.view.my_parking.MyParkingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: BaseActivity<ActivityMainBinding>() {
    var isFirstTimeLogin = false

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
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.let {
            it.addOnBackStackChangedListener {
                handleOnScreenChange(it.fragments.lastOrNull())
            }
        }
    }

    private fun handleOnScreenChange(screen: Fragment?) {
        val drawerMenu = ((supportFragmentManager
            .findFragmentById(R.id.drawer_menu) as? NavHostFragment)
            ?.childFragmentManager
            ?.primaryNavigationFragment as? DrawerMenuFragment)

        drawerMenu?.changeButtonState(
            when (screen) {
                is MapboxFragment -> DrawerMenuFragment.ScreenType.MAP
                is HistoryFragment -> DrawerMenuFragment.ScreenType.HISTORY
                is MyParkingFragment -> DrawerMenuFragment.ScreenType.MY_PARKING
                else -> DrawerMenuFragment.ScreenType.MAP
            }
        )
    }

    fun mainNavController() = findNavController(R.id.nav_host_fragment)

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
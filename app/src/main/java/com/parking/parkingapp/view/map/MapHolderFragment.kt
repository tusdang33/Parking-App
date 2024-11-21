package com.parking.parkingapp.view.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.parking.parkingapp.R
import com.parking.parkingapp.view.MainActivity

class MapHolderFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_holder, container, false)
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).visibleMapScreen(true)
    }
    override fun onStop() {
        super.onStop()
        (activity as MainActivity).visibleMapScreen(false)
    }
}
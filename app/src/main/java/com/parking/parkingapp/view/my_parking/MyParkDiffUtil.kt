package com.parking.parkingapp.view.my_parking

import androidx.recyclerview.widget.DiffUtil
import com.parking.parkingapp.data.model.MyRentedPark

class MyParkDiffUtil(
    private val oldList: List<MyRentedPark>,
    private val newList: List<MyRentedPark>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
}
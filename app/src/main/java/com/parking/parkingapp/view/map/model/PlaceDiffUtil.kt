package com.parking.parkingapp.view.map.model

import androidx.recyclerview.widget.DiffUtil
import com.parking.parkingapp.data.model.AutoCompleteModel

class PlaceDiffUtil(
    private val oldList: List<AutoCompleteModel>,
    private val newList: List<AutoCompleteModel>
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
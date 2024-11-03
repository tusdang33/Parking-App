package com.parking.parkingapp.view.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mapbox.geojson.Point
import com.parking.parkingapp.data.model.AutoCompleteModel
import com.parking.parkingapp.databinding.SuggestItemBinding

class PlaceAutocompleteAdapter:
    RecyclerView.Adapter<PlaceAutocompleteAdapter.SuggestViewHolder>() {

    inner class SuggestViewHolder(private val binding: SuggestItemBinding):
        ViewHolder(binding.root) {
        fun bind(item: AutoCompleteModel) {
            binding.root.setOnClickListener {
                onItemClick?.invoke(item.coordinates)
            }
            binding.suggestText.text = item.addressName
            binding.suggestSubText.text = item.placeName
        }
    }

    private val suggestList: MutableList<AutoCompleteModel> = mutableListOf()
    private var onItemClick: ((Point) -> Unit)? = null
    fun setOnItemClick(onClick: (Point) -> Unit) {
        onItemClick = onClick
    }

    fun updateList(data: List<AutoCompleteModel>) {
        val diffCallback = object: DiffUtil.Callback() {
            override fun getOldListSize(): Int = suggestList.size

            override fun getNewListSize(): Int = data.size

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean = suggestList[oldItemPosition].id == data[oldItemPosition].id

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean = suggestList[oldItemPosition] == data[oldItemPosition]
        }
        suggestList.clear()
        suggestList.addAll(data)
        notifyDataSetChanged()
//        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SuggestViewHolder {
        return SuggestViewHolder(
            SuggestItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = suggestList.size

    override fun onBindViewHolder(
        holder: SuggestViewHolder,
        position: Int
    ) {
        holder.bind(suggestList[position])
    }
}
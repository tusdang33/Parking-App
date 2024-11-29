package com.parking.parkingapp.view.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mapbox.geojson.Point
import com.parking.parkingapp.common.hasVisible
import com.parking.parkingapp.data.model.AutoCompleteModel
import com.parking.parkingapp.databinding.SuggestItemBinding

class PlaceAutocompleteAdapter:
    RecyclerView.Adapter<PlaceAutocompleteAdapter.SuggestViewHolder>() {

    inner class SuggestViewHolder(private val binding: SuggestItemBinding):
        ViewHolder(binding.root) {
        fun bind(item: AutoCompleteModel) {
            binding.root.setOnClickListener {
                onItemClick?.invoke(item.id, item.coordinates)
            }
            binding.suggestText.text = item.addressName
            binding.suggestSubText.apply {
                if (item.placeName.isNotBlank()) {
                    hasVisible = true
                    text = item.placeName
                } else {
                    hasVisible = false
                }
            }
        }
    }

    private val suggestList: MutableList<AutoCompleteModel> = mutableListOf()
    private var onItemClick: ((String, Point) -> Unit)? = null
    fun setOnItemClick(onClick: (String, Point) -> Unit) {
        onItemClick = onClick
    }

    fun updateList(data: List<AutoCompleteModel>) {
        suggestList.clear()
        suggestList.addAll(data)
        notifyDataSetChanged()
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
package com.parking.parkingapp.view.my_parking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.databinding.MyParkingItemBinding
import com.parking.parkingapp.view.map.formatCurrencyPerHour

class MyParkingAdapter : RecyclerView.Adapter<MyParkingAdapter.MyParkingViewHolder>() {
    private val myParkingList = mutableListOf<MyRentedPark>()

    fun updateData(newList: List<MyRentedPark>) {
        myParkingList.clear()
        myParkingList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class MyParkingViewHolder(val binding: MyParkingItemBinding) : ViewHolder(binding.root) {
        fun bind(myRentedPark: MyRentedPark) {
            Glide
                .with(binding.root.context)
                .load(myRentedPark.park.image)
                .centerCrop()
                .placeholder(R.drawable.parking_placeholder)
                .into(binding.myParkImage)
            binding.myParkName.text = myRentedPark.park.name
            binding.myParkAddress.text = myRentedPark.park.address
            binding.myParkPrice.text = formatCurrencyPerHour(myRentedPark.totalPay)
            binding.myParkTime.text = binding.root.context.getString(
                R.string.my_park_time,
                myRentedPark.startTime, myRentedPark.endTime
            )
            binding.myParkDate.text = myRentedPark.rentedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyParkingViewHolder {
        return MyParkingViewHolder(
            MyParkingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = myParkingList.size

    override fun onBindViewHolder(holder: MyParkingViewHolder, position: Int) {
        holder.bind(myParkingList[position])
    }
}
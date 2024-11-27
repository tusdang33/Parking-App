package com.parking.parkingapp.view.my_parking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.parking.parkingapp.R
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.RentStatus
import com.parking.parkingapp.databinding.MyParkingItemBinding
import com.parking.parkingapp.view.map.formatCurrency

class MyParkingAdapter : RecyclerView.Adapter<MyParkingAdapter.MyParkingViewHolder>() {
    private val myParkingList = mutableListOf<MyRentedPark>()
    var onParkClick: ((MyRentedPark) -> Unit)? = null

    fun updateData(newList: List<MyRentedPark>) {
        myParkingList.clear()
        myParkingList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class MyParkingViewHolder(val binding: MyParkingItemBinding) : ViewHolder(binding.root) {
        fun bind(myRentedPark: MyRentedPark) {
            binding.root.setOnClickListener {
                onParkClick?.invoke(myRentedPark)
            }
            Glide
                .with(binding.root.context)
                .load(myRentedPark.park.image)
                .centerCrop()
                .placeholder(R.drawable.parking_placeholder)
                .into(binding.myParkImage)
            binding.myParkName.text = myRentedPark.park.name
            binding.myParkAddress.text = myRentedPark.park.address
            binding.myParkPrice.text = formatCurrency(myRentedPark.totalPay.toDouble())
            binding.myParkTime.text = binding.root.context.getString(
                R.string.my_park_time,
                myRentedPark.startTime, myRentedPark.endTime
            )
            binding.myParkDate.text = myRentedPark.rentedDate
            binding.myParkStatus.apply {
                text = binding.root.context.getString(
                    when(myRentedPark.status) {
                        RentStatus.RENTING -> R.string.not_checkin_yet
                        RentStatus.CHECKED_IN -> R.string.checked_in
                        RentStatus.RENTED -> R.string.empty
                    }
                )
                setTextColor(
                    binding.root.context.getColor(
                        if (myRentedPark.status != RentStatus.CHECKED_IN) R.color.gray
                        else R.color.green
                    )
                )
            }
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
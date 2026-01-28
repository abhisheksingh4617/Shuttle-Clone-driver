package com.shuttleclone.driver.ui.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.Model.PassengerDataItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.myLog

class PassengerListAdapter(
    val mContext: Context,
    val data: List<PassengerDataItem>?,
    val onCallClick: (String) -> Unit
) : RecyclerView.Adapter<PassengerListAdapter.ViewHolder>() {

    val TAG = "PassengerListAdapter"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layPassengerItem: LinearLayout = view.findViewById(R.id.layPassengerItem)
        val tvPassengerName: TextView = view.findViewById(R.id.tvPassengerName)
        val tvSeatNo: TextView = view.findViewById(R.id.tvSeatNo)
        val imgPassengerStatus: ImageView = view.findViewById(R.id.imgPassengerStatus)
        val imgPassengerInOutStatus: ImageView = view.findViewById(R.id.imgPassengerInOutStatus)

        // This is the important part for the orange button
        val layCallDriver: LinearLayout? = view.findViewById(R.id.layCallDriver)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.layout_passenger_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val passengerdata = data?.get(position) ?: return

        holder.apply {
            try {
                // 1. Resolve the Name (Checks user_fullname, then fullname, then name)
                val nameValue = passengerdata.userFullName
                    ?: passengerdata.fullname
                    ?: passengerdata.name
                    ?: "No Name"
                tvPassengerName.text = nameValue

                // 2. Resolve the Phone (Checks user_phone, then phone, then mobile)
                val phoneValue = passengerdata.userPhone
                    ?: passengerdata.phone
                    ?: passengerdata.mobile
                    ?: ""

                // Set Seat Number
                tvSeatNo.text = passengerdata.seat ?: "-"

                // 3. Handle the Call Button Click
                layCallDriver?.setOnClickListener {
                    if (phoneValue.isNotEmpty() && phoneValue != "null") {
                        myLog(TAG, "Calling passenger: $phoneValue")
                        onCallClick(phoneValue)
                    } else {
                        android.widget.Toast.makeText(mContext, "No phone number available", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                // Set Pickup/Drop Icons
                if (passengerdata.isPickup == true) {
                    imgPassengerInOutStatus.setImageResource(R.drawable.ic_seater)
                    imgPassengerInOutStatus.visibility = View.VISIBLE
                } else if (passengerdata.isDrop == true) {
                    imgPassengerInOutStatus.setImageResource(R.drawable.ic_bus_stop)
                    imgPassengerInOutStatus.visibility = View.VISIBLE
                } else {
                    imgPassengerInOutStatus.visibility = View.GONE
                }

                // Travel Status Color
                val color = if (passengerdata.travelStatus.equals("ONBOARDED", true))
                    R.color.color_check else R.color.dark_gray
                imgPassengerStatus.setBackgroundColor(ContextCompat.getColor(mContext, color))

            } catch (e: Exception) {
                myLog(TAG, "onBindViewHolder Error: ${e.message}")
            }
        }
    }




    override fun getItemCount(): Int = data?.size ?: 0
}
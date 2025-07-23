package com.shuttleclone.driver.ui.Adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.ui.Activity.StopsPassengerListActivity
import com.shuttleclone.driver.ui.Activity.ActiveRideDetailsActivity
import com.shuttleclone.driver.Model.StopsItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.myLog

class BusStopsAdapter(
    val mContext: Context,
    val listner: ActiveRideDetailsActivity,
    val stopsData: List<StopsItem>?,
    val routeId: String?,
    val bookingDate: String?
) : RecyclerView.Adapter<BusStopsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvBusStops = view.findViewById<CardView>(R.id.cvBusStops)
        val tvStopName = view.findViewById<TextView>(R.id.tvStopName)
        val tvStopTiming = view.findViewById<TextView>(R.id.tvStopTiming)
        val tvNoOfPassenger = view.findViewById<TextView>(R.id.tvNoOfPassenger)
        val tvNoOfOutPassenger = view.findViewById<TextView>(R.id.tvNoOfOutPassenger)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_bus_stops_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val data = stopsData!!.get(position)
            tvStopName.text = data.name
            tvNoOfPassenger.text = data.pickupCount.toString()
            tvNoOfOutPassenger.text = data.dropCount.toString()

            var stopTime=""

            if (position==0)stopTime= data.departureTime.toString()
            else stopTime= data.arrivalTime.toString()

            tvStopTiming.text = stopTime
            cvBusStops.setOnClickListener {
                try {
                    val intent = Intent(mContext, StopsPassengerListActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    val bundle = Bundle()
                    bundle.putSerializable("stopsData", data)
                    intent.putExtra("routeId", routeId)
                    intent.putExtra("stopTime", stopTime)
                    intent.putExtra("bookingDate", bookingDate)
                    intent.putExtras(bundle)

                    if(data.dropBookings!!.size>0&&data.pickupBookings!!.size>0)
                        passengerDetailsAlert(intent)
                    else if (data.pickupBookings!!.size>0){
                        intent.putExtra("type", "pickup")
                        mContext.startActivity(intent)
                    }else if (data.dropBookings!!.size>0){
                        intent.putExtra("type", "drop")
                        mContext.startActivity(intent)
                    }
                }catch (e:Exception){
                    myLog("BusStopsAdpt", "onBindViewHolder: Error=${e.localizedMessage}")}

            }
        }

    }

    fun passengerDetailsAlert(intent: Intent) {
        try {
            val dialog = Dialog(listner)
            dialog.setContentView(R.layout.choose_which_passanger_dialog)
            dialog.setCancelable(true)
            dialog.window?.setBackgroundDrawable(ColorDrawable(0))

            dialog.findViewById<View>(R.id.btnPickUp).setOnClickListener {
                if (dialog.isShowing) dialog.dismiss()
                intent.putExtra("type", "pickup")
                mContext.startActivity(intent)
            }
            dialog.findViewById<View>(R.id.btnDrop).setOnClickListener {
                if (dialog.isShowing) dialog.dismiss()
                intent.putExtra("type", "drop")
                mContext.startActivity(intent)
            }
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()

        } catch (e: Exception) {
            myLog("TAG", "passengerDetailsAlert: Error=${e.localizedMessage}")
        }
    }


    override fun getItemCount(): Int {
        return if (null!=stopsData){
            return if (stopsData.isNotEmpty())
                stopsData.size
            else 0
        }else 0
    }
}
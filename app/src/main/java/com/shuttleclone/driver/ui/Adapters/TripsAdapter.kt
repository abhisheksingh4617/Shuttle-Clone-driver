package com.shuttleclone.driver.ui.Adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.ui.Activity.ActiveRideDetailsActivity
import com.shuttleclone.driver.ui.Activity.BaseActivity
import com.shuttleclone.driver.ui.Fragments.HomeFragment
import com.shuttleclone.driver.Model.RoutesItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.currentTime
import com.shuttleclone.driver.Util.myLog

class TripsAdapter(
    val mContext: Context,
    val listener: HomeFragment,
    val routesList: List<RoutesItem>?,
    val assignId: String?,
    val tripStatus: String?
) : RecyclerView.Adapter<TripsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvTripsInfo = view.findViewById<CardView>(R.id.cvTripsInfo)
        val tvTripStatus = view.findViewById<TextView>(R.id.tvTripStatus)
        val tvBusCode = view.findViewById<TextView>(R.id.tvBusCode)
        val tvPassengers = view.findViewById<TextView>(R.id.tvPassengers)
        val tvCurrentTime = view.findViewById<TextView>(R.id.tvCurrentTime)
        val tvStartAt = view.findViewById<TextView>(R.id.tvStartAt)
        val tvEndAt = view.findViewById<TextView>(R.id.tvEndAt)
        val tvBusRoute = view.findViewById<TextView>(R.id.tvBusRoute)
        val tvTotalSeats = view.findViewById<TextView>(R.id.tvTotalSeats)
        val tvSeatLeft = view.findViewById<TextView>(R.id.tvSeatLeft)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.layout_trips_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            try {

                val data = routesList!![position]!!
                val bookingsInfo=data.bookingsInfo


                if (bookingsInfo!!.bus?.regNo!=null)
                    tvBusCode.text = bookingsInfo.bus?.regNo.toString()


                (mContext as BaseActivity).showView(tvTripStatus)
                tvTripStatus.text = tripStatus


                /*if (tripStatus.equals("ASSIGNED")) {
                    (mContext as BaseActivity).showView(tvTripStatus)
                    tvTripStatus.text = tripStatus
                } else (mContext as BaseActivity).hideView(tvTripStatus)*/

                tvPassengers.text = bookingsInfo.totalPassengers.toString()
                tvTotalSeats.text = bookingsInfo.totalSeats.toString()
                tvSeatLeft.text = bookingsInfo.totalSeatLeft.toString()
                tvBusRoute.text = bookingsInfo.routeName
                tvCurrentTime.text = currentTime()
                tvStartAt.text = "${data.bookingDate} ,${data.time}"
//            tvEndAt.text = data.ticketEndAt

                cvTripsInfo.setOnClickListener {
                    val intent = Intent(mContext, ActiveRideDetailsActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable("tripsData", data)
                    intent.putExtra("totalPassengers", bookingsInfo.totalPassengers.toString())
                    intent.putExtra("totalSeats", bookingsInfo.totalSeats.toString())
                    intent.putExtra("dateTime", "${data.bookingDate} ,${data.time}")
                    intent.putExtra("routeId", data.busScheduleId.toString())
                    intent.putExtra("assignId", assignId)
                    intent.putExtra("tripStatus", tripStatus)
                    intent.putExtras(bundle)
                    mContext.startActivity(intent)

                    /* if (data.tripStatus.equals("ASSIGNED")) {
                         val intent = Intent(mContext, ActiveRideDetailsActivity::class.java)
                         val bundle = Bundle()
                         bundle.putSerializable("tripsData", routesList)
                         intent.putExtras(bundle)
                         mContext.startActivity(intent)
                     }*/
                }

            } catch (e: Exception) {
                myLog("TAG", "TripsAdapter-> onBindViewHolder: Error=${e.localizedMessage}")
            }
        }
    }

    override fun getItemCount(): Int {
        return if (null != routesList) {
            return if (routesList.isNotEmpty())
                routesList.size
            else 0
        } else 0

    }
}
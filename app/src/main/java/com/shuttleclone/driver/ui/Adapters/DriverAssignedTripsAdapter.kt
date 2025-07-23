package com.shuttleclone.driver.ui.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.ui.Fragments.HomeFragment
import com.shuttleclone.driver.Model.TripsDataItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.RunLayoutAnimation

class DriverAssignedTripsAdapter(
    val mContext: Context,
    val listener: HomeFragment,
    val tripList: List<TripsDataItem>?
) : RecyclerView.Adapter<DriverAssignedTripsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTripTitle = view.findViewById<TextView>(R.id.tvTripTitle)
        val rvDriverTrips = view.findViewById<RecyclerView>(R.id.rvDriverTrips)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.layout_driver_trips, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {

            if (null != tripList) {
                val data = tripList!![position]!!
                data?.let {
                    if (data.routes!!.size > 0) {
                        tvTripTitle.text = data.routeName
                        val tripsAdapter = TripsAdapter(
                            mContext,
                            listener,
                            data.routes,
                            data.assignId,
                            data.tripStatus
                        )
                        rvDriverTrips!!.apply {
                            layoutManager =
                                LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                            setHasFixedSize(true)
                            adapter = tripsAdapter
                        }
                        RunLayoutAnimation(mContext, rvDriverTrips!!)
                    } else {
                        tvTripTitle.visibility = View.GONE
                    }

                }

            }


        }
    }

    override fun getItemCount(): Int {
        return if (null != tripList) {
            return if (tripList.isNotEmpty())
                tripList.size
            else 0
        } else 0

    }
}
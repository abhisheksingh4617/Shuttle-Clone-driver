package com.shuttleclone.driver.ui.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.Model.StopsItem
import com.shuttleclone.driver.ui.Activity.BusRoutesActivity
import com.shuttleclone.driver.R


class ViewStopsAdapter(val context: BusRoutesActivity, val stopsData: List<StopsItem>?) :
    RecyclerView.Adapter<ViewStopsAdapter.ViewHolder>() {

    private var nextStopTitle: String? = ""

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgStopStatus = view.findViewById<ImageView>(R.id.imgStopStatus)
        val tvStopTitle = view.findViewById<TextView>(R.id.tvStopTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.stop_status_adapter_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            tvStopTitle.text = stopsData!!.get(position)!!.name

            imgStopStatus.setColorFilter(
                ContextCompat.getColor(context, R.color.darkgreen),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )

            if (nextStopTitle.equals(stopsData!!.get(position)!!.name)) {
                tvStopTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.darkgreen))
            } else {
                tvStopTitle.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent_color))
            }

            /* if (stopsData!!.get(position)!!.pickup!!)
                 imgStopStatus.setColorFilter(ContextCompat.getColor(context, R.color.darkgreen), android.graphics.PorterDuff.Mode.MULTIPLY)
             else if (stopsData!!.get(position)!!.drop!!)
                 imgStopStatus.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.MULTIPLY)*/
        }

    }

    override fun getItemCount(): Int {
        return if (null!=stopsData){
            return if (stopsData.isNotEmpty())
                stopsData.size
            else 0
        }else 0
    }

    fun setNextStop(nextStopTitle: String) {
        this.nextStopTitle = nextStopTitle
    }
}
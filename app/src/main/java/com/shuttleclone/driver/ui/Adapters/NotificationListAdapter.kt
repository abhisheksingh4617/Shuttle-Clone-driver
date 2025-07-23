package com.shuttleclone.driver.ui.Adapters

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.ui.Activity.NotificationActivity
import com.shuttleclone.driver.Model.NotificationsDataItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.clickWithThrottle
import com.shuttleclone.driver.Util.myLog

class NotificationListAdapter(
    val mContext: Context,
    val listner: NotificationActivity,
    val notificationData: List<NotificationsDataItem>
) : RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {

    val TAG="NotificationAdaptor"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvNotifications = view.findViewById<CardView>(R.id.cvNotifications)
        val tvNotificationTitle = view.findViewById<TextView>(R.id.tvNotificationTitle)
        val tvNotificationBody = view.findViewById<TextView>(R.id.tvNotificationBody)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val viewReadIndicator = view.findViewById<View>(R.id.viewReadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            try {
                tvNotificationTitle.text=notificationData.get(position)!!.title
                tvNotificationBody.text=notificationData.get(position)!!.content
                tvDate.text=notificationData.get(position)!!.createdAt

                viewReadIndicator.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent))

                /*cvNotifications.clickWithThrottle {
                    if (notificationData.get(position).read!=1)listner.updateNotificationStatus(notificationData.get(position).id)
                       openNotification(notificationData.get(position))
                }*/
            }catch (e:Exception){
                myLog(TAG, "onBindViewHolder: Error=${e.localizedMessage}")}

        }

    }

    private fun openNotification(data: NotificationsDataItem) {
        try {
            val view: View = listner.layoutInflater.inflate(R.layout.default_dailog_layout, null)
            val dialog = Dialog(listner, R.style.CustomBottomSheetDialogTheme)

            var title = data.content
            var body = data.content

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.findViewById<TextView>(R.id.tvHeading).text = Html.fromHtml(
                    title,
                    Html.FROM_HTML_MODE_COMPACT
                )
                view.findViewById<TextView>(R.id.tvBody).text = Html.fromHtml(
                    body,
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                view.findViewById<TextView>(R.id.tvHeading).text = Html.fromHtml(title)
                view.findViewById<TextView>(R.id.tvBody).text = Html.fromHtml(body)
            }

            view.findViewById<ImageView>(R.id.imgClose)?.clickWithThrottle {
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()

        }catch (e:Exception){
            myLog(TAG, "openNotification: Error=${e.localizedMessage}")}
    }

    override fun getItemCount(): Int {
        return if (null!=notificationData){
            return if (notificationData.isNotEmpty())
                notificationData.size
            else 0
        }else 0
    }
}
package com.shuttleclone.driver.ui.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shuttleclone.driver.Model.PassengerDataItem
import com.shuttleclone.driver.R
import com.shuttleclone.driver.ui.Adapters.PassengerListAdapter
import java.util.ArrayList

class PassengerListFragment : Fragment() {

    companion object {
        var mTitle = "Passenger List"
    }

    private var rvPassengerList: RecyclerView? = null
    private var passengerListAdapter: PassengerListAdapter? = null
    private var passengerDataList = ArrayList<PassengerDataItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_passenger_list, container, false)
        initView(view)
        setupRecyclerView()
        return view
    }

    private fun initView(view: View) {
        rvPassengerList = view.findViewById(R.id.rvPassengerList)
    }

    private fun setupRecyclerView() {
        passengerListAdapter = PassengerListAdapter(requireContext(), passengerDataList) { phoneNumber ->
            // IMPROVEMENT: Check for both null/empty and the literal string "null"
            // which often comes from backend APIs
            if (phoneNumber.isNullOrEmpty() || phoneNumber == "null") {
                Toast.makeText(context, "Phone number not available for this passenger", Toast.LENGTH_SHORT).show()
            } else {
                makePhoneCall(phoneNumber)
            }
        }

        rvPassengerList?.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = passengerListAdapter
        }
    }

    private fun makePhoneCall(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun updateData(newData: List<PassengerDataItem>?) {
        // SAFETY: Check if newData is null before clearing the list
        if (newData == null) return

        passengerDataList.clear()
        passengerDataList.addAll(newData)

        activity?.runOnUiThread {
            // Check if adapter is null before notifying
            passengerListAdapter?.notifyDataSetChanged()

            // LOGIC CHECK: If list is still empty, show a Toast or empty view
            if (passengerDataList.isEmpty()) {
                // You could show an empty state image here
            }
        }
    }
}
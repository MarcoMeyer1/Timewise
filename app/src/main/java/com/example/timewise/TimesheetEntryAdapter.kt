package com.example.timewise

import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TimesheetEntryAdapter(
    private val entries: MutableList<TimesheetEntry>,
    private val itemClickListener: (TimesheetEntry) -> Unit
) : RecyclerView.Adapter<TimesheetEntryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.eventName)
        val eventDate: TextView = view.findViewById(R.id.eventDate)
        val photoIcon: ImageView = view.findViewById(R.id.photoIcon)
    }

    fun updateEntries(newEntries: List<TimesheetEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()  // Notify the RecyclerView that the data has changed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_entry_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.eventName.text = entry.name
        holder.eventDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entry.startDate.time)

        if (entry.photo != null) {
            holder.photoIcon.visibility = View.VISIBLE
        } else {
            holder.photoIcon.visibility = View.GONE
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            Log.d("RecyclerView", "Item clicked: ${entry.name}")
            itemClickListener(entry)
        }
    }

    override fun getItemCount() = entries.size
}

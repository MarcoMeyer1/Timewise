package com.example.timewise

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TimeSheetAdapter(var timesheets: List<Timesheet>) : RecyclerView.Adapter<TimeSheetAdapter.ViewHolder>() {

    private var entries: List<TimesheetEntry> = arrayListOf()

    fun updateEntries(newEntries: List<TimesheetEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timesheet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = entries.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(entry: TimesheetEntry) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateTextView.text = dateFormat.format(entry.startDate.time)
            eventNameTextView.text = entry.name

            // Retrieve the color based on the timesheet's name
            val colorHex = timesheets.find { it.name == entry.category }?.colorHex ?: "#FFFFFF"
            cardView.setCardBackgroundColor(Color.parseColor(colorHex))
        }
    }
}

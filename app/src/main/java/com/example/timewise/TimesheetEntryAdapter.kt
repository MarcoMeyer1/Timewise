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

class TimesheetEntryAdapter(
    private var entries: MutableList<TimesheetManager.TimesheetEntry>,
    private val itemClickListener: (TimesheetManager.TimesheetEntry) -> Unit
) : RecyclerView.Adapter<TimesheetEntryAdapter.ViewHolder>() {

    fun updateEntries(newEntries: List<TimesheetManager.TimesheetEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timesheet_entry_item, parent, false)
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

        fun bind(entry: TimesheetManager.TimesheetEntry) {
            eventNameTextView.text = entry.name
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateTextView.text = dateFormat.format(entry.startDate.timeInMillis)
            val color = "#FF5733" // You can change this to fetch color dynamically
            cardView.setCardBackgroundColor(Color.parseColor(color))

            itemView.setOnClickListener {
                itemClickListener(entry)
            }
        }
    }
}
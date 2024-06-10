package com.example.timewise

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TimeSheetAdapter(private var timesheets: MutableList<TimesheetManager.Timesheet>) : RecyclerView.Adapter<TimeSheetAdapter.ViewHolder>() {
    private var entries: MutableList<TimesheetManager.TimesheetEntry> = mutableListOf()
    private var onTimesheetEditListener: OnTimesheetEditListener? = null

    fun setOnTimesheetEditListener(listener: OnTimesheetEditListener) {
        this.onTimesheetEditListener = listener
    }

    fun updateEntries(newEntries: List<TimesheetManager.TimesheetEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    fun updateTimesheets(newTimesheets: List<TimesheetManager.Timesheet>) {
        timesheets.clear()
        timesheets.addAll(newTimesheets)
        notifyDataSetChanged()
    }

    fun getTimesheets(): List<TimesheetManager.Timesheet> {
        return timesheets
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timesheet = timesheets[position]
        holder.bind(timesheet)
        Log.d("TimeSheetAdapter", "Binding timesheet: ${timesheet.name}")
    }

    override fun getItemCount(): Int = timesheets.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timesheetNameTextView: TextView = itemView.findViewById(R.id.timesheet_name)
        private val editButton: ImageView = itemView.findViewById(R.id.btnEdit)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(timesheet: TimesheetManager.Timesheet) {
            // Set the timesheet name
            timesheetNameTextView.text = timesheet.name

            // Use a default color if color is empty or invalid
            val color = if (timesheet.color.isNullOrEmpty()) "#FFFFFF" else timesheet.color
            cardView.setCardBackgroundColor(Color.parseColor(color))

            // Set click listener for editing timesheet
            itemView.setOnClickListener {
                onTimesheetEditListener?.onEditClicked(timesheet)
            }

            // Set click listener for edit button
            editButton.setOnClickListener {
                onTimesheetEditListener?.onEditClicked(timesheet)
            }
        }
    }

    interface OnTimesheetEditListener {
        fun onEditClicked(timesheet: TimesheetManager.Timesheet)
    }
}

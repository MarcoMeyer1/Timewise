package com.example.timewise

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ActiveTimesheetAdapter(
    private val timesheets: List<Timesheet>,
    private val onEditListener: OnTimesheetEditListener
) : RecyclerView.Adapter<ActiveTimesheetAdapter.TimesheetViewHolder>() {

    interface OnTimesheetEditListener {
        fun onEditClicked(timesheet: Timesheet)
    }

    inner class TimesheetViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        val nameTextView: TextView = cardView.findViewById(R.id.timesheet_name)
        val editButton: ImageView = cardView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_card, parent, false) as CardView
        return TimesheetViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: TimesheetViewHolder, position: Int) {
        val timesheet = timesheets[position]
        holder.nameTextView.text = timesheet.name
        holder.cardView.setCardBackgroundColor(Color.parseColor(timesheet.colorHex))

        // Set edit button click listener
        holder.editButton.setOnClickListener {
            onEditListener.onEditClicked(timesheet)
        }

        // Set card view click listener to navigate to new activity
        holder.cardView.setOnClickListener {
            val context = holder.cardView.context
            val intent = Intent(context, TimesheetDetails::class.java)
            intent.putExtra("timesheetId", timesheet.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = timesheets.size
}

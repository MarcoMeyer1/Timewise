package com.example.timewise

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TimesheetDetails : AppCompatActivity() {

    private lateinit var timesheetNameTextView: TextView
    private lateinit var entryCountTextView: TextView
    private lateinit var totalHoursTextView: TextView
    private lateinit var dbManager: DatabaseOperationsManager
    private lateinit var btnDeleteTimesheet: Button
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheet_details)

        btnDeleteTimesheet = findViewById(R.id.btnDeleteTimesheet)

        timesheetNameTextView = findViewById(R.id.timesheet_name)
        entryCountTextView = findViewById(R.id.entry_count)
        totalHoursTextView = findViewById(R.id.total_hours)

        dbManager = DatabaseOperationsManager(this)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val timesheetId = intent.getStringExtra("timesheetId")
        if (timesheetId != null) {
            fetchAndDisplayTimesheet(timesheetId)
        }

        btnDeleteTimesheet.setOnClickListener {
            val timesheetId = intent.getStringExtra("timesheetId")
            if (timesheetId != null) {
                TimesheetManager.deleteTimesheet(this,timesheetId)
                finish()
            }
        }
    }

    private fun fetchAndDisplayTimesheet(timesheetId: String) {
        val db = FirebaseDatabase.getInstance()
        dbManager.fetchTimesheets(db, userId) { timesheets, _ ->
            val timesheet = timesheets.find { it.id == timesheetId }
            if (timesheet != null) {
                displayTimesheetDetails(timesheet)
            }
        }
    }

    private fun displayTimesheetDetails(timesheet: TimesheetManager.Timesheet) {
        timesheetNameTextView.text = timesheet.name
        entryCountTextView.text = "Number of Entries: ${timesheet.entries.size}"

        val totalHours = timesheet.entries.sumBy {
            val start = it.startDate
            val end = it.endDate
            ((end.timeInMillis - start.timeInMillis) / (1000 * 60 * 60)).toInt()
        }

        totalHoursTextView.text = "Total Hours Spent: $totalHours"
    }


}

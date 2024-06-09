package com.example.timewise

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class EventsCalenderView : BaseActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeSheetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_calender_view)
        setupUI()
        updateToolbarColor("#F479FF")
    }

    private fun setupUI() {
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.timelineRecyclerView)
        // Initialize with an empty mutable list
        adapter = TimeSheetAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadTimesheets()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            updateEntries(selectedDate)
        }

        findViewById<Button>(R.id.btnAddEvent).setOnClickListener {
            EventCreationDialogFragment().show(supportFragmentManager, "createEvent")
        }
    }

    private fun loadTimesheets() {
        TimesheetManager.fetchTimesheets { timesheets ->
            adapter.updateTimesheets(timesheets)
            updateEntries(Calendar.getInstance())
        }
    }

    private fun updateEntries(selectedDate: Calendar) {
        val userId = TimesheetManager.getAuth().currentUser?.uid ?: return
        val db = TimesheetManager.getDatabase()
        val timesheetId = adapter.getTimesheets().firstOrNull()?.id ?: return

        DatabaseOperationsManager(this).fetchTimesheetEntriesBetweenDates(
            db,
            userId,
            timesheetId,
            selectedDate.timeInMillis,
            selectedDate.timeInMillis
        ) { entries ->
            val filteredEntries = entries.filter {
                it.startDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        it.startDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                        it.startDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
            }
            adapter.updateEntries(filteredEntries)
        }
    }
}

package com.example.timewise

import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class EventsCalendarView : BaseActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimesheetEntryAdapter
    private val databaseOperationsManager = DatabaseOperationsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_calender_view)
        setupUI()
        updateToolbarColor("#F479FF")
    }

    private fun setupUI() {
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.timelineRecyclerView)
        adapter = TimesheetEntryAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            updateEntries(selectedDate)
        }

        findViewById<Button>(R.id.btnAddEvent).setOnClickListener {
            EventCreationDialogFragment().show(supportFragmentManager, "createEvent")
        }
    }

    private fun updateEntries(selectedDate: Calendar) {
        val db = FirebaseDatabase.getInstance()
        databaseOperationsManager.fetchAllTimesheetEntriesForUser(db) { entries ->
            val filteredEntries = entries.filter {
                val entryDate = Calendar.getInstance().apply { timeInMillis = it.startDate }
                entryDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        entryDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                        entryDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
            }
            runOnUiThread {
                Log.d("EventsCalendarView", "Filtered entries: $filteredEntries")
                adapter.updateEntries(filteredEntries)
            }
        }
    }
}
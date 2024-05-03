package com.example.timewise

import EventCreationDialogFragment
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.net.Uri
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventsCalenderView : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeSheetAdapter
    private lateinit var allEntries: List<TimesheetEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_events_calender_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        calendarView = findViewById<CalendarView>(R.id.calendarView)
        recyclerView = findViewById<RecyclerView>(R.id.timelineRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TimeSheetAdapter()
        recyclerView.adapter = adapter

        val allTimesheets = getAllTimeSheets()

        allEntries = TimesheetManager.aggregateTimeSheetEntries(allTimesheets)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val nextTwoWeeks = getNextTwoWeeks(selectedDate.time)
            val filteredEntries = allEntries.filter { TimesheetEntry -> TimesheetEntry.startDate in nextTwoWeeks }
            adapter.updateEntries(filteredEntries)
        }

        val currentDate = Calendar.getInstance()
        val nextTwoWeeks = getNextTwoWeeks(currentDate.time)
        val filteredEntries = allEntries.filter { TimesheetEntry -> TimesheetEntry.startDate in nextTwoWeeks }
        adapter.updateEntries(filteredEntries)

        val btnAddEvent: Button = findViewById(R.id.btnAddEvent)
        btnAddEvent.setOnClickListener {

            EventCreationDialogFragment().show(supportFragmentManager, "createEvent")
        }
    }

    private fun getNextTwoWeeks(date: Date): List<String> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val nextTwoWeeks = ArrayList<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until 14) {
            nextTwoWeeks.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return nextTwoWeeks
    }


}



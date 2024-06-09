package com.example.timewise

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class EventsCalenderView : BaseActivity() {

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
        adapter = TimesheetEntryAdapter(mutableListOf()) { entry -> handleEntryClick(entry) }
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
                val entryDate = Calendar.getInstance().apply { timeInMillis = it.startDate.timeInMillis }
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

    private fun handleEntryClick(entry: TimesheetManager.TimesheetEntry) {
        entry.photo?.let { photoUri ->
            showPhoto(photoUri)
        } ?: run {
            Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPhoto(photoUri: Uri) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_photo_viewer)
        val photoView: ImageView = dialog.findViewById(R.id.photoView)
        photoView.setImageURI(photoUri)
        dialog.show()
    }
}


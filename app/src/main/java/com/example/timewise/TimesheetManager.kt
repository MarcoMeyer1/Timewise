package com.example.timewise

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.Calendar

object TimesheetManager {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser: FirebaseUser? = auth.currentUser

    data class Timesheet(
        val id: String,
        var name: String,
        var colorHex: String,
        var entries: MutableList<TimesheetEntry> = mutableListOf()
    )

    data class TimesheetEntry(
        val name: String,
        val startDate: Calendar,
        val endDate: Calendar,
        val isAllDay: Boolean,
        val category: String?,
        val photo: Uri?
    )

    fun addTimesheet(timesheet: Timesheet) {
        currentUser?.uid?.let { userId ->
            database.getReference("timesheets/$userId").push().setValue(timesheet)
        }
    }

    fun addTimesheetEntry(timesheetId: String, timesheetEntry: TimesheetEntry) {
        currentUser?.uid?.let { userId ->
            database.getReference("timesheets/$userId/$timesheetId/entries").push().setValue(timesheetEntry)
        }
    }

    fun fetchTimesheets(completion: (List<Timesheet>) -> Unit) {
        currentUser?.uid?.let { userId ->
            val timesheetsRef = database.getReference("timesheets/$userId")
            timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timesheets = mutableListOf<Timesheet>()
                    for (childSnapshot in snapshot.children) {
                        val timesheet = childSnapshot.getValue(Timesheet::class.java)
                        timesheet?.let { timesheets.add(it) }
                    }
                    completion(timesheets)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TimesheetManager", "Error fetching timesheets: $error")
                    completion(emptyList())
                }
            })
        }
    }


}

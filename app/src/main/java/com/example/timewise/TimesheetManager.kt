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
        val id: String = "",
        var name: String = "",
        var color: String = "",
        var entries: MutableList<TimesheetEntry> = mutableListOf()
    )

    data class TimesheetEntry(
        val name: String = "",
        val startDate: Calendar = Calendar.getInstance(),
        val endDate: Calendar = Calendar.getInstance(),
        val isAllDay: Boolean = false,
        val category: String? = null,
        val photo: Uri? = null
    )


    fun addTimesheet(timesheet: Timesheet) {
        currentUser?.uid?.let { userId ->
            database.getReference("users/$userId/timesheets").push().setValue(timesheet)
        }
    }

    fun addTimesheetEntry(timesheetId: String, timesheetEntry: TimesheetEntry) {
        currentUser?.uid?.let { userId ->
            database.getReference("users/$userId/timesheets/$timesheetId/entries").push().setValue(timesheetEntry)
        }
    }

    fun fetchTimesheets(completion: (List<Timesheet>) -> Unit) {
        currentUser?.uid?.let { userId ->
            val timesheetsRef = database.getReference("users/$userId/timesheets")
            timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timesheets = mutableListOf<Timesheet>()
                    for (childSnapshot in snapshot.children) {
                        val timesheet = childSnapshot.getValue(Timesheet::class.java)
                        Log.d("TimesheetManager", "Fetched timesheet: $timesheet")
                        timesheet?.let { timesheets.add(it) }
                    }
                    Log.d("TimesheetManager", "Total timesheets fetched: ${timesheets.size}")
                    completion(timesheets)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TimesheetManager", "Error fetching timesheets: $error")
                    completion(emptyList())
                }
            })
        }
    }

    fun fetchAllTimesheetEntriesForUser(db: FirebaseDatabase, completion: (List<TimesheetEntry>) -> Unit) {
        currentUser?.uid?.let { userId ->
            val entriesRef = db.getReference("users/$userId/timesheets")
            entriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val entries = mutableListOf<TimesheetEntry>()
                    for (timesheetSnapshot in snapshot.children) {
                        for (entrySnapshot in timesheetSnapshot.child("entries").children) {
                            val entry = entrySnapshot.getValue(TimesheetEntry::class.java)
                            entry?.let { entries.add(it) }
                        }
                    }
                    completion(entries)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TimesheetManager", "Error fetching timesheet entries: $error")
                    completion(emptyList())
                }
            })
        }
    }

    fun getAuth(): FirebaseAuth {
        return auth
    }

    fun getDatabase(): FirebaseDatabase {
        return database
    }
}

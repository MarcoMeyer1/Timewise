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
        var id: String = "",
        var name: String = "",
        var color: String = "",
        var entries: MutableList<TimesheetEntry> = mutableListOf()
    )

    data class TimesheetEntry(
        var name: String = "",
        var startDate: Calendar = Calendar.getInstance(),
        var endDate: Calendar = Calendar.getInstance(),
        var isAllDay: Boolean = false,
        var category: String? = null,
        var photo: Uri? = null
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

    fun fetchTimesheets(callback: (List<Timesheet>, Map<String, String>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val timesheetsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("timesheets")

            timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timesheets = mutableListOf<Timesheet>()
                    val timesheetIdMap = mutableMapOf<String, String>()
                    for (timesheetSnapshot in snapshot.children) {
                        val timesheet = timesheetSnapshot.getValue(Timesheet::class.java)
                        timesheet?.let {
                            timesheets.add(it)
                            timesheetIdMap[it.name] = timesheetSnapshot.key ?: ""
                        }
                    }
                    callback(timesheets, timesheetIdMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TimesheetManager", "Error fetching timesheets: ${error.message}")
                    callback(emptyList(), emptyMap())
                }
            })
        } else {
            Log.e("TimesheetManager", "No current user logged in")
            callback(emptyList(), emptyMap())
        }
    }


    fun getAuth(): FirebaseAuth {
        return auth
    }

    fun getDatabase(): FirebaseDatabase {
        return database
    }
}

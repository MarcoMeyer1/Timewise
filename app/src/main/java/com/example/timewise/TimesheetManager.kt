package com.example.timewise

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.Calendar
import android.widget.Toast

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
        var photo: Uri? = null,
        var color: String = "" // Add this property
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

    fun fetchTimesheets(callback: (List<Timesheet>, Map<String, String>, Map<String, List<TimesheetEntry>>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val timesheetsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("timesheets")

            timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timesheets = mutableListOf<Timesheet>()
                    val timesheetIdMap = mutableMapOf<String, String>()
                    val timesheetEntriesMap = mutableMapOf<String, List<TimesheetEntry>>()

                    for (timesheetSnapshot in snapshot.children) {
                        val timesheet = timesheetSnapshot.getValue(Timesheet::class.java)
                        val timesheetId = timesheetSnapshot.key
                        if (timesheet != null && timesheetId != null) {
                            timesheets.add(timesheet)
                            timesheetIdMap[timesheet.name] = timesheetId

                            val entriesSnapshot = timesheetSnapshot.child("entries")
                            val entries = mutableListOf<TimesheetEntry>()
                            for (entrySnapshot in entriesSnapshot.children) {
                                val entryMap = entrySnapshot.value as? HashMap<*, *>
                                if (entryMap != null) {
                                    val entry = TimesheetEntry(
                                        name = entryMap["name"] as String? ?: "",
                                        startDate = Calendar.getInstance().apply { timeInMillis = entryMap["startDate"] as Long? ?: 0L },
                                        endDate = Calendar.getInstance().apply { timeInMillis = entryMap["endDate"] as Long? ?: 0L },
                                        isAllDay = entryMap["isAllDay"] as Boolean? ?: false,
                                        category = entryMap["category"] as String?,
                                        photo = (entryMap["photo"] as String?)?.let { Uri.parse(it) }
                                    )
                                    entries.add(entry)
                                }
                            }
                            timesheet.entries = entries.toMutableList()  // Update the entries in the timesheet
                            timesheetEntriesMap[timesheetId] = entries
                        }
                    }
                    callback(timesheets, timesheetIdMap, timesheetEntriesMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TimesheetManager", "Error fetching timesheets: ${error.message}")
                    callback(emptyList(), emptyMap(), emptyMap())
                }
            })
        } else {
            Log.e("TimesheetManager", "No current user logged in")
            callback(emptyList(), emptyMap(), emptyMap())
        }
    }

    fun getAuth(): FirebaseAuth {
        return auth
    }

    fun getDatabase(): FirebaseDatabase {
        return database
    }


    fun deleteTimesheet(context: Context, timesheetId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val timesheetRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("timesheets").child(timesheetId)

            timesheetRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Timesheet deleted successfully", Toast.LENGTH_SHORT).show()
                    (context as? ActiveTimesheetsPage)?.fetchAndDisplayTimesheets()
                }
                .addOnFailureListener { e ->
                    Log.e("TimesheetManager", "Error deleting timesheet: ${e.message}")
                }
        } else {
            Log.e("TimesheetManager", "No current user logged in")
        }
    }
}
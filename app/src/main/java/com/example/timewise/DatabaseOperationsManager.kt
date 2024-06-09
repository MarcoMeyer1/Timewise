package com.example.timewise

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DatabaseOperationsManager(private val context: Context) {

    fun createUser(
        db: FirebaseDatabase,
        userId: String,
        username: String,
        email: String,
        hashedPassword: String,
        minHours: Int,
        maxHours: Int,
        profilePictureUrl: String? = null
    ) {
        val userRef = db.getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val user = mapOf(
                    "username" to username,
                    "email" to email,
                    "password" to hashedPassword,
                    "dailyGoal" to mapOf(
                        "minHours" to minHours,
                        "maxHours" to maxHours
                    ),
                    "profilePictureUrl" to profilePictureUrl,
                    "timesheets" to emptyMap<String, Any>()
                )

                userRef.setValue(user)
                    .addOnSuccessListener { showToast("User $username created successfully") }
                    .addOnFailureListener { e -> showToast("Error creating user: ${e.message}") }
            } else {
                showToast("User $username already exists")
            }
        }.addOnFailureListener { e -> showToast("Error checking user: ${e.message}") }
    }

    fun createTimesheet(
        db: FirebaseDatabase,
        userId: String,
        timesheetId: String,
        name: String,
        color: String
    ) {
        val timesheetRef = db.getReference("users").child(userId).child("timesheets").child(timesheetId)

        timesheetRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val timesheet = mapOf(
                    "name" to name,
                    "color" to color,
                    "timesheetEntries" to emptyMap<String, Any>()
                )

                timesheetRef.setValue(timesheet)
                    .addOnSuccessListener { showToast("Timesheet $name created successfully") }
                    .addOnFailureListener { e -> showToast("Error creating timesheet: ${e.message}") }
            } else {
                showToast("Timesheet $name already exists")
            }
        }.addOnFailureListener { e -> showToast("Error checking timesheet: ${e.message}") }
    }

    fun createTimesheetEntry(
        db: FirebaseDatabase,
        userId: String,
        timesheetId: String,
        entryId: String,
        eventName: String,
        startDate: String,
        endDate: String,
        allDay: Boolean,
        photoUrl: String? = null
    ) {
        val entryRef = db.getReference("users").child(userId).child("timesheets").child(timesheetId)
            .child("timesheetEntries").child(entryId)

        entryRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val timesheetEntry = mapOf(
                    "eventName" to eventName,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "allDay" to allDay,
                    "photoUrl" to photoUrl
                )

                entryRef.setValue(timesheetEntry)
                    .addOnSuccessListener { showToast("Timesheet entry created successfully") }
                    .addOnFailureListener { e -> showToast("Error creating timesheet entry: ${e.message}") }
            } else {
                showToast("Timesheet entry already exists")
            }
        }.addOnFailureListener { e -> showToast("Error checking timesheet entry: ${e.message}") }
    }

    fun fetchTimesheets(db: FirebaseDatabase, userId: String, callback: (List<TimesheetManager.Timesheet>) -> Unit) {
        val timesheetsRef = db.getReference("users").child(userId).child("timesheets")

        timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timesheets = mutableListOf<TimesheetManager.Timesheet>()
                for (timesheetSnapshot in snapshot.children) {
                    val timesheet = timesheetSnapshot.getValue(TimesheetManager.Timesheet::class.java)
                    timesheet?.let { timesheets.add(it) }
                }
                callback(timesheets)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error fetching timesheets: ${error.message}")
            }
        })
    }

    // Method to fetch timesheet entries between given dates
    fun fetchTimesheetEntriesBetweenDates(startDate: Long, endDate: Long, callback: (List<TimesheetManager.TimesheetEntry>) -> Unit) {
        // Implement database query to fetch timesheet entries between the provided dates
        // Execute the query and fetch the results
        // Convert the results into a list of TimesheetEntry objects
        // Invoke the callback with the list of timesheet entries
        // Example usage of callback: callback(timesheetEntriesList)
    }

    // Method to fetch colors for timesheets
    fun fetchColorsForTimesheets(callback: (Map<String, String>) -> Unit) {
        // Implement database query to fetch colors for timesheets
        // Execute the query and fetch the results
        // Convert the results into a map of timesheet name to color hex code
        // Invoke the callback with the map of timesheet colors
        // Example usage of callback: callback(timesheetColorsMap)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
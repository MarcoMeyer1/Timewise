package com.example.timewise

import android.content.Context
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

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

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
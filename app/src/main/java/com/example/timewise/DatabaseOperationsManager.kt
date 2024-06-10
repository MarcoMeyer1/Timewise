package com.example.timewise

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

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

    fun updateTimesheet(
        db: FirebaseDatabase,
        userId: String,
        timesheetId: String,
        name: String,
        color: String,
        onComplete: (Boolean) -> Unit
    ) {
        val timesheetRef = db.getReference("users").child(userId).child("timesheets").child(timesheetId)
        val updates = mapOf(
            "name" to name,
            "color" to color
        )

        timesheetRef.updateChildren(updates).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }.addOnFailureListener { e ->
            Log.e("DatabaseOperationsManager", "Error updating timesheet: ${e.message}")
            onComplete(false)
        }
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
                    "entries" to emptyMap<String, Any>()
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
        timesheetName: String, // Using timesheet name directly
        entryId: String,
        eventName: String,
        startDate: Long,
        endDate: Long,
        allDay: Boolean,
        photoUrl: String? = null
    ) {
        val entryRef = db.getReference("users").child(userId).child("timesheets").child(timesheetName).child("entries").child(entryId)

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



    fun fetchTimesheets(db: FirebaseDatabase, userId: String, callback: (List<TimesheetManager.Timesheet>, Map<String, String>) -> Unit) {
        val timesheetsRef = db.getReference("users").child(userId).child("timesheets")

        timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timesheets = mutableListOf<TimesheetManager.Timesheet>()
                val timesheetIdMap = mutableMapOf<String, String>()
                for (timesheetSnapshot in snapshot.children) {
                    val timesheet = TimesheetManager.Timesheet().apply {
                        id = timesheetSnapshot.key ?: ""
                        name = timesheetSnapshot.child("name").getValue(String::class.java) ?: ""
                        color = timesheetSnapshot.child("color").getValue(String::class.java) ?: ""
                        entries = timesheetSnapshot.child("entries").children.mapNotNull { entrySnapshot ->
                            TimesheetManager.TimesheetEntry(
                                name = entrySnapshot.child("eventName").getValue(String::class.java) ?: "",
                                startDate = Calendar.getInstance().apply {
                                    timeInMillis = entrySnapshot.child("startDate").getValue(Long::class.java) ?: 0L
                                },
                                endDate = Calendar.getInstance().apply {
                                    timeInMillis = entrySnapshot.child("endDate").getValue(Long::class.java) ?: 0L
                                },
                                isAllDay = entrySnapshot.child("allDay").getValue(Boolean::class.java) ?: false,
                                category = entrySnapshot.child("category").getValue(String::class.java),
                                photo = entrySnapshot.child("photo").getValue(String::class.java)?.let { Uri.parse(it) }
                            )
                        }.toMutableList()
                    }
                    timesheets.add(timesheet)
                    timesheetIdMap[timesheet.name] = timesheet.id
                }
                callback(timesheets, timesheetIdMap)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error fetching timesheets: ${error.message}")
            }
        })
    }




    fun fetchAllTimesheetEntriesForUser(
        db: FirebaseDatabase,
        userId: String,
        completion: (List<TimesheetManager.TimesheetEntry>) -> Unit
    ) {
        val userRef = db.getReference("users/$userId/timesheets")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allEntries = mutableListOf<TimesheetManager.TimesheetEntry>()
                for (timesheetSnapshot in snapshot.children) {
                    val entriesSnapshot = timesheetSnapshot.child("entries")
                    for (entrySnapshot in entriesSnapshot.children) {
                        val entry = TimesheetManager.TimesheetEntry().apply {
                            name = entrySnapshot.child("eventName").getValue(String::class.java) ?: ""
                            startDate = Calendar.getInstance().apply {
                                timeInMillis = entrySnapshot.child("startDate").getValue(Long::class.java) ?: 0L
                            }
                            endDate = Calendar.getInstance().apply {
                                timeInMillis = entrySnapshot.child("endDate").getValue(Long::class.java) ?: 0L
                            }
                            isAllDay = entrySnapshot.child("allDay").getValue(Boolean::class.java) ?: false
                            category = entrySnapshot.child("category").getValue(String::class.java)
                            photo = entrySnapshot.child("photo").getValue(String::class.java)?.let { Uri.parse(it) }
                            color = timesheetSnapshot.child("color").getValue(String::class.java) ?: "" // Get color from timesheet
                        }
                        allEntries.add(entry)
                    }
                }
                completion(allEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseOperationsManager", "Error fetching entries: ${error.message}")
                completion(emptyList())
            }
        })
    }

    fun fetchUserGoals(db: FirebaseDatabase, userId: String, completion: (Float, Float) -> Unit) {
        val userRef = db.getReference("users/$userId/dailyGoal")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val minHours = snapshot.child("minHours").getValue(Float::class.java) ?: 0f
                val maxHours = snapshot.child("maxHours").getValue(Float::class.java) ?: 0f
                completion(minHours, maxHours)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseOperationsManager", "Error fetching user goals: $error")
                completion(0f, 0f)
            }
        })
    }


    fun fetchTimesheetEntriesBetweenDates(
        db: FirebaseDatabase,
        userId: String,
        start: Long,
        end: Long,
        completion: (List<TimesheetManager.TimesheetEntry>) -> Unit
    ) {
        val entriesRef = db.getReference("users/$userId/timesheets")

        entriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timesheetEntries = mutableListOf<TimesheetManager.TimesheetEntry>()
                for (timesheetSnapshot in snapshot.children) {
                    val entriesSnapshot = timesheetSnapshot.child("entries")
                    for (entrySnapshot in entriesSnapshot.children) {
                        val entry = TimesheetManager.TimesheetEntry()
                        val startDateMillis = entrySnapshot.child("startDate").getValue(Long::class.java) ?: 0L
                        val endDateMillis = entrySnapshot.child("endDate").getValue(Long::class.java) ?: 0L
                        entry.name = entrySnapshot.child("eventName").getValue(String::class.java) ?: ""
                        entry.startDate = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                        entry.endDate = Calendar.getInstance().apply { timeInMillis = endDateMillis }
                        entry.isAllDay = entrySnapshot.child("allDay").getValue(Boolean::class.java) ?: false
                        entry.category = entrySnapshot.child("category").getValue(String::class.java)
                        entry.photo = entrySnapshot.child("photo").getValue(String::class.java)?.let { Uri.parse(it) }
                        entry.color = timesheetSnapshot.child("color").getValue(String::class.java) ?: ""
                        if (entry.startDate.timeInMillis in start..end) {
                            timesheetEntries.add(entry)
                        }
                    }
                }
                completion(timesheetEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseOperationsManager", "Error fetching timesheet entries: $error")
                completion(emptyList())
            }
        })
    }

    fun fetchTimesheetsWithEntries(
        db: FirebaseDatabase,
        userId: String,
        start: Long,
        end: Long,
        completion: (Map<String, List<TimesheetManager.TimesheetEntry>>, Map<String, String>) -> Unit
    ) {
        val timesheetsRef = db.getReference("users/$userId/timesheets")

        timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timesheetEntriesMap = mutableMapOf<String, MutableList<TimesheetManager.TimesheetEntry>>()
                val timesheetNamesMap = mutableMapOf<String, String>()
                for (timesheetSnapshot in snapshot.children) {
                    val timesheetId = timesheetSnapshot.key ?: continue
                    val timesheetName = timesheetSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val timesheetColor = timesheetSnapshot.child("color").getValue(String::class.java) ?: "#FFFFFF"
                    timesheetNamesMap[timesheetId] = timesheetName // Store the name
                    val entriesSnapshot = timesheetSnapshot.child("entries")
                    val entriesList = mutableListOf<TimesheetManager.TimesheetEntry>()
                    for (entrySnapshot in entriesSnapshot.children) {
                        val entryMap = entrySnapshot.value as? HashMap<*, *>
                        if (entryMap != null) {
                            val entry = TimesheetManager.TimesheetEntry(
                                name = entryMap["name"] as String? ?: "",
                                startDate = Calendar.getInstance().apply { timeInMillis = entryMap["startDate"] as Long? ?: 0L },
                                endDate = Calendar.getInstance().apply { timeInMillis = entryMap["endDate"] as Long? ?: 0L },
                                isAllDay = entryMap["isAllDay"] as Boolean? ?: false,
                                category = entryMap["category"] as String?,
                                photo = (entryMap["photo"] as String?)?.let { Uri.parse(it) },
                                color = timesheetColor // Assign the color to each entry
                            )
                            if (entry.startDate.timeInMillis in start..end) {
                                entriesList.add(entry)
                            }
                        }
                    }
                    timesheetEntriesMap[timesheetId] = entriesList
                }
                Log.d("DatabaseOperationsManager", "Fetched Timesheet Entries Map: $timesheetEntriesMap")
                completion(timesheetEntriesMap, timesheetNamesMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseOperationsManager", "Error fetching timesheet entries: ${error.message}")
                completion(emptyMap(), emptyMap())
            }
        })
    }






    fun fetchColorsForTimesheets(
        db: FirebaseDatabase,
        userId: String,
        callback: (Map<String, String>) -> Unit
    ) {
        val timesheetsRef = db.getReference("users").child(userId).child("timesheets")

        timesheetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timesheetColors = mutableMapOf<String, String>()
                for (timesheetSnapshot in snapshot.children) {
                    val timesheetId = timesheetSnapshot.key
                    val color = timesheetSnapshot.child("color").getValue(String::class.java)
                    if (timesheetId != null && color != null) {
                        timesheetColors[timesheetId] = color
                    }
                }
                callback(timesheetColors)
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error fetching timesheet colors: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.timewise

import java.util.Calendar

object TimesheetManager {
    var timesheets: MutableList<Timesheet> = mutableListOf()

    fun addTimesheet(timesheet: Timesheet) {
        timesheets.add(timesheet)
    }

    fun addAllTimesheets(timesheets: List<Timesheet>) {
        this.timesheets.addAll(timesheets)
    }

    fun addTimesheetEntry(timesheetId: Int, timesheetEntry: TimesheetEntry) {
        val timesheet = timesheets.find { it.id == timesheetId }
        timesheet?.entries?.add(timesheetEntry)
    }

    fun getEntries(timesheetId: Int): List<TimesheetEntry> {
        val timesheet = timesheets.find { it.id == timesheetId }
        return timesheet?.entries ?: emptyList()
    }
    fun getDummyTimesheet(): Timesheet {
        val timesheetName = "Work"  // This is the name of the Timesheet.
        val colorHex = "#FFA500"    // Orange color, representing the Timesheet.

        return Timesheet(
            id = 1,
            name = timesheetName,
            colorHex = colorHex,
            entries = mutableListOf(
                TimesheetEntry(
                    name = "Meeting",
                    startDate = Calendar.getInstance().apply { set(2024, Calendar.MAY, 5, 9, 0) }, // May 5, 2024, 9:00 AM
                    endDate = Calendar.getInstance().apply { set(2024, Calendar.MAY, 5, 10, 0) }, // May 5, 2024, 10:00 AM
                    isAllDay = false,
                    category = timesheetName,  // Using the Timesheet name as the category
                    photo = null
                ),
                TimesheetEntry(
                    name = "Lunch",
                    startDate = Calendar.getInstance().apply { set(2024, Calendar.MAY, 5, 12, 0) }, // May 5, 2024, 12:00 PM
                    endDate = Calendar.getInstance().apply { set(2024, Calendar.MAY, 5, 13, 0) }, // May 5, 2024, 1:00 PM
                    isAllDay = false,
                    category = timesheetName,  // Using the Timesheet name as the category
                    photo = null
                )
            )
        )
    }


}

// Timesheet data class
data class Timesheet(
    val id: Int,
    var name: String,
    var colorHex: String,
    val entries: MutableList<TimesheetEntry>? = null

)

// TimesheetEntry data class
data class TimesheetEntry(
    val name: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val isAllDay: Boolean,
    val category: String?,
    val photo: String?
)

object TimesheetRepository {

}
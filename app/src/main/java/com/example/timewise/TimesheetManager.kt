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

    fun getEntries(): List<TimesheetEntry> {
        return timesheets.flatMap { it.entries ?: emptyList() }
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
    fun getDummyTimesheet(): Timesheet {
        return Timesheet(
            id = 1,
            name = "Work",
            colorHex = "#FFA500", // Orange color
            entries = mutableListOf(
                TimesheetEntry(
                    name = "Meeting",
                    startDate = Calendar.getInstance()
                        .apply { set(2024, Calendar.MAY, 5, 9, 0) }, // May 5, 2024, 9:00 AM
                    endDate = Calendar.getInstance()
                        .apply { set(2024, Calendar.MAY, 5, 10, 0) }, // May 5, 2024, 10:00 AM
                    isAllDay = false,
                    category = "Business",
                    photo = null
                ),
                TimesheetEntry(
                    name = "Lunch",
                    startDate = Calendar.getInstance()
                        .apply { set(2024, Calendar.MAY, 5, 12, 0) }, // May 5, 2024, 12:00 PM
                    endDate = Calendar.getInstance()
                        .apply { set(2024, Calendar.MAY, 5, 13, 0) }, // May 5, 2024, 1:00 PM
                    isAllDay = false,
                    category = "Personal",
                    photo = null
                ),

                )
        )
    }
}
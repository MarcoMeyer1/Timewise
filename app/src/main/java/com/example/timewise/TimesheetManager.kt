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

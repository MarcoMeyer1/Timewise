package com.example.timewise

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
    val description: String,
    val hours: Int
)

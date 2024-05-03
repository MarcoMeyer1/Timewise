package com.example.timewise

import android.net.Uri
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

    fun aggregateTimeSheetEntries(): List<TimesheetEntry> {
        val allEntries = mutableListOf<TimesheetEntry>()
        for (timesheet in TimesheetManager.timesheets) {
            allEntries.addAll(timesheet.entries ?: emptyList())
        }
        return allEntries
    }

    fun getEntries(timesheetId: Int): List<TimesheetEntry> {
        val timesheet = timesheets.find { it.id == timesheetId }
        return timesheet?.entries ?: emptyList()
    }
    fun getDummyTimesheets(): MutableList<Timesheet> {
        val timesheets = mutableListOf<Timesheet>()

        // First Timesheet
        val timesheetName1 = "Work"
        val colorHex1 = "#FFA500"

        val entries1 = mutableListOf(
            TimesheetEntry(
                name = "Meeting 1",
                startDate = getCalendar(2024, Calendar.MAY, 5, 8, 0),
                endDate = getCalendar(2024, Calendar.MAY, 5, 10, 0),
                isAllDay = false,
                category = timesheetName1,
                photo = null
            ),
            TimesheetEntry(
                name = "Lunch 1",
                startDate = getCalendar(2024, Calendar.MAY, 5, 12, 0),
                endDate = getCalendar(2024, Calendar.MAY, 5, 14, 0),
                isAllDay = false,
                category = timesheetName1,
                photo = null
            ),
            TimesheetEntry(
                name = "Task 1",
                startDate = getCalendar(2024, Calendar.MAY, 5, 15, 0),
                endDate = getCalendar(2024, Calendar.MAY, 5, 17, 0),
                isAllDay = false,
                category = timesheetName1,
                photo = null
            )
        )

        val timesheet1 = Timesheet(
            id = 1,
            name = timesheetName1,
            colorHex = colorHex1,
            entries = entries1
        )
        timesheets.add(timesheet1)

        // Second Timesheet
        val timesheetName2 = "Personal"
        val colorHex2 = "#00FF00"

        val entries2 = mutableListOf(
            TimesheetEntry(
                name = "Gym",
                startDate = getCalendar(2024, Calendar.MAY, 6, 8, 0),
                endDate = getCalendar(2024, Calendar.MAY, 6, 10, 0),
                isAllDay = false,
                category = timesheetName2,
                photo = null
            ),
            TimesheetEntry(
                name = "Grocery shopping",
                startDate = getCalendar(2024, Calendar.MAY, 6, 11, 0),
                endDate = getCalendar(2024, Calendar.MAY, 6, 16, 0),
                isAllDay = false,
                category = timesheetName2,
                photo = null
            ),
            TimesheetEntry(
                name = "Reading",
                startDate = getCalendar(2024, Calendar.MAY, 6, 14, 0),
                endDate = getCalendar(2024, Calendar.MAY, 6, 22, 0),
                isAllDay = false,
                category = timesheetName2,
                photo = null
            )
        )

        val timesheet2 = Timesheet(
            id = 2,
            name = timesheetName2,
            colorHex = colorHex2,
            entries = entries2
        )
        timesheets.add(timesheet2)

        // Third Timesheet
        val timesheetName3 = "Books"
        val colorHex3 = "#00FFFF"

        val entries3 = mutableListOf(
            TimesheetEntry(
                name = "Gym",
                startDate = getCalendar(2024, Calendar.MAY, 6, 8, 0),
                endDate = getCalendar(2024, Calendar.MAY, 6, 10, 0),
                isAllDay = false,
                category = timesheetName2,
                photo = null
            ),
            TimesheetEntry(
                name = "Grocery shopping",
                startDate = getCalendar(2024, Calendar.MAY, 6, 11, 0),
                endDate = getCalendar(2024, Calendar.MAY, 6, 16, 0),
                isAllDay = false,
                category = timesheetName2,
                photo = null
            ),
            TimesheetEntry(
                name = "Reading",
                startDate = getCalendar(2024, Calendar.MAY, 6, 14, 0),
                endDate = getCalendar(2024, Calendar.MAY, 6, 22, 0),
                isAllDay = false,
                category = timesheetName2,
                photo = null
            )
        )

        val timesheet3 = Timesheet(
            id = 3,
            name = timesheetName3,
            colorHex = colorHex3,
            entries = entries3
        )
        timesheets.add(timesheet3)

        return timesheets
    }

    fun getCalendar(year: Int, month: Int, day: Int, hourOfDay: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply { set(year, month, day, hourOfDay, minute) }
    }




}

// Timesheet data class
data class Timesheet(
    val id: Int,
    var name: String,
    var colorHex: String,
    var entries: MutableList<TimesheetEntry>? = null

)

// TimesheetEntry data class
data class TimesheetEntry(
    val name: String,
    val startDate: Calendar,
    val endDate: Calendar,
    val isAllDay: Boolean,
    val category: String?,
    val photo: Uri?
)


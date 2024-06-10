package com.example.timewise

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class TimesheetDetails : AppCompatActivity() {

    private lateinit var timesheetNameTextView: TextView
    private lateinit var entryCountTextView: TextView
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheet_details)

        timesheetNameTextView = findViewById(R.id.timesheet_name)
        entryCountTextView = findViewById(R.id.entry_count)
        pieChart = findViewById(R.id.pieChart)

        val timesheetId = intent.getStringExtra("timesheetId")
        if (timesheetId != null) {
            fetchAndDisplayTimesheet(timesheetId)
        }
    }

    private fun fetchAndDisplayTimesheet(timesheetId: String) {
        TimesheetManager.fetchTimesheets { timesheets, idMap, entriesMap ->
            val timesheet = timesheets.find { it.id == timesheetId }
            if (timesheet != null) {
                displayTimesheetDetails(timesheet)
            }
        }
    }

    private fun displayTimesheetDetails(timesheet: TimesheetManager.Timesheet) {
        timesheetNameTextView.text = timesheet.name
        entryCountTextView.text = "Number of Entries: ${timesheet.entries.size}"

        // Calculate total hours spent, minimum hours left, and maximum hours left
        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val minHours = prefs.getString("MinHours", "0")?.toInt() ?: 0
        val maxHours = prefs.getString("MaxHours", "0")?.toInt() ?: 0
        val totalHours = timesheet.entries.sumBy {
            val start = it.startDate
            val end = it.endDate
            ((end.timeInMillis - start.timeInMillis) / (1000 * 60 * 60)).toInt()
        }
        val minLeft = (minHours - totalHours).coerceAtLeast(0)
        val maxLeft = (maxHours - totalHours).coerceAtLeast(0)

        // Prepare data for pie chart
        val userHours = totalHours.toFloat()
        val pieEntries = listOf(
            PieEntry(userHours, "Hours Spent"),
            PieEntry(minLeft.toFloat(), "Min Hours Left"),
            PieEntry(maxLeft.toFloat(), "Max Hours Left")
        )

        // Set up pie chart
        val dataSet = PieDataSet(pieEntries, "Time Progress")
        dataSet.colors = listOf(
            Color.parseColor("#A0C4FF"), // Lighter Blue
            Color.parseColor("#B9E2A4"), // Lighter Green
            Color.parseColor("#FFB4A2")  // Lighter Red
        )
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.BLACK

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(16f)
        pieData.setValueTextColor(Color.BLACK)

        pieChart.data = pieData
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(16f)
        pieChart.legend.textSize = 16f
        pieChart.legend.textColor = Color.BLACK
        pieChart.invalidate() // Refresh the chart

        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.isEnabled = true
        pieChart.legend.setCustom(
            listOf(
                LegendEntry("Hours Spent", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.parseColor("#A0C4FF")),
                LegendEntry("Min Hours Left", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.parseColor("#B9E2A4")),
                LegendEntry("Max Hours Left", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.parseColor("#FFB4A2"))
            )
        )
    }
}
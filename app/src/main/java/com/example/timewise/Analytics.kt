package com.example.timewise

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar

class Analytics : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val selectDateButton: Button = findViewById(R.id.selectDateButton)
        val chart: BarChart = findViewById(R.id.chart)

        selectDateButton.setOnClickListener {
            // Ideally, launch a DatePicker dialog here and get user-selected dates
            Toast.makeText(this, "Date picker not implemented", Toast.LENGTH_SHORT).show()
        }

        // Fetch and display the data
        setupChart(chart)
    }

    private fun setupChart(chart: BarChart) {
        val entries = getChartData()
        val dataSet = BarDataSet(entries, "Hours by Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val data = BarData(dataSet)
        chart.data = data
        chart.description.text = "Category Hours"
        chart.animateY(1000)
        chart.invalidate() // Refresh chart
    }

    private fun getChartData(): MutableList<BarEntry> {
        val entries = mutableListOf<BarEntry>()
        val categoryHours = mutableMapOf<String, Float>()
        var dummyTimesheet = TimesheetManager.getDummyTimesheet()
        TimesheetManager.addTimesheet(dummyTimesheet)
        val timesheetEntries = TimesheetManager.getEntries(1)  // Make sure this ID matches a timesheet that exists

        if (timesheetEntries.isEmpty()) {
            Log.d("Analytics", "No entries found for timesheet")
        }

        // Calculate total hours per category
        for (entry in timesheetEntries) {
            val duration = (entry.endDate.timeInMillis - entry.startDate.timeInMillis) / (1000 * 60 * 60).toFloat() // Convert to hours
            val currentHours = categoryHours[entry.category] ?: 0f
            categoryHours[entry.category ?: "Uncategorized"] = currentHours + duration
        }

        var index = 0f
        for ((category, hours) in categoryHours) {
            entries.add(BarEntry(index++, hours))
            Log.d("Analytics", "Category: $category, Hours: $hours")
        }

        return entries
    }
}

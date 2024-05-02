package com.example.timewise

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class Analytics : BaseActivity() {
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val selectDateButton: Button = findViewById(R.id.selectDateButton)
        val chart: BarChart = findViewById(R.id.chart)

        updateToolbarColor("#FF6262")

        selectDateButton.setOnClickListener {
            selectDateRange {
                setupChart(chart)
            }
        }

        // Load all data initially
        setupChart(chart)
    }

    private fun selectDateRange(onDatesSelected: () -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            startDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            DatePickerDialog(this, { _, eYear, eMonth, eDayOfMonth ->
                endDate = Calendar.getInstance().apply {
                    set(eYear, eMonth, eDayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                onDatesSelected()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).apply {
                setTitle("Select End Date")
                show()
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).apply {
            setTitle("Select Start Date")
            show()
        }
    }

    private fun setupChart(chart: BarChart) {
        val entries = getChartData()
        if (entries.isEmpty()) {
            chart.clear()
            Toast.makeText(this, "No data to display", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = BarDataSet(entries, "Hours by Category")
        val colors = TimesheetManager.timesheets.mapNotNull {
            try {
                Color.parseColor(it.colorHex)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        dataSet.colors = if (colors.isNotEmpty()) colors else ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        chart.data = data
        chart.description.isEnabled = false

// Configure the legend
        val legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.form = Legend.LegendForm.SQUARE // This can be LINE, CIRCLE, or SQUARE
        legend.formSize = 8f // Set the size of the legend forms (icons)
        legend.textSize = 12f // Set the text size of the legend

        chart.animateY(1000)
        chart.invalidate()
    }










    private fun getChartData(): MutableList<BarEntry> {
        val entries = mutableListOf<BarEntry>()
        val categoryHours = mutableMapOf<String, Float>()

        // Define a flag to determine whether to use dummy data or actual data
        val useDummyData = true  // Set this to true to use dummy data, false to use actual timesheet data


        val timesheets = if (useDummyData) TimesheetManager.getDummyTimesheets() else TimesheetManager.timesheets

        timesheets.forEach { timesheet ->
            timesheet.entries?.forEach { entry ->
                if ((startDate == null || entry.startDate.timeInMillis >= startDate!!.timeInMillis) &&
                    (endDate == null || entry.endDate.timeInMillis <= endDate!!.timeInMillis)) {
                    val duration = (entry.endDate.timeInMillis - entry.startDate.timeInMillis) / (1000 * 60 * 60).toFloat()
                    categoryHours[timesheet.name] = categoryHours.getOrDefault(timesheet.name, 0f) + duration
                }
            }
        }

        var index = 0f
        categoryHours.forEach { (category, hours) ->
            entries.add(BarEntry(index++, hours))
        }

        return entries
    }

}

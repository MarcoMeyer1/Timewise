package com.example.timewise

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var timesheetEntryList: RecyclerView
    private lateinit var chart: BarChart
    private lateinit var dailyChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        setupRecyclerView()

        val selectDateButton: Button = findViewById(R.id.selectDateButton)
        chart = findViewById(R.id.chart)
        dailyChart = findViewById(R.id.dailyChart)

        updateToolbarColor("#FF6262")

        selectDateButton.setOnClickListener {
            selectDateRange {
                updateData()
            }
        }

        // Load all data initially
        initializeDateRange()
        updateData()
    }

    private fun initializeDateRange() {
        startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }  // One week ago
        endDate = Calendar.getInstance()  // Today
    }

    private fun setupRecyclerView() {
        timesheetEntryList = findViewById(R.id.timesheetEntryList)
        timesheetEntryList.layoutManager = LinearLayoutManager(this)
        timesheetEntryList.adapter = TimesheetEntryAdapter(mutableListOf(), this::handleEntryClick)
    }

    private fun updateData() {
        val entries = getTimesheetEntries()
        updateTimesheetEntries(entries)
        setupChart(entries)
        setupDailyChart(entries)
    }

    private fun getTimesheetEntries(): List<TimesheetManager.TimesheetEntry> {
        val start = startDate?.timeInMillis ?: return emptyList()
        val end = endDate?.timeInMillis ?: return emptyList()

        val entries = mutableListOf<TimesheetManager.TimesheetEntry>()
        val userId = TimesheetManager.getAuth().currentUser?.uid ?: return entries
        val db = TimesheetManager.getDatabase()

        DatabaseOperationsManager(this).fetchTimesheetEntriesBetweenDates(db, userId, start, end) {
            entries.addAll(it)
            (timesheetEntryList.adapter as TimesheetEntryAdapter).updateEntries(entries)
        }

        return entries
    }

    private fun updateTimesheetEntries(entries: List<TimesheetManager.TimesheetEntry>) {
        (timesheetEntryList.adapter as TimesheetEntryAdapter).updateEntries(entries)
    }

    private fun handleEntryClick(entry: TimesheetManager.TimesheetEntry) {
        entry.photo?.let { photoUri ->
            showPhoto(photoUri)
        } ?: run {
            Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPhoto(photoUri: Uri) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_photo_viewer)
        val photoView: ImageView = dialog.findViewById(R.id.photoView)
        photoView.setImageURI(photoUri)
        dialog.show()
    }

    private fun selectDateRange(onDatesSelected: () -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            startDate = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            DatePickerDialog(this, { _, year, month, day ->
                endDate = Calendar.getInstance().apply {
                    set(year, month, day, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                onDatesSelected()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupChart(entries: List<TimesheetManager.TimesheetEntry>) {
        val dataEntries = getChartData(entries)
        if (dataEntries.isEmpty()) {
            chart.clear()
            Toast.makeText(this, "No data to display", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = BarDataSet(dataEntries, "Hours by Timesheet")
        val userId = TimesheetManager.getAuth().currentUser?.uid ?: return
        val db = TimesheetManager.getDatabase()

        DatabaseOperationsManager(this).fetchColorsForTimesheets(db, userId) { colors ->
            dataSet.colors = if (colors.isNotEmpty()) colors.values.map { Color.parseColor(it) }.toMutableList() else ColorTemplate.MATERIAL_COLORS.toMutableList()
            dataSet.valueTextSize = 12f

            val data = BarData(dataSet)
            chart.data = data
            chart.description.isEnabled = false

            val legend = chart.legend
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            legend.form = Legend.LegendForm.SQUARE
            legend.formSize = 8f
            legend.textSize = 12f

            chart.animateY(1000)
            chart.invalidate()
        }
    }

    private fun getChartData(entries: List<TimesheetManager.TimesheetEntry>): MutableList<BarEntry> {
        val dataEntries = mutableListOf<BarEntry>()

        val timesheetHours = mutableMapOf<String, Float>()
        for (entry in entries) {
            val hours = (entry.endDate.timeInMillis - entry.startDate.timeInMillis) / (1000 * 60 * 60).toFloat()
            timesheetHours[entry.name] = timesheetHours.getOrDefault(entry.name, 0f) + hours
        }

        timesheetHours.entries.forEachIndexed { index, entry ->
            dataEntries.add(BarEntry(index.toFloat(), entry.value))
        }

        return dataEntries
    }

    private fun setupDailyChart(entries: List<TimesheetManager.TimesheetEntry>) {
        val dataEntries = getDailyChartData(entries)
        if (dataEntries.isEmpty()) {
            dailyChart.clear()
            Toast.makeText(this, "No data to display", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = BarDataSet(dataEntries, "Hours per Day")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        dailyChart.data = data
        dailyChart.description.isEnabled = false

        val legend = dailyChart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.form = Legend.LegendForm.SQUARE
        legend.formSize = 8f
        legend.textSize = 12f

        dailyChart.animateY(1000)
        dailyChart.invalidate()
    }

    private fun getDailyChartData(entries: List<TimesheetManager.TimesheetEntry>): MutableList<BarEntry> {
        val dataEntries = mutableListOf<BarEntry>()

        val dailyHours = mutableMapOf<String, Float>()
        for (entry in entries) {
            val date = "${entry.startDate.get(Calendar.YEAR)}-${entry.startDate.get(Calendar.MONTH) + 1}-${entry.startDate.get(Calendar.DAY_OF_MONTH)}"
            val hours = (entry.endDate.timeInMillis - entry.startDate.timeInMillis) / (1000 * 60 * 60).toFloat()
            dailyHours[date] = dailyHours.getOrDefault(date, 0f) + hours
        }

        dailyHours.entries.forEachIndexed { index, entry ->
            dataEntries.add(BarEntry(index.toFloat(), entry.value))
        }

        return dataEntries
    }
}

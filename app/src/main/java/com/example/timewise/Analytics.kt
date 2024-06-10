package com.example.timewise

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Analytics : BaseActivity() {
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private lateinit var timesheetEntryList: RecyclerView
    private lateinit var chart: BarChart
    private lateinit var dailyChart: BarChart
    private var timesheetEntries: List<TimesheetManager.TimesheetEntry> = emptyList()
    private var timesheetEntriesMap: Map<String, List<TimesheetManager.TimesheetEntry>> = emptyMap()
    private var timesheetNamesMap: Map<String, String> = emptyMap()

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
                fetchDataAndUpdateUI()
            }
        }

        // Load all data initially
        fetchDataAndUpdateUI()
    }

    private fun setupRecyclerView() {
        timesheetEntryList = findViewById(R.id.timesheetEntryList)
        timesheetEntryList.layoutManager = LinearLayoutManager(this)
        timesheetEntryList.adapter = TimesheetEntryAdapter(mutableListOf(), this::handleEntryClick)
    }

    private fun fetchDataAndUpdateUI() {
        val start = startDate?.timeInMillis ?: return
        val end = endDate?.timeInMillis ?: return

        val userId = TimesheetManager.getAuth().currentUser?.uid ?: return
        val db = TimesheetManager.getDatabase()

        DatabaseOperationsManager(this).fetchTimesheetsWithEntries(db, userId, start, end) { fetchedEntriesMap, fetchedTimesheetNamesMap ->
            timesheetEntriesMap = fetchedEntriesMap
            timesheetNamesMap = fetchedTimesheetNamesMap
            updateTimesheetEntries()
            updateCharts()
        }
    }

    private fun handleEntryClick(entry: TimesheetManager.TimesheetEntry) {
        entry.photo?.let { photoUri ->
            showPhoto(photoUri)
        } ?: run {
            Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTimesheetEntries() {
        (timesheetEntryList.adapter as TimesheetEntryAdapter).updateEntries(timesheetEntries)
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

    private fun updateCharts() {
        setupChart(chart, getChartData())
        setupDailyChart(dailyChart, getDailyChartData())
    }

    private fun setupChart(chart: BarChart, entries: List<BarEntry>) {
        if (entries.isEmpty()) {
            chart.clear()
            Toast.makeText(this, "No data to display", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = BarDataSet(entries, "Hours by Timesheet")
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

            // Set x-axis labels
            val xAxis = chart.xAxis
            val timesheetNames = timesheetEntriesMap.keys.map { timesheetId ->
                timesheetNamesMap[timesheetId] ?: "Unknown"
            }
            xAxis.valueFormatter = IndexAxisValueFormatter(timesheetNames)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawLabels(true)
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(true)

            chart.animateY(1000)
            chart.invalidate()
        }
    }




    private fun getChartData(): List<BarEntry> {
        val entries = mutableListOf<BarEntry>()

        // Generate BarEntry for each timesheet
        timesheetEntriesMap.entries.forEachIndexed { index, entry ->
            val totalHours = entry.value.sumByDouble {
                ((it.endDate.timeInMillis - it.startDate.timeInMillis) / (1000 * 60 * 60)).toDouble()
            }.toFloat()
            entries.add(BarEntry(index.toFloat(), totalHours))
        }

        return entries
    }


    private fun setupDailyChart(chart: BarChart, entries: List<BarEntry>) {
        if (entries.isEmpty()) {
            chart.clear()
            Toast.makeText(this, "No data to display", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = BarDataSet(entries, "Hours per Day")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
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

        val dailyHours = getDailyHours()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

// Inside the setupDailyChart function
        val xAxis = chart.xAxis
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val date = dailyHours.keys.sorted().getOrNull(value.toInt())
                return date?.let {
                    try {
                        val parsedDate = dateFormat.parse(it)
                        parsedDate?.let { parsed ->
                            val formattedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(parsed)
                            formattedDate
                        } ?: ""
                    } catch (e: ParseException) {
                        ""
                    }
                } ?: ""
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        // Fetch the user's goals and add limit lines
        val userId = TimesheetManager.getAuth().currentUser?.uid ?: return
        val db = TimesheetManager.getDatabase()

        DatabaseOperationsManager(this).fetchUserGoals(db, userId) { minHours, maxHours ->
            val minLine = LimitLine(minHours, "Min Hours")
            minLine.lineWidth = 2f
            minLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            minLine.textSize = 10f
            minLine.lineColor = Color.RED

            val maxLine = LimitLine(maxHours, "Max Hours")
            maxLine.lineWidth = 2f
            maxLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            maxLine.textSize = 10f
            maxLine.lineColor = Color.GREEN

            val yAxis = chart.axisLeft
            yAxis.addLimitLine(minLine)
            yAxis.addLimitLine(maxLine)

            chart.invalidate()
        }

        chart.animateY(1000)
        chart.invalidate()
    }


    private fun getDailyChartData(): List<BarEntry> {
        val entries = mutableListOf<BarEntry>()

        val dailyHours = getDailyHours()

        dailyHours.forEach { (date, hours) ->
            val index = dailyHours.keys.sorted().indexOf(date)
            entries.add(BarEntry(index.toFloat(), hours))
        }

        return entries
    }

    private fun getDailyHours(): Map<String, Float> {
        val dailyHours = mutableMapOf<String, Float>()
        for (entry in timesheetEntries) {
            val date = "${entry.startDate.get(Calendar.YEAR)}-${entry.startDate.get(Calendar.MONTH) + 1}-${entry.startDate.get(Calendar.DAY_OF_MONTH)}"
            val hours = ((entry.endDate.timeInMillis - entry.startDate.timeInMillis) / (1000 * 60 * 60)).toFloat()
            dailyHours[date] = dailyHours.getOrDefault(date, 0f) + hours
        }
        return dailyHours
    }
}

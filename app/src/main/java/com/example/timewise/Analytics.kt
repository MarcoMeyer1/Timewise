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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        setupRecyclerView()

        val selectDateButton: Button = findViewById(R.id.selectDateButton)
        val chart: BarChart = findViewById(R.id.chart)


        updateToolbarColor("#FF6262")

        selectDateButton.setOnClickListener {
            selectDateRange {
                setupChart(chart)
                updateTimesheetEntries()
            }
        }

        // Load all data initially
        setupChart(chart)
        updateTimesheetEntries()

    }
    private fun initializeDateRange() {
        // Set initial dates here if needed
        startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }  // One week ago
        endDate = Calendar.getInstance()  // Today
        updateTimesheetEntries()  // Load initial data
    }
    private fun setupRecyclerView() {
        timesheetEntryList = findViewById(R.id.timesheetEntryList)
        timesheetEntryList.layoutManager = LinearLayoutManager(this)
        timesheetEntryList.adapter = TimesheetEntryAdapter(mutableListOf(), this::handleEntryClick)
    }
    private fun getTimesheetEntries(): List<TimesheetEntry> {
        val start = startDate
        val end = endDate

        // Define a flag to determine whether to use dummy data or actual data
        val useDummyData = true  // Set this to true to use dummy data, false to use actual timesheet data

        if (start == null || end == null) {
            Log.d("Analytics", "No start or end date set.")
            return emptyList()
        }

        // Select timesheets based on the dummy data flag
        val timesheets = if (useDummyData) TimesheetManager.getDummyTimesheets() else TimesheetManager.timesheets

        Log.d("Analytics", "Filtering from ${start.time} to ${end.time}")
        val filteredEntries = timesheets.flatMap { timesheet ->
            timesheet.entries?.filter { entry ->
                val startMatches = entry.startDate.timeInMillis >= start.timeInMillis
                val endMatches = entry.endDate.timeInMillis <= end.timeInMillis
                Log.d("Analytics", "Entry ${entry.name}: Start ${entry.startDate.time}, End ${entry.endDate.time}, Matches: $startMatches, $endMatches")
                startMatches && endMatches
            }.also { filtered ->
                Log.d("Analytics", "Entries for ${timesheet.name}: ${filtered?.size ?: 0}")
            } ?: emptyList()
        }

        Log.d("Analytics", "Total entries found: ${filteredEntries.size}")
        return filteredEntries
    }





    private fun handleEntryClick(entry: TimesheetEntry) {
        Log.d("Analytics", "Entry clicked: ${entry.name}, Photo URL: ${entry.photo}")
        entry.photo?.let { photoUrl ->
            Log.d("Analytics", "Showing photo for entry: ${entry.name}")
            showPhoto(photoUrl)
        } ?: run {
            Log.d("Analytics", "No picture to show for entry: ${entry.name}")
            Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateTimesheetEntries() {
        Log.d("Analytics", "Updating entries... Start Date: ${startDate?.time}, End Date: ${endDate?.time}")
        val entries = getTimesheetEntries()  // Get entries with updated dates
        Log.d("Analytics", "Entries count after update: ${entries.size}")
        (timesheetEntryList.adapter as TimesheetEntryAdapter).updateEntries(entries)
    }





    private fun showPhoto(photoUrl: String) {
        val photoUri = Uri.parse(photoUrl)
        val dialog = Dialog(this)  // Ensure the context is suitable for dialog creation.
        dialog.setContentView(R.layout.layout_photo_viewer)
        val photoView: ImageView = dialog.findViewById(R.id.photoView)
        photoView.setImageURI(photoUri)
        dialog.show()
    }


    private fun selectDateRange(onDatesSelected: () -> Unit) {
        val now = Calendar.getInstance()  // Define 'now' to use the current time
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
                updateTimesheetEntries()  // This ensures entries are updated immediately after dates are set
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
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

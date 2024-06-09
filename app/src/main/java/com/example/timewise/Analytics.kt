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
        startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }  // One week ago
        endDate = Calendar.getInstance()  // Today
        updateTimesheetEntries()
    }

    private fun setupRecyclerView() {
        timesheetEntryList = findViewById(R.id.timesheetEntryList)
        timesheetEntryList.layoutManager = LinearLayoutManager(this)
        timesheetEntryList.adapter = TimesheetEntryAdapter(mutableListOf(), this::handleEntryClick)
    }

    private fun getTimesheetEntries(): List<TimesheetManager.TimesheetEntry> {
        val start = startDate?.timeInMillis ?: return emptyList()
        val end = endDate?.timeInMillis ?: return emptyList()

        val entries = mutableListOf<TimesheetManager.TimesheetEntry>()
        val userId = TimesheetManager.getAuth().currentUser?.uid ?: return entries
        val db = TimesheetManager.getDatabase()

        DatabaseOperationsManager(this).fetchTimesheetEntriesBetweenDates(db, userId, "timesheetId", start, end) {
            entries.addAll(it)
            (timesheetEntryList.adapter as TimesheetEntryAdapter).updateEntries(entries)
        }

        return entries
    }

    private fun handleEntryClick(entry: TimesheetManager.TimesheetEntry) {
        entry.photo?.let { photoUri ->
            showPhoto(photoUri)
        } ?: run {
            Toast.makeText(this, "No picture", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTimesheetEntries() {
        val entries = getTimesheetEntries()
        (timesheetEntryList.adapter as TimesheetEntryAdapter).updateEntries(entries)
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
                updateTimesheetEntries()
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

    private fun getChartData(): MutableList<BarEntry> {
        val entries = mutableListOf<BarEntry>()
        // Populate the entries list with data
        return entries
    }
}

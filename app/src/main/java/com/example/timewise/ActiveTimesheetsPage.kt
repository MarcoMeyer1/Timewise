package com.example.timewise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import com.flask.colorpicker.ColorPickerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ActiveTimesheetsPage : BaseActivity(), TimeSheetAdapter.OnTimesheetEditListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeSheetAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_timesheets_page)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        updateToolbarColor("#5ECB35")
        setupRecyclerView()
        fetchAndDisplayTimesheets()

        val fab: FloatingActionButton = findViewById(R.id.fab_add_timesheet)
        fab.setOnClickListener {
            val intent = Intent(this, NewTimesheet::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView_timesheets)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TimeSheetAdapter(mutableListOf())
        adapter.setOnTimesheetEditListener(this)
        recyclerView.adapter = adapter
    }

    private fun fetchAndDisplayTimesheets() {
        TimesheetManager.fetchTimesheets { timesheets ->
            runOnUiThread {
                Log.d("ActiveTimesheetsPage", "Fetched timesheets: $timesheets")
                adapter.updateTimesheets(timesheets)
            }
        }
    }

    override fun onEditClicked(timesheet: TimesheetManager.Timesheet) {
        showEditDialog(timesheet)
    }

    private fun showEditDialog(timesheet: TimesheetManager.Timesheet) {
        val dialogView = layoutInflater.inflate(R.layout.edit_timesheet_dialog, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.editName)
        val colorPickerView: ColorPickerView = dialogView.findViewById(R.id.colorPicker)

        nameEditText.setText(timesheet.name)
        colorPickerView.setInitialColor(Color.parseColor(timesheet.color), true)

        AlertDialog.Builder(this)
            .setTitle("Edit Timesheet")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val newName = nameEditText.text.toString()
                val newColor = "#" + Integer.toHexString(colorPickerView.selectedColor).substring(2)
                val updatedTimesheet = timesheet.copy(name = newName, color = newColor)
                updateTimesheet(updatedTimesheet)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTimesheet(updatedTimesheet: TimesheetManager.Timesheet) {
        val index = adapter.getTimesheets().indexOfFirst { it.id == updatedTimesheet.id }
        if (index != -1) {
            val updatedList = adapter.getTimesheets().toMutableList()
            updatedList[index] = updatedTimesheet
            adapter.updateTimesheets(updatedList)
            adapter.notifyItemChanged(index)
        } else {
            Log.d("UpdateTimesheet", "Timesheet not found for update")
        }
    }
}

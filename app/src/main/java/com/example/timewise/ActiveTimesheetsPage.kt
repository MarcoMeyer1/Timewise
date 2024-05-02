package com.example.timewise

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flask.colorpicker.ColorPickerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActiveTimesheetsPage : BaseActivity(), TimesheetAdapter.OnTimesheetEditListener{


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimesheetAdapter
    var useDummy: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_timesheets_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updateToolbarColor("#5ECB35")
        setupRecyclerView()
        initializeData()


        val fab: FloatingActionButton = findViewById(R.id.fab_add_timesheet)
        fab.setOnClickListener {
            val intent = Intent(this, NewTimesheet::class.java)
            startActivity(intent)
            finish()
        }

    }
    override fun onEditClicked(timesheet: Timesheet) {
        showEditDialog(timesheet)
    }



    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView_timesheets)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TimesheetAdapter(TimesheetManager.timesheets, this)
        recyclerView.adapter = adapter
    }

    fun initializeData() {
        if (useDummy) {
            initializeDummyData()
        }
    }
    fun initializeDummyData() {
        var dummyTimesheets = TimesheetManager.getDummyTimesheets()
        TimesheetManager.addAllTimesheets(dummyTimesheets)
    }

    fun showEditDialog(timesheet: Timesheet) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.edit_timesheet_dialog, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.editName)
        val colorPickerView: ColorPickerView = dialogView.findViewById(R.id.colorPicker)

        nameEditText.setText(timesheet.name)
        colorPickerView.setInitialColor(Color.parseColor(timesheet.colorHex), true)

        AlertDialog.Builder(this)
            .setTitle("Edit Timesheet")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val newName = nameEditText.text.toString()
                val newColor = "#" + Integer.toHexString(colorPickerView.selectedColor).substring(2)
                val updatedTimesheet = timesheet.copy(name = newName, colorHex = newColor)
                updateTimesheet(updatedTimesheet)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun updateTimesheet(updatedTimesheet: Timesheet) {
        val index = TimesheetManager.timesheets.indexOfFirst { it.id == updatedTimesheet.id }
        if (index != -1) {
            TimesheetManager.timesheets[index] = updatedTimesheet
            adapter.notifyItemChanged(index)
        } else {
            Log.d("UpdateTimesheet", "Timesheet not found for update")
        }
    }

}

class TimesheetAdapter(
    private val timesheets: List<Timesheet>,
    private val onEditListener: OnTimesheetEditListener
) : RecyclerView.Adapter<TimesheetAdapter.TimesheetViewHolder>() {
    interface OnTimesheetEditListener {
        fun onEditClicked(timesheet: Timesheet)
    }
    class TimesheetViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        val nameTextView: TextView = cardView.findViewById(R.id.timesheet_name)
        val editButton: ImageView = cardView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_card, parent, false) as CardView
        return TimesheetViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: TimesheetViewHolder, position: Int) {
        val timesheet = timesheets[position]
        holder.nameTextView.text = timesheet.name
        holder.cardView.setCardBackgroundColor(Color.parseColor(timesheet.colorHex))
        holder.editButton.setOnClickListener {
            onEditListener.onEditClicked(timesheet)
        }
    }

    override fun getItemCount() = timesheets.size


}



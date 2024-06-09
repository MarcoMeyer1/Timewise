package com.example.timewise

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.timewise.TimesheetManager.Timesheet
import com.example.timewise.TimesheetManager.TimesheetEntry
import java.text.SimpleDateFormat
import java.util.*

class EventCreationDialogFragment : DialogFragment() {
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var selectedImageUri: Uri? = null

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var dbManager: DatabaseOperationsManager
    private var timesheets: List<Timesheet> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        dbManager = DatabaseOperationsManager(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.popup_layout, null)
        val txtEventName = view?.findViewById<EditText>(R.id.txtEventName)
        val btnStartDate = view?.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = view?.findViewById<Button>(R.id.btnEndDate)
        val allDaySwitch = view?.findViewById<Switch>(R.id.allDaySwitch)
        val categorySpinner = view?.findViewById<Spinner>(R.id.categorySpinner)
        val btnAddPhoto = view?.findViewById<Button>(R.id.btnAddPhoto)
        val btnCreateEvent = view?.findViewById<Button>(R.id.btnCreateEvent)

        btnStartDate?.setOnClickListener { showDateTimePickerDialog(true, btnStartDate) }
        btnEndDate?.setOnClickListener { showDateTimePickerDialog(false, btnEndDate) }
        btnAddPhoto?.setOnClickListener { openPhotoPicker() }

        btnCreateEvent?.setOnClickListener {
            val eventName = txtEventName?.text.toString()
            val allDay = allDaySwitch?.isChecked ?: false
            val category = categorySpinner?.selectedItem.toString()
            val timesheet = timesheets.find { it.name == category }

            if (timesheet != null) {
                val timesheetEntry = TimesheetEntry(eventName, startDate!!, endDate!!, allDay, category, selectedImageUri)
                val userId = TimesheetManager.getAuth().currentUser?.uid ?: return@setOnClickListener

                dbManager.createTimesheetEntry(
                    TimesheetManager.getDatabase(),
                    userId,
                    timesheet.id,
                    UUID.randomUUID().toString(),
                    eventName,
                    startDate!!.timeInMillis,
                    endDate!!.timeInMillis,
                    allDay,
                    selectedImageUri?.toString()
                )

                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Timesheet not found", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }

        fetchTimesheets { fetchedTimesheets ->
            timesheets = fetchedTimesheets
            categorySpinner?.let { initializeCategorySpinner(it) }
        }

        builder.setView(view)
        return builder.create()
    }

    private fun openPhotoPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun fetchTimesheets(callback: (List<Timesheet>) -> Unit) {
        TimesheetManager.fetchTimesheets { fetchedTimesheets ->
            callback(fetchedTimesheets)
        }
    }

    private fun initializeCategorySpinner(categorySpinner: Spinner) {
        val categories = timesheets.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun showDateTimePickerDialog(isStartDate: Boolean, dateButton: Button) {
        val context = requireContext()
        val calendar = if (isStartDate) startDate ?: Calendar.getInstance() else endDate ?: Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        val formattedDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                        dateButton.text = formattedDateTime

                        if (isStartDate) startDate = calendar else endDate = calendar
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

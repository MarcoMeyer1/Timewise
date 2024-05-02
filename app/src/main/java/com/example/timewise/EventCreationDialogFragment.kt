import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import com.example.timewise.TimesheetEntry
import com.example.timewise.TimesheetManager
import com.example.timewise.TimesheetRepository
import java.text.SimpleDateFormat
import java.util.*
import com.example.timewise.R;

class EventCreationDialogFragment : DialogFragment() {
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var selectedPhotoPath: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.popup_layout, null)

        val txtEventName = view?.findViewById<EditText>(R.id.txtEventName)
        val btnStartDate = view?.findViewById<Button>(R.id.btnStartDate)
        val btnEndDate = view?.findViewById<Button>(R.id.btnEndDate)
        val allDaySwitch = view?.findViewById<Switch>(R.id.allDaySwitch)
        val categorySpinner = view?.findViewById<Spinner>(R.id.categorySpinner)
        val btnAddPhoto = view?.findViewById<Button>(R.id.btnAddPhoto)
        val btnCreateEvent = view?.findViewById<Button>(R.id.btnCreateEvent)

        btnStartDate?.setOnClickListener {
            showDateTimePickerDialog(true, btnStartDate)
        }
        btnEndDate?.setOnClickListener {
            showDateTimePickerDialog(false, btnEndDate)
        }
        btnAddPhoto?.setOnClickListener {
            // Implement photo picker functionality
        }
        btnCreateEvent?.setOnClickListener {
            val eventName = txtEventName?.text.toString()
            val allDay = allDaySwitch?.isChecked ?: false
            val category = categorySpinner?.selectedItem.toString()

            val timesheetEntry = TimesheetEntry(eventName, startDate!!, endDate!!, allDay, category, selectedPhotoPath)
            val dummyTimesheet = TimesheetRepository.getDummyTimesheet()
            TimesheetManager.addTimesheetEntry(dummyTimesheet.id, timesheetEntry)
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    private fun showDateTimePickerDialog(isStartDate: Boolean, dateButton: Button) {
        val calendar = if (isStartDate) startDate ?: Calendar.getInstance() else endDate ?: Calendar.getInstance()
        DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            // Optionally add time picker here
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            dateButton.text = formattedDate
            if (isStartDate) startDate = calendar else endDate = calendar
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}

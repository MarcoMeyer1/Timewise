package com.example.timewise

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class DailyProgress : BaseActivity() {

    private lateinit var databaseOperationsManager: DatabaseOperationsManager
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId: String? = auth.currentUser?.uid

    private lateinit var tvStreakNumber: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_daily_progress)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        updateToolbarColor("#FF8C00")

        // Initialize the views
        tvStreakNumber = findViewById(R.id.tvStreakNumber)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        tvMessage = findViewById(R.id.tvMessage)

        databaseOperationsManager = DatabaseOperationsManager(this)

        userId?.let {
            fetchUserGoals(it)
            fetchDailyProgress(it)

        }
    }

    private fun fetchDailyProgress(userId: String) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        databaseOperationsManager.fetchTimesheetEntriesForDate(FirebaseDatabase.getInstance(), userId, today) { entries ->
            val totalHours = entries.sumOf { (it.endDate.timeInMillis - it.startDate.timeInMillis) / 3600000.0 }
            val hoursRemaining = userGoals.first - totalHours
            tvStreakNumber.text = calculateStreak(entries).toString()
            progressBar.progress = ((totalHours / userGoals.first) * 100).coerceIn(0.0, 100.0).toInt()
            progressText.text = if (hoursRemaining > 0) {
                "${String.format("%.2f", hoursRemaining)} hours to go"
            } else {
                "Goal achieved!"
            }

            databaseOperationsManager.fetchUserName(FirebaseDatabase.getInstance(), userId) { displayName ->
                tvMessage.text = "You are doing really great, $displayName!"
            }
        }
    }




    private fun fetchUserGoals(userId: String) {
        databaseOperationsManager.fetchUserGoals(FirebaseDatabase.getInstance(), userId) { minHours, maxHours ->
            userGoals = Pair(minHours.toFloat(), maxHours.toFloat())
        }
    }


    private fun calculateStreak(entries: List<TimesheetManager.TimesheetEntry>): Int {
        val today = Calendar.getInstance()
        var streak = 0

        for (i in 0 until 7) {
            val day = today.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, -i)
            if (entries.any { isSameDay(it.startDate, day) }) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        private var userGoals: Pair<Float, Float> = Pair(0f, 0f)
    }
}

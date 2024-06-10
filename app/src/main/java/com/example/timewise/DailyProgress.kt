package com.example.timewise

import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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

    // Icons for each day of the week
    private lateinit var ivMonday: ImageView
    private lateinit var ivTuesday: ImageView
    private lateinit var ivWednesday: ImageView
    private lateinit var ivThursday: ImageView
    private lateinit var ivFriday: ImageView
    private lateinit var ivSaturday: ImageView
    private lateinit var ivSunday: ImageView

    private var currentStreak = 0
    private var lastStreakUpdate = 0L

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

        // Initialize the day icons
        ivMonday = findViewById(R.id.ivMonday)
        ivTuesday = findViewById(R.id.ivTuesday)
        ivWednesday = findViewById(R.id.ivWednesday)
        ivThursday = findViewById(R.id.ivThursday)
        ivFriday = findViewById(R.id.ivFriday)
        ivSaturday = findViewById(R.id.ivSaturday)
        ivSunday = findViewById(R.id.ivSunday)

        databaseOperationsManager = DatabaseOperationsManager(this)

        userId?.let {
            fetchUserGoals(it)
            fetchStreak(it)
            fetchLastStreakUpdate(it)
            fetchDailyProgress(it)
        }
    }

    private fun fetchStreak(userId: String) {
        databaseOperationsManager.fetchStreak(FirebaseDatabase.getInstance(), userId) { streak ->
            currentStreak = streak
            tvStreakNumber.text = streak.toString()
        }
    }

    private fun updateStreak(userId: String, streak: Int) {
        databaseOperationsManager.updateStreak(FirebaseDatabase.getInstance(), userId, streak)
    }

    private fun fetchLastStreakUpdate(userId: String) {
        databaseOperationsManager.fetchLastStreakUpdate(FirebaseDatabase.getInstance(), userId) { lastUpdate ->
            lastStreakUpdate = lastUpdate
        }
    }

    private fun updateLastStreakUpdate(userId: String, date: Long) {
        databaseOperationsManager.updateLastStreakUpdate(FirebaseDatabase.getInstance(), userId, date)
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
            progressBar.progress = ((totalHours / userGoals.first) * 100).coerceIn(0.0, 100.0).toInt()
            progressText.text = if (hoursRemaining > 0) {
                "${String.format("%.2f", hoursRemaining)} hours to go"
            } else {
                "Goal achieved!"
            }

            databaseOperationsManager.fetchUserName(FirebaseDatabase.getInstance(), userId) { displayName ->
                tvMessage.text = "You are doing really great, $displayName!"
            }

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (totalHours >= userGoals.first && lastStreakUpdate < todayStart) {
                currentStreak++
                updateStreak(userId, currentStreak)
                updateLastStreakUpdate(userId, todayStart)
            } else if (totalHours < userGoals.first && lastStreakUpdate < todayStart) {
                currentStreak = 0
                updateStreak(userId, currentStreak)
                updateLastStreakUpdate(userId, todayStart)
            }

            tvStreakNumber.text = currentStreak.toString()
            updateDayIcons(entries, totalHours)
        }
    }

    private fun updateDayIcons(entries: List<TimesheetManager.TimesheetEntry>, totalHours: Double) {
        val today = Calendar.getInstance()
        val daysOfWeek = arrayOf(
            Pair(ivSunday, Calendar.SUNDAY),
            Pair(ivMonday, Calendar.MONDAY),
            Pair(ivTuesday, Calendar.TUESDAY),
            Pair(ivWednesday, Calendar.WEDNESDAY),
            Pair(ivThursday, Calendar.THURSDAY),
            Pair(ivFriday, Calendar.FRIDAY),
            Pair(ivSaturday, Calendar.SATURDAY)
        )

        daysOfWeek.forEach { (imageView, dayOfWeek) ->
            val day = today.clone() as Calendar
            day.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            if (isSameDay(day, today)) {
                // Check today's progress
                if (totalHours >= userGoals.first) {
                    imageView.setImageResource(R.drawable.ic_check_circle) // Met progression
                } else {
                    imageView.setImageResource(R.drawable.ic_missed_circle) // Missed progression
                }
            } else if (entries.any { isSameDay(it.startDate, day) }) {
                imageView.setImageResource(R.drawable.ic_check_circle) // Met progression
            } else {
                imageView.setImageResource(R.drawable.ic_missed_circle) // Missed progression
            }
        }
    }

    private fun fetchUserGoals(userId: String) {
        databaseOperationsManager.fetchUserGoals(FirebaseDatabase.getInstance(), userId) { minHours, maxHours ->
            userGoals = Pair(minHours.toFloat(), maxHours.toFloat())
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        private var userGoals: Pair<Float, Float> = Pair(0f, 0f)
    }
}

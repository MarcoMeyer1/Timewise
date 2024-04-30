package com.example.timewise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.GridView

class EventsPageCalenderView : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_page_calender_view)
        val daysOfMonth = mutableListOf<String>()
        for (i in 1..31) {
            daysOfMonth.add(i.toString())
        }

    }
}
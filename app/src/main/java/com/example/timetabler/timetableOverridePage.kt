package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class timetableOverridePage : AppCompatActivity() {

    private var generateTimetable = GenerateTimetable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable_override_page)
        val selectedStaffMap = intent.getSerializableExtra("selectedStaffMap") as? HashMap<String, String>

        // Use the map as needed
        if(selectedStaffMap != null)
        {
            generateTimetable.timetable1(selectedStaffMap){result ->

            }
        }



    }
}
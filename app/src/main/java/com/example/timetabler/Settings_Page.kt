package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.ActionBarDrawerToggle

class Settings_Page : AppCompatActivity() {

    private lateinit var toggle1: ToggleButton
    private lateinit var toggle2: ToggleButton
    private lateinit var toggle3: ToggleButton
    private lateinit var priorityList : TextView
    private lateinit var deleteData : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)

        toggle1 = findViewById(R.id.notifToggle)
        toggle2 = findViewById(R.id.darkModeToggle)
        toggle3 = findViewById(R.id.languageToggle)
        deleteData = findViewById(R.id.deleteStaffData)
        priorityList = findViewById(R.id.priorityListBtn)
        deleteData.setOnClickListener{
            //do stuff
        }
        priorityList.setOnClickListener{
            //do something
        }



    }
}
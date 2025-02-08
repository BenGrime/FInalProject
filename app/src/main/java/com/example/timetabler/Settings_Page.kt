package com.example.timetabler

import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class Settings_Page : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var toggle1: ToggleButton
    private lateinit var toggle2: ToggleButton
    private lateinit var toggle3: ToggleButton
    private lateinit var priorityList : LinearLayout
    private lateinit var savePreferences : LinearLayout
    private lateinit var settingsGrid : GridLayout
    private lateinit var deleteData : LinearLayout
    private lateinit var priorityText : TextView
    private lateinit var backBtn : ImageView
    private lateinit var logoutBtn : LinearLayout

    private lateinit var auth: FirebaseAuth

    private var spinnerList : ArrayList<Spinner> = ArrayList()

    var fh = FirebaseHandler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)
        auth = Firebase.auth
        savePreferences = findViewById(R.id.savePreferences)
        backBtn = findViewById(R.id.backBtnSettings)
        backBtn.setOnClickListener{
            finish()
        }
        toggle1 = findViewById(R.id.notifToggle)
        toggle2 = findViewById(R.id.darkModeToggle)
        toggle3 = findViewById(R.id.languageToggle)
//        logoutBtn = findViewById(R.id.logoutBtn)
//        logoutBtn.setOnClickListener{
//            auth.signOut()
//            val intent = Intent(this, LoginScreen::class.java)
//            startActivity(intent)
//            finish()
//        }
        settingsGrid = findViewById(R.id.settingsGrid)
        deleteData = findViewById(R.id.deleteStaffData)
        priorityList = findViewById(R.id.priorityListBtn)
        priorityText = findViewById(R.id.priorityText)
        deleteData.setOnClickListener{
            //do stuff
        }
        priorityList.setOnClickListener{
            settingsGrid.visibility = if (settingsGrid.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if(settingsGrid.visibility == View.GONE){
                priorityText.text = "Edit Priority List"
            }
            else
            {
                priorityText.text = "Close Priority List"
            }
        }
        savePreferences.setOnClickListener{
            //save stuff
            //get all rides, using the ride add it to a pair with "5" add that to board
            var finishedBoard : ArrayList<Pair<String, Int>> = ArrayList()
            var counter = 0

            fh.getAllRides {
                for(r in it)
                {
                    var selected = spinnerList[counter].selectedItem as? Int

                    finishedBoard.add(Pair(r.Name, selected) as Pair<String, Int>)
                    counter++
                }
                val firebaseBoard = finishedBoard.map { innerList -> mapOf("ride" to innerList.first, "value" to innerList.second) } as ArrayList<Map<String, Int>>
                firebaseBoard.forEach { row -> println("Row: $row, Type: ${row::class.simpleName}") }
                db.collection("RidePriority").document("1").set(mapOf("priorityList" to firebaseBoard)).addOnSuccessListener{
                    Toast.makeText(this, "UPDATED", Toast.LENGTH_SHORT).show()

                }
            }
        }

        fh.getPriority{result ->
            for(pair in result)
            {
                val name = pair.first
                val value = pair.second

                val rideLinearLayout = LinearLayout(this@Settings_Page).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 10, 8, 10) // Set margin for the layout
                    }
                    orientation = LinearLayout.HORIZONTAL // Horizontal orientation
                    gravity = Gravity.CENTER_VERTICAL // Center contents vertically
                    setPadding(16, 16, 16, 16) // Padding inside the layout
                    setBackgroundResource(R.drawable.rounded_rectangle) // Rounded rectangle background
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.white) // Tint background
                }

                val rideTextView = TextView(this@Settings_Page).apply {
                    text = name // Set the ride name
                    textSize = 16f // Text size in SP
                    setTextColor(ContextCompat.getColor(context, R.color.black)) // Set text color
                    setPadding(8, 8, 8, 8) // Padding for the TextView
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                val spinner = Spinner(this@Settings_Page)
                val selection = listOf(1, 2, 3)
                val adapter = ArrayAdapter(this@Settings_Page, android.R.layout.simple_spinner_item, selection)
                adapter.setDropDownViewResource(R.layout.spinner_custom_dropdown)
                spinner.adapter = adapter
                val index = selection.indexOf(value)
                if (index >= 0) { // Ensure the value exists in the selection
                    spinner.setSelection(index)
                }

                // Add TextView and ImageView to the LinearLayout
                rideLinearLayout.addView(rideTextView)
                rideLinearLayout.addView(spinner)

                spinnerList.add(spinner)

                // Optionally add this LinearLayout to a parent layout (like GridLayout)
                settingsGrid.addView(rideLinearLayout)
            }
        }
    }
}
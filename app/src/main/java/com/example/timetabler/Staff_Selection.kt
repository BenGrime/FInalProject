package com.example.timetabler

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class Staff_Selection : AppCompatActivity() {

    private val db = Firebase.firestore
    var fh = FirebaseHandler()

    private lateinit var backBtn : ImageView
    private lateinit var cancelBtn : Button
    private lateinit var nextBtn : Button
    private lateinit var gridLayout: GridLayout
    private lateinit var staffViewSpinner : Spinner

    private var nameList = ArrayList<String>()
    private var allStaff = ArrayList<Staff>()
    private var filteredNameList = ArrayList<String>()
    private val buttons = mutableListOf<Button>() // List to keep references to all buttons
    private val selectedStaff = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_selection)

        gridLayout = findViewById(R.id.gridLayout)
        staffViewSpinner = findViewById(R.id.staffViewSpinner)

        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener(View.OnClickListener {
            finish()
        })
        cancelBtn = findViewById(R.id.StaffSelectCancel)
        cancelBtn.setOnClickListener(View.OnClickListener {
            finish()
        })
        nextBtn = findViewById(R.id.StaffSelectConfirm)
        nextBtn.setOnClickListener(View.OnClickListener {
            //go to next page, pass list of chosen staff through
            val selectedStaffNames = ArrayList<String>() // List to store the names of selected staff

            for (button in buttons) {
                // Check if the button's background matches the green drawable
                println(selectedStaff.size)
//                if (button.background.constantState == ContextCompat.getDrawable(this, R.drawable.selected_green)?.constantState) {
//                    for(staff in allStaff)
//                    {
//                        if(staff.Name == button.text.toString()){
//                            selectedStaffNames.add(staff.Name)
//                        }
//
//                    }
//
//                }

            }
            val intent = Intent(this, RequirementsScreen::class.java)
//            intent.putExtra("staffSelected", selectedStaffNames)  // Pass the list of Staff
            intent.putExtra("staffSelected", ArrayList(selectedStaff))  // Pass the list of Staff
            startActivity(intent)
            finish()
        })

        // Spinner setup
        val spinnerList = listOf("All Staff", "SRO", "Fairground", "Attendants")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,  // Default spinner layout
            spinnerList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Dropdown style
        staffViewSpinner.adapter = adapter

        // Initial selected category
        var selected = "All Staff"
        staffViewSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selected = spinnerList[position]
                    filterAndDisplayStaff(selected) // Filter staff based on selection
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Optional: Handle no selection
                }
            }
        showLoading()

        // Fetch staff from Firebase
        fh.getAllStaff { result ->
            hideLoading()
            for (staff in result) {
                allStaff.add(staff)
                nameList.add(staff.Name)
            }

            // Initially display all staff
            filteredNameList.addAll(nameList)
            displayStaff(filteredNameList)
        }
    }

    private fun filterAndDisplayStaff(selectedCategory: String) {
        filteredNameList.clear()
        buttons.clear()

        val formattedCategory = if (selectedCategory == "Attendants") {
            selectedCategory.dropLast(1).trim() // Normalize to lowercase
        } else {
            selectedCategory
        }

        // Filter the list based on the selected category
        when (formattedCategory) {
            "All Staff" -> filteredNameList.addAll(allStaff.map { it.Name }) // Add all names
            else -> filteredNameList.addAll(allStaff.filter { it.Category == formattedCategory }.map { it.Name })
        }

        // Update the grid with the filtered list
        displayStaff(filteredNameList)
    }

    private fun displayStaff(filteredList: List<String>) {
        // Clear existing buttons
        gridLayout.removeAllViews()

        // Add buttons for filtered list
        for (name in filteredList) {
            val button = Button(this).apply {
                text = name
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // Equal distribution
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                    setMargins(16, 16, 16, 16)

                }
                isSingleLine = false // Allow multi-line text
                maxLines = 2 // Limit text to 2 lines
                setEllipsize(TextUtils.TruncateAt.END)
                // Set initial background color to white
                // Set initial background color based on selection state
                if (selectedStaff.contains(name)) {
                    background = ContextCompat.getDrawable(this@Staff_Selection, R.drawable.selected_green)
                } else {
                    background = ContextCompat.getDrawable(this@Staff_Selection, R.drawable.rounded_rectangle)
                }
            }
            button.setOnClickListener {
                if (selectedStaff.contains(name)) {
                    selectedStaff.remove(name) // Deselect staff
                    button.setBackgroundResource(R.drawable.rounded_rectangle)
                } else {
                    selectedStaff.add(name) // Select staff
                    button.setBackgroundResource(R.drawable.selected_green)
                }
            }
            buttons.add(button) // Add button to the list
            gridLayout.addView(button)
        }
    }

    private fun showLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.GONE
    }
}
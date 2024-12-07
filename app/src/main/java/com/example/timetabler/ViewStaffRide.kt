package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class ViewStaffRide : AppCompatActivity() {

    private lateinit var backBtnViewStaff: ImageView
    private lateinit var staffSelectView: Spinner
    private lateinit var staffMemberTxt: TextView
    private lateinit var editBtn: ImageView
    private lateinit var categoryTxt: TextView
    private lateinit var dobTxt: TextView
    private lateinit var prevRideTxt: TextView
    private lateinit var gridLayout: GridLayout

    var fh = FirebaseHandler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_staff_ride)

        // Initialize views using findViewById
        backBtnViewStaff = findViewById(R.id.backBtnViewStaff)
        staffSelectView = findViewById(R.id.staffSelectView)
        staffMemberTxt = findViewById(R.id.staffMemberTxt)
        editBtn = findViewById(R.id.editBtn)
        categoryTxt = findViewById(R.id.categoryTxt)
        dobTxt = findViewById(R.id.dobTxt)
        prevRideTxt = findViewById(R.id.prevRideTxt)
        gridLayout = findViewById(R.id.gridLayout)

        var staffList = ArrayList<Staff>()
        var staffNames: ArrayList<String> = ArrayList()

        backBtnViewStaff.setOnClickListener(View.OnClickListener {
            finish()
        })

        fh.getAllStaff { staffArray ->

            staffList = staffArray as ArrayList<Staff>
            staffNames.add("SelectStaff")
            for(staff in staffList){
                staffNames.add(staff.Name)
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, staffNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            staffSelectView.adapter = adapter
        }

        staffSelectView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ensure that position is valid (it should be greater than 0)
                if (position > 0) {
                    val selectedStaff = staffList[position - 1] // Account for the first "Select Staff" option

                    // Use the selected staff object to populate the TextViews
                    staffMemberTxt.text = selectedStaff.Name
                    categoryTxt.text = "Category: ${selectedStaff.Category}" // Assuming 'Category' is a property


                    val formattedDoB = formatDate(selectedStaff.DoB)
                    dobTxt.text = "Date of Birth: $formattedDoB"


                    prevRideTxt.text = if (selectedStaff.PreviousRide.isEmpty()) {
                        "No previous ride available"
                    } else {
                        "Previous Ride: ${selectedStaff.PreviousRide}"
                    }

                    val ridesTrained = selectedStaff.RidesTrained // List of rides trained on
                    gridLayout.removeAllViews()

                    // Create a map to track the base ride names and whether both "Op" and "Att" exist
                    val rideNamesMap = mutableMapOf<String, MutableSet<String>>()

                    // Process each ride name
                    for (ride in ridesTrained) {
                        val baseRideName = getBaseRideName(ride.toString()) // Remove "Op" or "Att" if present

                        // Add the full ride name (including Op or Att) to the set for that base name
                        if (!rideNamesMap.containsKey(baseRideName)) {
                            rideNamesMap[baseRideName] = mutableSetOf()
                        }
                        rideNamesMap[baseRideName]?.add(ride.toString())
                    }

                    // Now, for each base ride name, check if both "Op" and "Att" exist and create a TextView
                    for ((baseName, rideSet) in rideNamesMap) {
                        // Assuming you have a way to determine if a ride requires an age of 16 or more to operate
                        val minAgeToOperate = 16 // or retrieve it dynamically if needed (e.g. from a Ride object)

                        // Determine which ride name to add
                        val rideToAdd = if (rideSet.contains("${baseName} Op") && rideSet.contains("${baseName} Att")) {
                            baseName // Only show the base name if both "Op" and "Att" are in the list
                        } else {
                            // Check if the ride requires a minimum age of 16 and strip "Op" if necessary
                            if (rideSet.contains("${baseName} Op") && minAgeToOperate >= 16) {
                                baseName // Strip off " Op" since the ride requires age 16 or more
                            } else {
                                // Show the first available version (Op or Att) if not both are available
                                rideSet.first()
                            }
                        }

                        // Create a new TextView for the ride name
                        val rideTextView = TextView(this@ViewStaffRide).apply {
                            text = rideToAdd // Set the text for the ride
                            textSize = 16f // Set text size (you can adjust this)
                            setTextColor(resources.getColor(android.R.color.white)) // Set text color
                            setPadding(16, 8, 16, 8) // Add padding (left, top, right, bottom)
                            layoutParams = GridLayout.LayoutParams().apply {
                                // Add margin between cells
                                setMargins(8, 8, 8, 8) // (left, top, right, bottom) margins for the TextView
                                width = GridLayout.LayoutParams.WRAP_CONTENT
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                            }
                        }


                        // Add the created TextView to the GridLayout
                        gridLayout.addView(rideTextView)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Handle the case when no staff is selected
            }
        }
    }
    private fun formatDate(timestamp: Timestamp): String {
        return try {
            // Convert the Timestamp to a Date object
            val date = timestamp.toDate()

            // Format the Date object into dd/MM/yyyy format
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(date) // Return the formatted date
        } catch (e: Exception) {
            e.printStackTrace()
            timestamp.toDate().toString() // If parsing fails, return the original date string
        }
    }

    fun getBaseRideName(ride: String): String {
        return when {
            ride.endsWith(" Op", ignoreCase = true) -> ride.removeSuffix(" Op").trim()
            ride.endsWith(" Att", ignoreCase = true) -> ride.removeSuffix(" Att").trim()
            else -> ride
        }
    }
}
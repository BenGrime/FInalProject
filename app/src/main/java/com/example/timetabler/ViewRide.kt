package com.example.timetabler

import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ViewRide : AppCompatActivity() {

    private lateinit var backBtnViewRide: ImageView
    private lateinit var titleTxt: TextView
    private lateinit var rideSelectView: Spinner
    private lateinit var rideNameTxt: TextView
    private lateinit var editBtnRide: TextView
    private lateinit var minAgeToAttTxt: TextView
    private lateinit var minAgeToOpTxt: TextView
    private lateinit var minNumAttTxt: TextView
    private lateinit var minNumOpTxt: TextView
    private lateinit var prefNumAttTxt: TextView
    private lateinit var prefNumOpTxt: TextView
    private lateinit var openTxt: TextView
    private lateinit var gridLayoutRideView: GridLayout
    private lateinit var showStaff: TextView

    var fh = FirebaseHandler()
    var selectedRide: Ride? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_ride)


        val rideSelected = intent.getStringExtra("rideName")

        // Initialize views
        backBtnViewRide = findViewById(R.id.backBtnViewRide)
        titleTxt = findViewById(R.id.titleTxt)
        rideSelectView = findViewById(R.id.rideSelectView)
        rideNameTxt = findViewById(R.id.rideNameTxt)
        editBtnRide = findViewById(R.id.editBtnRide)
        minAgeToAttTxt = findViewById(R.id.minAgeToAttTxt)
        minAgeToOpTxt = findViewById(R.id.minAgeToOpTxt)
        minNumAttTxt = findViewById(R.id.minNumAttTxt)
        minNumOpTxt = findViewById(R.id.MinNumOpTxt)
        prefNumAttTxt = findViewById(R.id.PrefNumAttTxt)
        prefNumOpTxt = findViewById(R.id.PrefNumOpTxt)
        openTxt = findViewById(R.id.openTxt)
        gridLayoutRideView = findViewById(R.id.gridLayoutRideView)
        showStaff = findViewById(R.id.showStaff)
        showStaff.setOnClickListener{
            gridLayoutRideView.visibility = if (gridLayoutRideView.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if(gridLayoutRideView.visibility == View.GONE){
                showStaff.text = "Show Rides Trained"
            }
            else
            {
                showStaff.text = "Hide Rides Trained"
            }
        }

        val rideList = ArrayList<Ride>()
        val rideNames: ArrayList<String> = ArrayList()

        backBtnViewRide.setOnClickListener {
            finish()
        }

        editBtnRide.setOnClickListener {
            selectedRide?.let {
                val intent = Intent(this@ViewRide, editRide::class.java)
                intent.putExtra("rideObject", selectedRide)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Please select a ride to edit", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch all rides from Firebase
        fh.getAllRides { rideArray ->
            rideList.addAll(rideArray)
            rideNames.add("Select Ride")
            rideArray.forEach { ride ->
                rideNames.add(ride.Name)
            }
            val adapter = ArrayAdapter(this, R.layout.spinner_custom_dropdown, rideNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            rideSelectView.adapter = adapter

            // If rideName is passed, automatically select the corresponding ride
            if (!rideSelected.isNullOrEmpty()) {
                val selectedRideIndex = rideNames.indexOf(rideSelected)
                if (selectedRideIndex > 0) { // Make sure it's not "Select Ride"
                    rideSelectView.setSelection(selectedRideIndex)
                }
            }
        }

        // Handle ride selection
        rideSelectView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedRide = rideList[position - 1]  // Account for the "Select Ride" option
                    editBtnRide.isEnabled = true
                    editBtnRide.alpha = 1f

                    // Display ride details
                    selectedRide?.let { ride ->
                        rideNameTxt.text = ride.Name
                        minAgeToAttTxt.text = "Minimum age to attend: " + ride.minAgeToAttend
                        minAgeToOpTxt.text = "Minimum age to operate: " + ride.minAgeToOperate
                        minNumAttTxt.text = "Minimum number of attendants: " + ride.minNumAtt
                        minNumOpTxt.text = "Minimum number of operators: " + ride.minNumOp
                        prefNumAttTxt.text = "Preferred number of attends: " + ride.prefNumAtt
                        prefNumOpTxt.text = "Preferred number of operators: " + ride.prefNumOp
                        openTxt.text = "Is the ride Open: " + if (ride.open) "Yes" else "No"

                        // Clear previous staff list before adding new one
                        gridLayoutRideView.removeAllViews()

                        // Add trained staff to GridLayout
                        ride.staffTrained.forEach { staff ->
                            var staffName = staff.toString()
                            if(ride.prefNumAtt > 0 && ride.prefNumOp > 0)
                            {
                                //if ride has Ops and Attendants, keep the Att and Op
                            }
                            else
                            {
                                staffName = when {
                                    staffName.endsWith(" Op", ignoreCase = true) -> staffName.removeSuffix(" Op")
                                    staffName.endsWith(" Att", ignoreCase = true) -> staffName.removeSuffix(" Att")
                                    else -> staffName
                                }
                            }

                            val rideLinearLayout = LinearLayout(this@ViewRide).apply {
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
                                backgroundTintList = ContextCompat.getColorStateList(context, R.color.purple) // Tint background
                            }

                            // Create the TextView for the ride name
                            val rideTextView = TextView(this@ViewRide).apply {
                                text = staffName // Set the ride name
                                textSize = 16f // Text size in SP
                                setTextColor(ContextCompat.getColor(context, R.color.white)) // Set text color
                                setPadding(8, 8, 8, 8) // Padding for the TextView
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            }

                            // Create the ImageView for the icon
                            val rideImageView = ImageView(this@ViewRide).apply {
                                setImageResource(R.drawable.eye_icon) // Set the drawable resource
                                contentDescription = "View Ride" // Set content description for accessibility
                                layoutParams = LinearLayout.LayoutParams(
                                    70 ,
                                    70
                                ).apply { marginEnd = 16 }
                                setColorFilter(ContextCompat.getColor(context, android.R.color.white), PorterDuff.Mode.SRC_IN)
                                setOnClickListener{
                                    staffName.let {
                                        val cleanedStaffName = when {
                                            it.endsWith(" Op", ignoreCase = true) -> it.removeSuffix(" Op")
                                            it.endsWith(" Att", ignoreCase = true) -> it.removeSuffix(" Att")
                                            else -> it // If neither suffix is found, leave it as is
                                        }
                                        val intent = Intent(this@ViewRide, ViewStaff::class.java)
                                        intent.putExtra("staffName", cleanedStaffName)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }

                            // Add TextView and ImageView to the LinearLayout
                            rideLinearLayout.addView(rideTextView)
                            rideLinearLayout.addView(rideImageView)

                            // Optionally add this LinearLayout to a parent layout (like GridLayout)
                            gridLayoutRideView.addView(rideLinearLayout)
                        }
                    }
                } else {
                    // Disable the edit button when no ride is selected
                    editBtnRide.isEnabled = false
                    editBtnRide.alpha = 0.5f
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Disable the edit button if no ride is selected
                editBtnRide.isEnabled = false
                editBtnRide.alpha = 0.5f
            }
        }
    }
}

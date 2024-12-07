package com.example.timetabler

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ViewRide : AppCompatActivity() {

    private lateinit var backBtnViewRide: ImageView
    private lateinit var titleTxt: TextView
    private lateinit var rideSelectView: Spinner
    private lateinit var rideNameTxt: TextView
    private lateinit var editBtnRide: ImageView
    private lateinit var minAgeToAttTxt: TextView
    private lateinit var minAgeToOpTxt: TextView
    private lateinit var minNumAttTxt: TextView
    private lateinit var minNumOpTxt: TextView
    private lateinit var prefNumAttTxt: TextView
    private lateinit var prefNumOpTxt: TextView
    private lateinit var openTxt: TextView
    private lateinit var gridLayoutRideView: GridLayout

    var fh = FirebaseHandler()
    var selectedRide: Ride? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_ride)

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

        val rideList = ArrayList<Ride>()
        val staffNames: ArrayList<String> = ArrayList()

        backBtnViewRide.setOnClickListener {
            finish()
        }

        editBtnRide.setOnClickListener {
            selectedRide?.let {
                // Navigate to edit page if a ride is selected
                // You can start an Activity here for editing
            } ?: run {
                Toast.makeText(this, "Please select a ride to edit", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch all rides from Firebase
        fh.getAllRides { rideArray ->
            rideList.addAll(rideArray)
            staffNames.add("Select Ride")
            rideArray.forEach { ride ->
                staffNames.add(ride.Name)
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, staffNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            rideSelectView.adapter = adapter
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
                        minAgeToAttTxt.text = "Min age to Att :\n" + ride.minAgeToAttend
                        minAgeToAttTxt.text = "Min age to Att :\n" + ride.minAgeToAttend
                        minAgeToOpTxt.text = "Min age to Op :\n" + ride.minAgeToOperate
                        minNumAttTxt.text = "Min Num of Att :\n" + ride.minNumAtt
                        minNumOpTxt.text = "Min Num of Op :\n" + ride.minNumOp
                        prefNumAttTxt.text = "Pref Num of Att :\n" + ride.prefNumAtt
                        prefNumOpTxt.text = "Pref Num of Op :\n" + ride.prefNumOp
                        openTxt.text = "Open: \n" + ride.open

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
                            val rideTextView = TextView(this@ViewRide).apply {
                                text = staffName
                                textSize = 16f
                                setTextColor(resources.getColor(android.R.color.white))
                                setPadding(16, 8, 16, 8)
                                maxLines = 2 // This ensures that the text will wrap to the next line if it's too long
                                ellipsize = android.text.TextUtils.TruncateAt.END // This will add ellipsis if the text exceeds two lines
                                layoutParams = GridLayout.LayoutParams().apply {
                                    setMargins(8, 8, 8, 8)
                                    width = GridLayout.LayoutParams.WRAP_CONTENT
                                    height = GridLayout.LayoutParams.WRAP_CONTENT
                                }
                            }
                            gridLayoutRideView.addView(rideTextView)
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

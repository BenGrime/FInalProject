package com.example.timetabler

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.io.Serializable

class RequirementsScreen : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var NextBtn : Button
    private lateinit var CancelBtn : Button
    private val fh = FirebaseHandler()

    private val spinnerList = mutableListOf<Spinner>()
    private var staffList = ArrayList<Staff>()
    private var attList = ArrayList<String>()
    private var opList = ArrayList<String>()
    private var sroList = ArrayList<String>()
    private var adultList = ArrayList<String>()
    private var nameList = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requirements_screen)
        val staffSelected = intent.getSerializableExtra("staffSelected") as? ArrayList<String>
        gridLayout = findViewById(R.id.gridRequirementLayout)
        NextBtn = findViewById(R.id.requirementConfirm)
        CancelBtn = findViewById(R.id.requirementCancel)

        CancelBtn.setOnClickListener(View.OnClickListener {
            finish()
        })
        nameList.add("Select Staff")

        if (staffSelected != null) {
            //change strings to objects
            fh.getSelectedStaffObjs(staffSelected){

                for(staff in it)
                {
                    staffList.add(staff)
                    nameList.add(staff.Name)
                    when (staff.Category) {
                        "Attendant" -> {
                            attList.add(staff.Name)
                        }
                        "Fairground" -> {
                            opList.add(staff.Name)
                            adultList.add(staff.Name)
                        }
                        else -> {
                            adultList.add(staff.Name)
                            sroList.add(staff.Name)
                        }
                    }
                }

                fh.getAllRides { rides ->
                    hideLoading()
                    staffSelected.add(0, "Select Staff")
                    //create TextView with ride 1
                    for(ride in rides)
                    {
                        var iterator = 1

                        for(i in 1..ride.prefNumOp)
                        {
                            createRow(ride, iterator)
                            iterator++
                        }
                        for(i in 1..ride.prefNumAtt)
                        {
                            createRow(ride, iterator)
                            iterator++
                        }
                    }
                }
            }

        }
        showLoading()
    }
    private fun showLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.GONE
    }

    private fun createRow(ride:Ride, iterator: Int)
    {
        var adapter: ArrayAdapter<String>
        var rideName : String
        if(ride.prefNumOp + ride.prefNumAtt > 1 && ride.prefNumOp != 0)//if true then we need to add Op or Att at the end
        {
            if(ride.prefNumOp >= iterator)
            {
                if(ride.minAgeToOperate == 18){
                    val trainedList : ArrayList<String> = ArrayList()
                    for(staff in staffList)
                    {
                        for(r in staff.RidesTrained)
                        {
                            if(ride.Name == r && (staff.Category.equals("SRO") || staff.Category.equals("Fairground")))
                            {
                                trainedList.add(staff.Name)
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                else{
                    val trainedList : ArrayList<String> = ArrayList()
                    for(staff in staffList)
                    {
                        for(r in staff.RidesTrained)
                        {
                            if(ride.Name == r && staff.Category.equals("SRO"))
                            {
                                trainedList.add(staff.Name)
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                rideName = ride.Name + " Op"
            }
            else{
                rideName = ride.Name + " Att"
                if(ride.minAgeToAttend == 18 && ride.prefNumOp <= 1){
                    val trainedList : ArrayList<String> = ArrayList()
                    for(staff in staffList)
                    {
                        for(r in staff.RidesTrained)
                        {
                            if(ride.Name == r && staff.Category.equals("Fairground"))
                            {
                                trainedList.add(staff.Name)
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                else{
                    val trainedList : ArrayList<String> = ArrayList()
                    for(staff in staffList)
                    {
                        for(r in staff.RidesTrained)
                        {
                            if(ride.Name == r && staff.Category.equals("Attendant"))
                            {
                                trainedList.add(staff.Name)
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
            }
        }
        else//we can leave it as the normal ride name
        {
            rideName = ride.Name
            if(ride.prefNumOp != 0){
                val trainedList : ArrayList<String> = ArrayList()
                for(staff in staffList)
                {
                    for(r in staff.RidesTrained)
                    {
                        if(ride.Name == r && (staff.Category.equals("SRO") || staff.Category.equals("Fairground")))
                        {
                            trainedList.add(staff.Name)
                        }
                    }
                }
                adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
            }
            else
            {
                if(ride.minAgeToOperate == 18){
                    if(ride.prefNumOp > 0){
                        val trainedList : ArrayList<String> = ArrayList()
                        for(staff in staffList)
                        {
                            for(r in staff.RidesTrained)
                            {
                                if(ride.Name == r && staff.Category.equals("Fairground"))
                                {
                                    trainedList.add(staff.Name)
                                }
                            }
                        }
                        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                    }
                    else{
                        val trainedList : ArrayList<String> = ArrayList()
                        for(staff in staffList)
                        {
                            for(r in staff.RidesTrained)
                            {
                                if(ride.Name == r && staff.Category.equals("Attendant"))
                                {
                                    trainedList.add(staff.Name)
                                }
                            }
                        }
                        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                    }
                }
                else{
                    val trainedList : ArrayList<String> = ArrayList()
                    for(staff in staffList)
                    {
                        for(r in staff.RidesTrained)
                        {
                            if(ride.Name == r && staff.Category.equals("SRO"))
                            {
                                trainedList.add(staff.Name)
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
            }
        }
        val rideTextView = TextView(this).apply {
            text = rideName
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@RequirementsScreen, R.color.black))
            setBackgroundColor(ContextCompat.getColor(this@RequirementsScreen, R.color.white))
            setPadding(16, 16, 16, 16) // Padding inside the TextView
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0 // Equal distribution
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(0, 1f) // Occupy first column
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED) // Dynamic row position
                setMargins(8, 20, 8, 20) // Add margins between elements
            }
        }
        gridLayout.addView(rideTextView)

        val staffSpinner = Spinner(this).apply {
            setPadding(16, 16, 16, 16)
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0 // Equal distribution
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(1, 1f) // Occupy second column
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED) // Dynamic row position
                setMargins(8, 20, 8, 20) // Add margins between elements
                setBackgroundColor(ContextCompat.getColor(this@RequirementsScreen, R.color.white))
            }

            tag = rideName + "Staff"
        }
        spinnerList.add(staffSpinner)
        staffSpinner.adapter = adapter
        gridLayout.addView(staffSpinner)
    }

}
package com.example.timetabler

import android.content.Intent
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
        val staffCopy = staffSelected
        gridLayout = findViewById(R.id.gridRequirementLayout)
        NextBtn = findViewById(R.id.requirementConfirm)
        CancelBtn = findViewById(R.id.requirementCancel)

        CancelBtn.setOnClickListener(View.OnClickListener {
            finish()
        })
        NextBtn.setOnClickListener(View.OnClickListener {

            // Assuming selectedStaffList is a List<List<String>>
            val selectedStaffList = mutableListOf<List<String>>()

            // Populate the list as you did earlier
            for (spinner in spinnerList) {
                val selectedStaff = spinner.selectedItem?.toString() ?: "No staff selected"
                val tag = spinner.tag?.toString() ?: "No tag set"
                selectedStaffList.add(listOf(tag, selectedStaff)) // Add ride and staff as a list
            }
            staffSelected?.removeFirst()
            // Pass the list to the next activity
            val intent = Intent(this, timetableOverridePage::class.java)
            intent.putExtra("staffSelected", staffCopy)
            intent.putExtra("RideStaffList", ArrayList(selectedStaffList)) // Convert to ArrayList before passing
            startActivity(intent)
            finish()
        })


        nameList.add("Select Staff")

        if (staffSelected != null) {
            //change strings to objects
            fh.getSelectedStaffObjs(staffSelected){

                for(staff in it)//get all staff objects from names from previous page
                {
                    staffList.add(staff)//add objects to list
                    nameList.add(staff.Name)//add the names to this list
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
                        if(ride.open) {
                            var iterator = 1

                            for (i in 1..ride.prefNumOp) {
                                createRow(ride, iterator)
                                iterator++
                            }
                            for (i in 1..ride.prefNumAtt) {
                                createRow(ride, iterator)
                                iterator++
                            }
                        }
                    }
                    repeat(6) {
                        carParkSection()
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

    private fun carParkSection()
    {
        val rideTextView = TextView(this).apply {
            text = "Car Park"
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

            tag = "Car Park"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nameList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
        spinnerList.add(staffSpinner)
        staffSpinner.adapter = adapter
        gridLayout.addView(staffSpinner)
    }

    private fun createRow(ride:Ride, iterator: Int)
    {
        var adapter: ArrayAdapter<String>
        var rideName : String
        if(ride.prefNumOp + ride.prefNumAtt > 1)//if true then we need to add Op or Att at the end
        {
            if(ride.prefNumOp >= iterator)//
            {
                if(ride.minAgeToOperate == 18){
                    val trainedList : ArrayList<String> = ArrayList()
                    trainedList.add("Select Staff")
                    for(staff in staffList)
                    {
                        var repeater = 0
                        for(r in staff.RidesTrained)
                        {
                            val strippedR = when{
                                r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                else -> r
                            }
                            if(ride.Name == strippedR && (staff.Category.equals("SRO") || staff.Category.equals("Fairground")))
                            {
                                if(iterator <= ride.minAgeToOperate && r.toString().endsWith(" Op")){
                                    if(repeater == 0)
                                    {
                                        trainedList.add(staff.Name)
                                        repeater += 1
                                    }
                                }

                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                else //16 or 21 to operate
                {
                    val trainedList : ArrayList<String> = ArrayList()
                    trainedList.add("Select Staff")
                    for(staff in staffList)
                    {
                        var repeater = 0
                        for(r in staff.RidesTrained)
                        {
                            val strippedR = when{
                                r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                else -> r
                            }

                            if(ride.Name == strippedR && staff.Category.equals("SRO"))
                            {
                                if(iterator <= ride.minAgeToOperate)//looking for operators
                                {
                                    if(r.toString().endsWith( "Op")){
                                        if(repeater == 0)
                                        {
                                            trainedList.add(staff.Name)
                                            repeater += 1
                                        }
                                    }
                                }
                            }
                            else if(ride.Name == strippedR && ride.minAgeToOperate == 16)
                            {
                                trainedList.add(staff.Name)
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                if(ride.minAgeToOperate <18)
                {
                    rideName = ride.Name
                }
                else{
                    rideName = ride.Name + " Op"
                }
            }
            else{
                rideName = ride.Name + " Att"
                if(ride.minAgeToAttend == 18 && ride.prefNumOp <= 1){
                    val trainedList : ArrayList<String> = ArrayList()
                    trainedList.add("Select Staff")
                    for(staff in staffList)
                    {
                        var repeater = 0
                        for(r in staff.RidesTrained)
                        {
                            val strippedR = when{
                                r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                else -> r
                            }
                            if(ride.Name == strippedR && r.toString().endsWith(" Att")) {
                                if(repeater == 0){
                                    trainedList.add(staff.Name)
                                    repeater++
                                }
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                else{
                    val trainedList : ArrayList<String> = ArrayList()
                    trainedList.add("Select Staff")
                    for(staff in staffList)
                    {
                        var repeater = 0
                        for(r in staff.RidesTrained)
                        {
                            val strippedR = when{
                                r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                else -> r
                            }
                            if(ride.Name == strippedR && r.toString().endsWith(" Att"))
                            {
                                if(repeater == 0)
                                {
                                    trainedList.add(staff.Name)
                                    repeater++
                                }
//                                        && staff.Category.equals("Attendant")
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
                trainedList.add("Select Staff")
                for(staff in staffList)
                {
                    for(r in staff.RidesTrained)
                    {
                        val strippedR = when{
                            r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                            r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                            else -> r
                        }//strip ride name to remove Op or Att
                        if(ride.Name == strippedR && (staff.Category.equals("SRO") || staff.Category.equals("Fairground")))//if its the correct ride and the staff in old enough
                        {
                            trainedList.add(staff.Name)//add them to the list
                        }
                        else if(ride.Name == strippedR && ride.minAgeToOperate == 16)
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
                        trainedList.add("Select Staff")
                        for(staff in staffList)
                        {
                            for(r in staff.RidesTrained)
                            {
                                val strippedR = when{
                                    r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                    r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                    else -> r
                                }
                                if(ride.Name == strippedR && staff.Category.equals("Fairground"))
                                {
                                    trainedList.add(staff.Name)
                                }
                            }
                        }
                        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                    }
                    else{
                        val trainedList : ArrayList<String> = ArrayList()
                        trainedList.add("Select Staff")
                        for(staff in staffList)
                        {
                            for(r in staff.RidesTrained)
                            {
                                val strippedR = when{
                                    r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                    r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                    else -> r
                                }
                                if(ride.Name == strippedR && staff.Category.equals("Attendant"))
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
                    trainedList.add("Select Staff")
                    for(staff in staffList)
                    {
                        for(r in staff.RidesTrained)
                        {
                            val strippedR = when{
                                r.toString().endsWith(" Op", ignoreCase = true) -> r.toString().removeSuffix(" Op")
                                r.toString().endsWith(" Att", ignoreCase = true) -> r.toString().removeSuffix(" Att")
                                else -> r
                            }
                            if(ride.Name == strippedR && staff.Category.equals("SRO"))
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

            tag = rideName
        }
        spinnerList.add(staffSpinner)
        staffSpinner.adapter = adapter
        gridLayout.addView(staffSpinner)
    }

}
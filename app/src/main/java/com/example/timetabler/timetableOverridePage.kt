package com.example.timetabler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

class timetableOverridePage : AppCompatActivity() {

    var fh = FirebaseHandler()
    private val db = Firebase.firestore
    private var generateTimetable = GenerateTimetable()
    private lateinit var loadingText: TextView
    private var staffList = ArrayList<Staff>()
    private var attList = ArrayList<String>()
    private var opList = ArrayList<String>()
    private var sroList = ArrayList<String>()
    private var adultList = ArrayList<String>()
    private var nameList = ArrayList<String>()
    private lateinit var gridLayout: GridLayout
    private val spinnerList = mutableListOf<Spinner>()
    private lateinit var NextBtn : Button
    private lateinit var CancelBtn : Button

    private var finishedBoard = ArrayList<ArrayList<String>>()
    private var failedToUpdateList = ArrayList<ArrayList<String>>()

    private var skipped = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable_override_page)
        val selectedStaffList = intent.getSerializableExtra("RideStaffList") as? ArrayList<ArrayList<String>>
        val staffSelected = intent.getSerializableExtra("staffSelected") as? ArrayList<String>
        val staffCopy = staffSelected?.let { ArrayList(it) }

        loadingText = findViewById(R.id.loadingText)
        showLoading()
        loadingText.text = "Generating the board..."
        gridLayout = findViewById(R.id.gridOverrideLayout)
        NextBtn = findViewById(R.id.OverrideConfirm)
        CancelBtn = findViewById(R.id.OverrideCancel)


        CancelBtn.setOnClickListener(View.OnClickListener {
            finish()
        })
        NextBtn.setOnClickListener(View.OnClickListener {
            showLoading()
            //check the spinners, update previous rides.
            var checkList : ArrayList<String> = ArrayList()
            var carryOn = true
            for(s in spinnerList)
            {
                if(!checkList.contains(s.selectedItem.toString()))
                {
                    checkList.add(s.selectedItem.toString())
                    break
                }
                else
                {
                    Toast.makeText(this, s.selectedItem.toString()+" has been Selected Twice", Toast.LENGTH_SHORT).show()
                    carryOn = false
                    break
                }

            }
            if(carryOn)
            {
                //store board in firebase
                updateStaff(finishedBoard)
                //back to main activity
            }

        })




        if(selectedStaffList != null && staffSelected!= null)
        {
            fh.getSelectedStaffObjs(staffSelected) { staffListObj ->
                fh.getAllRides { rides ->
                    (rides as? ArrayList<Ride>)?.let {
                        loadingText.text = "Generating the board"
                        generateTimetable.timetable1(selectedStaffList, staffSelected, staffListObj, it){ result ->
                            nameList.add("Select Staff")

                            //change strings to objects
                            if (staffCopy != null) {
                                fh.getSelectedStaffObjs(staffCopy){

                                    for(staff in it)//get all staff objects from names from previous page
                                    {
                                        staffList.add(staff)
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
                                    var rideNumber = 0
                                    fh.getAllRides { rides ->
//                                        hideLoading()
                                        staffSelected.add(0, "Select Staff")
                                        //create TextView with ride 1

                                        for(ride in rides) {
                                            if(ride.open) {
                                                var iterator = 1

                                                for (i in 1..ride.prefNumOp) {
                                                    createRow(ride, iterator, result, rideNumber)
                                                    rideNumber++
                                                    iterator++
                                                }
                                                for (i in 1..ride.prefNumAtt) {
                                                    createRow(ride, iterator, result, rideNumber)
                                                    iterator++
                                                    rideNumber++
                                                }
                                            }
                                        }
                                        var carParkNumber = 0
                                        result.forEach { row ->
                                            if(row[0] == "Car Park"){
                                                carParkNumber++
                                            }
                                        }
                                        repeat(carParkNumber){
                                            carParkSection(rideNumber, result)
                                            rideNumber++
                                        }
                                        finishedBoard = ArrayList(result)
                                        hideLoading()

                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun updateStaff(list: ArrayList<ArrayList<String>>)
    {
        val tempList = ArrayList(list)
        val validStaffList = list.filter { it[1] != "Select Staff" }
        val pendingTasks = AtomicInteger(validStaffList.size) // Track pending tasks
        loadingText.text = "Saving to Database..."

        fun processNext() {
            if (tempList.isNotEmpty()) {
                // Get the first element from the list
                val row = tempList.removeAt(0)
                val ride = row[0]
                val staff = row[1]

                // Process the current element
                fh.getDocumentFromName(staff) { staffObj ->
                    if (staffObj != null) {//if name is "Select Staff" it'll return null
                        val strippedRide = ride.replace(Regex("(Op|Att)\$"), "").trim()
                        val updateMap = mapOf("previousRide" to strippedRide)
                        db.collection("Staff").document(staffObj.Id).update(updateMap)
                            .addOnSuccessListener {
                                // Decrease the pending task count and process next task
                                if (pendingTasks.decrementAndGet() == 0)
                                {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    println("Task completed. Skipped tasks: "+failedToUpdateList.size)
                                    saving()
                                }
                                else
                                {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    processNext() // Continue processing the next task
                                }
                            }
                            .addOnFailureListener {
                                // Handle failure if necessary
                                val tempList = ArrayList<String>()
                                tempList.add(staffObj.Name)
                                tempList.add(strippedRide)
                                failedToUpdateList.add(tempList)
                                if (pendingTasks.decrementAndGet() == 0) {
                                     // All tasks are complete, hide loading
                                    println("Task completed. Skipped tasks: "+failedToUpdateList.size)
                                    saving()
                                } else {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    processNext() // Continue despite failure
                                }
                            }
                    } else {
                        processNext() // Skip and process the next item
                    }
                }
            } else if (pendingTasks.decrementAndGet() == 0) {
                println("Task completed. Remaining tasks: $pendingTasks")
                println("Task completed. Skipped tasks: "+failedToUpdateList.size)
                saving()

            }
        }

        // Start processing the list
        if (tempList.isNotEmpty()) {
            processNext()
        } else {
            saving()
        }
    }

    private fun createRow(ride:Ride, iterator: Int, board : ArrayList<ArrayList<String>>, rideNumber : Int)
    {
        var adapter: ArrayAdapter<String>
        var rideName : String
        val trainedList : ArrayList<String> = ArrayList()
        if(ride.prefNumOp + ride.prefNumAtt > 1)//if true then we need to add Op or Att at the end
        {
            if(ride.prefNumOp >= iterator)//
            {
                if(ride.minAgeToOperate == 18){
//                    trainedList : ArrayList<String> = ArrayList()
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
                                if(repeater == 0)
                                {
                                    trainedList.add(staff.Name)
                                    repeater += 1
                                }
                            }
                        }
                    }
                    adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainedList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                }
                else
                {
//                    val trainedList : ArrayList<String> = ArrayList()
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
                                if(repeater == 0)
                                {
                                    trainedList.add(staff.Name)
                                    repeater += 1
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
//                    val trainedList : ArrayList<String> = ArrayList()
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
                            if(ride.Name == strippedR ) {
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
//                    val trainedList : ArrayList<String> = ArrayList()
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
                            if(ride.Name == strippedR )
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
//                val trainedList : ArrayList<String> = ArrayList()
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
//                        val trainedList : ArrayList<String> = ArrayList()
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
//                        val trainedList : ArrayList<String> = ArrayList()
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
//                    val trainedList : ArrayList<String> = ArrayList()
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
            setTextColor(ContextCompat.getColor(this@timetableOverridePage, R.color.black))
            setBackgroundColor(ContextCompat.getColor(this@timetableOverridePage, R.color.white))
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
                setBackgroundColor(ContextCompat.getColor(this@timetableOverridePage, R.color.white))
            }

            tag = rideName
        }
        spinnerList.add(staffSpinner)
        staffSpinner.adapter = adapter

        var used = false
        generateTimetable.getShortBoardUsed { sbu ->
            used = sbu
        }
        if(used)
        {
            if(rideName == board.get(rideNumber-skipped)[0])
            {
                val position = trainedList.indexOf(board.get(rideNumber-skipped)[1])
                if (position != -1) {
                    staffSpinner.setSelection(position) // Set the personOnRide as the selected item
                }
            }
            else
            {
                skipped++
            }

        }
        else
        {
            val position = trainedList.indexOf(board.get(rideNumber)[1])
            if (position != -1) {
                staffSpinner.setSelection(position) // Set the personOnRide as the selected item
            }
        }



        gridLayout.addView(staffSpinner)
    }

    private fun carParkSection(rideNumber : Int, board : ArrayList<ArrayList<String>>)
    {
        val rideTextView = TextView(this).apply {
            text = "Car Park"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@timetableOverridePage, R.color.black))
            setBackgroundColor(ContextCompat.getColor(this@timetableOverridePage, R.color.white))
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
                setBackgroundColor(ContextCompat.getColor(this@timetableOverridePage, R.color.white))
            }

            tag = "Car Park"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nameList).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
        spinnerList.add(staffSpinner)
        staffSpinner.adapter = adapter
        val position = nameList.indexOf(board.get(rideNumber)[1])
        if (position != -1) {
            staffSpinner.setSelection(position) // Set the personOnRide as the selected item
        }

        gridLayout.addView(staffSpinner)
    }

    private fun saving()
    {
        println("Storing Board Now")
        //store board on firebase
        val firebaseBoard = finishedBoard.map { innerList -> mapOf("ride" to innerList[0], "staff" to innerList[1]) } as ArrayList<Map<String, String>>
        firebaseBoard.forEach { row -> println("Row: $row, Type: ${row::class.simpleName}") }
        db.collection("Board").document("completeBoard").set(mapOf("Board" to firebaseBoard)).addOnSuccessListener{
            hideLoading() // Call hideLoading when all tasks are complete
            Toast.makeText(this, "Board Successfully saved", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("failedToUpdateList", failedToUpdateList as Serializable)
            startActivity(intent)
            finish()
        }


    }


    private fun showLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.GONE
    }
}
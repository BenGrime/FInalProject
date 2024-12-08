package com.example.timetabler

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginBottom
import com.github.javafaker.Bool
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewStaff : AppCompatActivity() {

    private lateinit var backBtnViewStaff: ImageView
    private lateinit var staffSelectView: Spinner
    private lateinit var staffMemberTxt: TextView
    private lateinit var editBtn: ImageView
    private lateinit var addRideBtn: ImageView
    private lateinit var categoryTxt: TextView
    private lateinit var dobTxt: TextView
    private lateinit var prevRideTxt: TextView
    private lateinit var gridLayout: GridLayout

    private lateinit var dialog : Dialog
    private lateinit var createStaffCancel : Button
    private lateinit var createStaffConfirm : Button
    private lateinit var nameInput : TextInputEditText
    private lateinit var dobInput : TextInputEditText


    // Declare lateinit variables
    private lateinit var textViewBulk: TextView
    private lateinit var dropdownsBulk: GridLayout
    private lateinit var addStaffCancel: MaterialButton
    private lateinit var addStaffConfirm: MaterialButton
    private lateinit var addRideToListBtn: MaterialButton

    private val db = FirebaseFirestore.getInstance()

    var fh = FirebaseHandler()

    var selectedStaff: Staff? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_staff)

        // Initialize views using findViewById
        backBtnViewStaff = findViewById(R.id.backBtnViewStaff)
        staffSelectView = findViewById(R.id.staffSelectView)
        staffMemberTxt = findViewById(R.id.staffMemberTxt)
        editBtn = findViewById(R.id.editBtn)
        categoryTxt = findViewById(R.id.categoryTxt)
        dobTxt = findViewById(R.id.dobTxt)
        prevRideTxt = findViewById(R.id.prevRideTxt)
        gridLayout = findViewById(R.id.gridLayout)
        addRideBtn = findViewById(R.id.addRideIcon)



        dialog = Dialog(this)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
        dialog.setCancelable(true)


        var staffList = ArrayList<Staff>()
        var staffNames: ArrayList<String> = ArrayList()

        backBtnViewStaff.setOnClickListener(View.OnClickListener {
            finish()
        })

        addRideBtn.setOnClickListener{
            if(selectedStaff != null)
            {
                //show pop-up or page with "add ride(s) to selected Staff
                dialog.setContentView(R.layout.add_staff_bulk_dialog)
                addRideToListBtn = dialog.findViewById(R.id.addRideButton)
                textViewBulk = dialog.findViewById(R.id.textViewBulk)
                dropdownsBulk = dialog.findViewById(R.id.dropdownsBulk)
                addStaffCancel = dialog.findViewById(R.id.AddStaffCancelBulk)
                addStaffConfirm = dialog.findViewById(R.id.AddStaffConfirmBulk)
                //on it, have a add another ride which adds a textView and Spinner to grid layout

                addRideToListBtn.setOnClickListener {
                    addRideElement()
                }
                addStaffCancel.setOnClickListener{
                    dialog.dismiss()
                }
//                addStaffConfirm.setOnClickListener {
//                    // List to store selected items from the spinners
//                    val selectedItems = mutableSetOf<String>()
//
//                    // Boolean to track duplicates
//                    var hasDuplicates = false
//
//                    // Iterate through the GridLayout's children
//                    for (i in 0 until dropdownsBulk.childCount) {
//                        val view = dropdownsBulk.getChildAt(i)
//                        if (view is Spinner) { // Check if the view is a Spinner
//                            val selectedItem = view.selectedItem?.toString()
//                            if (!selectedItem.isNullOrEmpty() || selectedItem.toString() != "Select Ride") {
//                                // Check if the item is already in the set
//                                if (!selectedItems.add(selectedItem.toString())) {
//                                    hasDuplicates = true
//                                    break
//                                }
//                            }
//                        }
//                    }
//
//                    // Handle result
//                    if (hasDuplicates) {
//                        Toast.makeText(this, "Each ride must be unique. Please select different rides.", Toast.LENGTH_SHORT).show()
//                    } else {
//                        //create map to update firebase, currently staff.ridesTrained
//                        val currentStaff : MutableList<Any> = selectedStaff?.RidesTrained!!.toMutableList()
//                        var allRideUpdated = false
//                        for(item in selectedItems)
//                        {
//                            var carryOn = true
//                            if(carryOn)
//                            {
//                                carryOn = false
//                                fh.getRideFromName(item){ride ->
//                                    val currentRides : MutableList<Any> = ride?.staffTrained!!.toMutableList()
//                                    if(selectedStaff?.Category == "SRO")
//                                    {
//                                        currentRides.add(selectedStaff?.Name + " Op")
//                                        currentStaff.add(ride.Name + " Op")
//                                        if(ride.prefNumAtt > 0)
//                                        {
//                                            currentRides.add(selectedStaff?.Name + " Att")
//                                            currentStaff.add(ride.Name + " Att")
//                                        }
//                                    }
//                                    else if(selectedStaff?.Category == "Fairground")
//                                    {
//                                        currentRides.add(selectedStaff?.Name + " Op")
//                                        currentStaff.add(ride.Name + " Op")
//                                        if(ride.prefNumAtt > 0 && ride.minAgeToAttend > calculateAge(selectedStaff!!.DoB))
//                                        {
//                                            currentRides.add(selectedStaff?.Name + " Att")
//                                            currentStaff.add(ride.Name + " Att")
//                                        }
//                                    }
//                                    else
//                                    {
//                                        if(ride.minAgeToOperate == 16)//if attendant ride, and they are an attendant, just add Op at the end
//                                        {
//                                            currentRides.add(selectedStaff?.Name+" Op")
//                                            currentStaff.add(ride.Name + " Op")
//                                        }
//                                        else
//                                        {
//                                            currentRides.add(selectedStaff?.Name+" Att")
//                                            currentStaff.add(ride.Name + " Att")
//                                        }
//                                    }
//                                    var updateRideMap =  hashMapOf<String, Any>("staffTrained" to currentRides)
//                                    db.collection("Rides").document(ride.Id).update(updateRideMap).addOnSuccessListener {
//                                        carryOn = true
//                                    }
//                                }
//
//                            }
//
//                        }
//
//                        if(allRideUpdated)
//                        {
//                            var updateMap =  hashMapOf<String, Any>("ridesTrained" to currentStaff)
//                            db.collection("Staff").document(selectedStaff!!.Id).update(updateMap).addOnSuccessListener {
//                                Toast.makeText(this, "All updated", Toast.LENGTH_SHORT).show()
//                                dialog.dismiss()
//                            }
//                        }
//
//                    }
//                }
                addStaffConfirm.setOnClickListener {
                    // List to store selected items from the spinners
                    val selectedItems = mutableSetOf<String>()

                    // Boolean to track duplicates
                    var hasDuplicates = false

                    // Iterate through the GridLayout's children
                    for (i in 0 until dropdownsBulk.childCount) {
                        val view = dropdownsBulk.getChildAt(i)
                        if (view is Spinner) { // Check if the view is a Spinner
                            val selectedItem = view.selectedItem?.toString()
                            if (!selectedItem.isNullOrEmpty() && selectedItem != "Select Ride") {
                                // Check if the item is already in the set
                                if (!selectedItems.add(selectedItem)) {
                                    hasDuplicates = true
                                    break
                                }
                            }
                        }
                    }

                    // Handle result
                    if (hasDuplicates) {
                        Toast.makeText(this, "Each ride must be unique. Please select different rides.", Toast.LENGTH_SHORT).show()
                    } else {
                        val currentStaff: MutableList<Any> = selectedStaff?.RidesTrained!!.toMutableList()
                        var completedRides = 0
                        val totalRides = selectedItems.size

                        // Function to handle ride completion
                        fun onRideUpdated() {
                            completedRides++
                            if (completedRides == totalRides) {
                                // Update staff only after all rides are updated
                                val updateMap = hashMapOf<String, Any>("ridesTrained" to currentStaff)
                                db.collection("Staff").document(selectedStaff!!.Id).update(updateMap).addOnSuccessListener {
                                    Toast.makeText(this, "All updated", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                            }
                        }

                        // Iterate through selected rides
                        for (item in selectedItems) {
                            fh.getRideFromName(item) { ride ->
                                ride?.let {
                                    val currentRides: MutableList<Any> = it.staffTrained.toMutableList()
                                    when (selectedStaff?.Category) {
                                        "SRO" -> {
                                            currentRides.add("${selectedStaff?.Name} Op")
                                            currentStaff.add("${ride.Name} Op")
                                            if (ride.prefNumAtt > 0) {
                                                currentRides.add("${selectedStaff?.Name} Att")
                                                currentStaff.add("${ride.Name} Att")
                                            }
                                        }
                                        "Fairground" -> {
                                            currentRides.add("${selectedStaff?.Name} Op")
                                            currentStaff.add("${ride.Name} Op")
                                            if (ride.prefNumAtt > 0 && ride.minAgeToAttend < calculateAge(selectedStaff!!.DoB)) {
                                                currentRides.add("${selectedStaff?.Name} Att")
                                                currentStaff.add("${ride.Name} Att")
                                            }
                                        }
                                        else -> {
                                            if (ride.minAgeToOperate == 16) {
                                                currentRides.add("${selectedStaff?.Name} Op")
                                                currentStaff.add("${ride.Name} Op")
                                            } else {
                                                currentRides.add("${selectedStaff?.Name} Att")
                                                currentStaff.add("${ride.Name} Att")
                                            }
                                        }
                                    }

                                    val updateRideMap = hashMapOf<String, Any>("staffTrained" to currentRides)
                                    db.collection("Rides").document(ride.Id).update(updateRideMap).addOnSuccessListener {
                                        onRideUpdated()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Failed to update ride: ${ride.Name}", Toast.LENGTH_SHORT).show()
                                        onRideUpdated() // Proceed even on failure
                                    }
                                }
                            }
                        }
                    }
                }




            }
            dialog.show()


        }

        editBtn.setOnClickListener(View.OnClickListener {
            if (selectedStaff != null) { // Ensure selectedStaff is not null

                dialog.setContentView(R.layout.create_new_staff)

                createStaffCancel = dialog.findViewById(R.id.createStaffCancel)
                createStaffConfirm = dialog.findViewById(R.id.createStaffConfirm)
                nameInput = dialog.findViewById(R.id.staffNameInput)
                dobInput = dialog.findViewById(R.id.staffDobInput)

                // Populate the dialog with selected staff's information
                nameInput.setText(selectedStaff?.Name ?: "")
                dobInput.setText(selectedStaff?.DoB?.let { it1 -> formatDate(it1) })
                // Add other details like dobInput if needed

                dialog.show()

                createStaffCancel.setOnClickListener {
                    dialog.dismiss()
                }

                createStaffConfirm.setOnClickListener {
                    val newName = nameInput.text?.toString()?.trim()
                    val newDobText = dobInput.text?.toString()?.trim()

                    if (newName == selectedStaff?.Name && newDobText == selectedStaff?.DoB?.let { formatDate(it) }) {
                        Toast.makeText(this, "Fields were not changed", Toast.LENGTH_SHORT).show()
                    } else {
                        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newDobText)) {
                            Toast.makeText(this, "Name or Date of Birth cannot be empty", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Convert newDobText to Timestamp
                        val dobFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val newDobTimestamp = try {
                            Timestamp(dobFormat.parse(newDobText) ?: throw IllegalArgumentException("Invalid date"))
                        } catch (e: Exception) {
                            Toast.makeText(this, "Invalid Date of Birth format. Use dd/MM/yyyy.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Firestore update logic
                        selectedStaff?.let { staff ->
                            val updatedFields = mapOf(
                                "name" to newName,
                                "doB" to newDobTimestamp,
                                "category" to when {
                                    calculateAge(newDobTimestamp) < 18 -> "Attendant"
                                    calculateAge(newDobTimestamp) in 18..20 -> "Fairground"
                                    calculateAge(newDobTimestamp) > 20 -> "SRO"
                                    else -> ""
                                }
                            )

                            db.collection("Staff").document(staff.Id)
                                .update(updatedFields)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Staff updated successfully", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()

                                    // Re-fetch the updated staff data
                                   recreate()

                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to update staff: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } ?: run {
                            Toast.makeText(this, "No staff selected to update", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
            else
            {
                // Optionally, show a message indicating no staff is selected
                Toast.makeText(this, "Please select a staff member to edit", Toast.LENGTH_SHORT).show()
            }

        })

        fh.getAllStaff { staffArray ->

            staffList = staffArray as ArrayList<Staff>
            staffNames.add("Select Staff")
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
                    selectedStaff = staffList[position - 1] // Account for the first "Select Staff" option
                    editBtn.isEnabled = true
                    editBtn.alpha = 1f
                    addRideBtn.isEnabled = true
                    addRideBtn.alpha = 1f

                    // Use the selected staff object to populate the TextViews
                    staffMemberTxt.text = selectedStaff!!.Name
                    categoryTxt.text = "Category: ${selectedStaff!!.Category}" // Assuming 'Category' is a property


                    val formattedDoB = formatDate(selectedStaff!!.DoB)
                    dobTxt.text = "Date of Birth: $formattedDoB"


                    prevRideTxt.text = if (selectedStaff!!.PreviousRide == "null") {
                        "No previous ride available"
                    } else {
                        "Previous Ride: ${selectedStaff!!.PreviousRide}"
                    }

                    val ridesTrained = selectedStaff!!.RidesTrained // List of rides trained on
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
                        val rideTextView = TextView(this@ViewStaff).apply {
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
                else {
                    // Disable the edit button if "Select Staff" is selected
                    editBtn.isEnabled = false
                    editBtn.alpha = 0.5f // Make the button semi-transparent to indicate it's disabled
                    addRideBtn.isEnabled = false
                    addRideBtn.alpha = 0.5f
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Handle the case when no staff is selected
                // Disable the edit button as no valid selection is made
                editBtn.isEnabled = false
                editBtn.alpha = 0.5f
                addRideBtn.isEnabled = false
                addRideBtn.alpha = 0.5f
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

    fun convertStrToTime(dateStr : String) : Timestamp{
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val date = formatter.parse(dateStr)
        return Timestamp(date) // Returns a Firebase Timestamp object

    }

    fun calculateAge(birthDate: Timestamp): Int {
        // Get the current date as a Calendar instance
        val calendarNow = Calendar.getInstance()

        // Convert the Firebase Timestamp to a Date and set it in a Calendar instance
        val calendarBirth = Calendar.getInstance()
        calendarBirth.time = birthDate.toDate() // Convert Timestamp to Date

        // Calculate the age
        var age = calendarNow.get(Calendar.YEAR) - calendarBirth.get(Calendar.YEAR)

        // If their birthday hasn't occurred yet this year, subtract 1 from age
        if (calendarNow.get(Calendar.DAY_OF_YEAR) < calendarBirth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    private fun addRideElement() {
        // Create a new TextView
        val textView = TextView(this).apply {
            text = "Ride Name" // Example placeholder
            textSize = 18f
            setTextColor(resources.getColor(R.color.black, theme))
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                marginEnd = 16
                setMargins(6, 6, 6, 6) // Add margins to the TextView
            }
        }

        val spinner = Spinner(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.WRAP_CONTENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(6, 6, 6, 6) // Add margins to the Spinner
            }
        }
        textView.setPadding(6, 6, 6, 6)
        spinner.setPadding(6, 6, 6, 6)
        val untrainedRides = mutableListOf<String>()
        var trainedRides = selectedStaff?.RidesTrained
        fh.getAllRides { rideArray ->
            untrainedRides.add("Select Ride")
            for (ride in rideArray) {
                if (!trainedRides?.contains(ride.Name + " Op")!! && !trainedRides.contains(ride.Name + " Att")) {
                    // if it doesnt contain the ride name
                    if(calculateAge(selectedStaff!!.DoB) >= ride.minAgeToOperate || ((calculateAge(selectedStaff!!.DoB) >= ride.minAgeToAttend) && ride.prefNumAtt > 0))
                    {//if they are old enough to operate OR old enough to attend AND the ride needs attendants

                        //this add attendant rides to 18+
                        if(!selectedStaff!!.Category.equals("Attendant") && ride.minAgeToOperate == 16)
                        {

                        }
                        else
                        {
                            untrainedRides.add(ride.Name)
                        }
                    }

                }
                //what if they are only the Att or Op and we want to add the other to a ride that has the option for the other
                else if(trainedRides.contains(ride.Name + " Op") && !trainedRides.contains(ride.Name + " Att"))
                {
                    if(calculateAge(selectedStaff!!.DoB) >= ride.minAgeToOperate && ride.prefNumAtt > 0){
                        untrainedRides.add(ride.Name + " Att")
                    }


                }
                else if(!trainedRides.contains(ride.Name + " Op") && trainedRides.contains(ride.Name + " Att"))
                {
                    if(calculateAge(selectedStaff!!.DoB) >= ride.minAgeToOperate && ride.prefNumAtt > 0){
                        untrainedRides.add(ride.Name + " Op")
                    }
                }
            }
            // Add dummy items to the Spinner
            val adapter = ArrayAdapter(
                this@ViewStaff,
                android.R.layout.simple_spinner_item,
                untrainedRides
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

        }
        // Add the new TextView and Spinner to the GridLayout
        dropdownsBulk.addView(textView)
        dropdownsBulk.addView(spinner)
    }
}
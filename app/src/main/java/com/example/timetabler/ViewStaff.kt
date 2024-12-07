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
    private lateinit var categoryTxt: TextView
    private lateinit var dobTxt: TextView
    private lateinit var prevRideTxt: TextView
    private lateinit var gridLayout: GridLayout

    private lateinit var dialog : Dialog
    private lateinit var createStaffCancel : Button
    private lateinit var createStaffConfirm : Button
    private lateinit var nameInput : TextInputEditText
    private lateinit var dobInput : TextInputEditText

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



        var staffList = ArrayList<Staff>()
        var staffNames: ArrayList<String> = ArrayList()

        backBtnViewStaff.setOnClickListener(View.OnClickListener {
            finish()
        })

        editBtn.setOnClickListener(View.OnClickListener {
            if (selectedStaff != null) { // Ensure selectedStaff is not null
                dialog = Dialog(this)
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
                dialog.setCancelable(true)
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
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Handle the case when no staff is selected
                // Disable the edit button as no valid selection is made
                editBtn.isEnabled = false
                editBtn.alpha = 0.5f
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
}
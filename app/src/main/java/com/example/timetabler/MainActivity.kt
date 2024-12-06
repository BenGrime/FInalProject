package com.example.timetabler

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    var fh = FirebaseHandler()

    //set dialog
    private lateinit var dialog : Dialog
    private lateinit var  dialog2 : Dialog
    private lateinit var loadingDialog: Dialog

    //main menu buttons
    private lateinit var addStaffButton : Button
    private lateinit var removeStaffButton : Button
    private lateinit var createNewStaff : Button
    private lateinit var deleteStaffbtn : Button
    private lateinit var viewStaffButton : Button
    private lateinit var createRideBtn : Button
    private lateinit var DeleteRideBtn : Button
    private lateinit var viewRideBtn : Button
    private lateinit var generateBoardBtn : Button


    //layouts for main menu
    private lateinit var staffPage : GridLayout
    private lateinit var ridesPage : GridLayout

    //pop up for creating staff
    private lateinit var createStaffCancel : Button
    private lateinit var createStaffConfirm : Button
    private lateinit var nameInput : TextInputEditText
    private lateinit var dobInput : TextInputEditText

    //pop up for adding staff to ride
    private lateinit var addStaffCancel : Button
    private lateinit var addStaffConfirm : Button
    private lateinit var rideSelect : Spinner
    private lateinit var staffSelect : Spinner

    //pop up for removing staff from ride
    private lateinit var removeStaffCancel : Button
    private lateinit var removeStaffConfirm : Button
    private lateinit var  rideRemoveSelect : Spinner
    private lateinit var  staffRemoveSelect : Spinner


    //pop up for delete staff
    private lateinit var staffDeleteSelect : Spinner
    private lateinit var deleteStaffCancel : Button
    private lateinit var  deleteStaffConfirm : Button

    //buttons for deletion confirmation
    private lateinit var conDeleteCancel : Button
    private lateinit var conDeleteConfirm : Button

    //pop up for delete ride
    private lateinit var deleteRideSpn : Spinner
    private lateinit var conDeleteRideCancel : Button
    private lateinit var conDeleteRideConfirm : Button


    //other
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var settingsBtn : ImageView
    private lateinit var notificationBtn : ImageView
    var selectedRide =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ridePage = intent.getStringExtra("RidePage")


        //THIS IS FOR THE TOGGLE

        staffPage = findViewById(R.id.staffPage)
        ridesPage = findViewById(R.id.ridesPage)
        toggleGroup = findViewById(R.id.toggleGroup)
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        if(ridePage != null && ridePage == "true")
        {
            staffPage.visibility = View.GONE
            ridesPage.visibility = View.VISIBLE
        }

        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val staffViewButton = findViewById<MaterialButton>(R.id.staffViewButton)
        val ridesViewButton = findViewById<MaterialButton>(R.id.ridesViewButton)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            // Check if the Staff button is selected
            if (checkedId == R.id.staffViewButton && isChecked) {
                // Change the colors when Staff is selected
                staffViewButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.customGreen)))
                ridesViewButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightPurple)))
                staffPage.visibility = View.VISIBLE
                ridesPage.visibility = View.GONE
            }
            // Check if the Rides button is selected
            else if (checkedId == R.id.ridesViewButton && isChecked) {
                // Change the colors when Rides is selected
                ridesViewButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.customGreen)))
                staffViewButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightPurple)))
                staffPage.visibility = View.GONE
                ridesPage.visibility = View.VISIBLE
            }
        }

        //THIS IS FOR POP UPS AND BUTTON FUNCTIONS

        dialog = Dialog(this)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
        dialog.setCancelable(true)

        //binding main menu buttons
        addStaffButton = findViewById(R.id.addStaffButton)
        removeStaffButton = findViewById(R.id.removeStaffButton)
        createNewStaff = findViewById(R.id.createStaffButton)
        notificationBtn = findViewById(R.id.notification)
        settingsBtn = findViewById(R.id.settings)
        generateBoardBtn = findViewById(R.id.generateBoardButton)

        settingsBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Settings_Page::class.java)
            startActivity(intent)
        })

        notificationBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Notifications_Page::class.java)
            startActivity(intent)
        })

        generateBoardBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, Staff_Selection::class.java))
        })


        addStaffButton.setOnClickListener(View.OnClickListener {

            showLoading()
            dialog.setContentView(R.layout.add_staff_dialogue)
            addStaffCancel = dialog.findViewById(R.id.AddStaffCancel)
            addStaffConfirm = dialog.findViewById(R.id.AddStaffConfirm)
            staffSelect = dialog.findViewById(R.id.staffSelect)
            rideSelect = dialog.findViewById(R.id.rideSelect)

            var selectedStaff = ""
            var selectedRide = ""

            // Initialize the staff members list
            val staffMembers = mutableListOf<String>()
            fh.getAllStaff { staffArray ->
                staffMembers.add("Select Staff")
                for (s in staffArray) {
                    staffMembers.add(s.Name)
                }

                // Set up the Adapter for staffSelect spinner
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,  // Default spinner layout
                    staffMembers
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Dropdown style
                staffSelect.adapter = adapter

                // Initialize a variable to hold the selected staff
                val untrainedRides = mutableListOf<String>()

                staffSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedStaff = staffMembers[position]
                        untrainedRides.clear()
                        if (selectedStaff != "Select Staff") {
                            // Fetch the selected staff's details
                            fh.getDocumentFromName(selectedStaff) { staff ->
                                if (staff != null) {
                                    // Get the list of rides the staff is already trained on
                                    val trainedRides = staff.RidesTrained

                                    // Get all rides and filter the ones the staff is not trained on

                                    fh.getAllRides { rideArray ->
                                        untrainedRides.add("Select Ride")
                                        for (ride in rideArray) {
                                            if (!trainedRides.contains(ride.Name + " Op") && !trainedRides.contains(ride.Name + " Att")) {
                                                // if it doesnt contain the ride name
                                                if(calculateAge(staff.DoB) >= ride.minAgeToOperate || ((calculateAge(staff.DoB) >= ride.minAgeToAttend) && ride.prefNumAtt > 0))
                                                {//if they are old enough to operate OR old enough to attend AND the ride needs attendants

                                                    //this add attendant rides to 18+
                                                    if(!staff.Category.equals("Attendant") && ride.minAgeToOperate == 16)
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
                                                if(calculateAge(staff.DoB) >= ride.minAgeToOperate && ride.prefNumAtt > 0){
                                                    untrainedRides.add(ride.Name + " Att")
                                                }


                                            }
                                            else if(!trainedRides.contains(ride.Name + " Op") && trainedRides.contains(ride.Name + " Att"))
                                            {
                                                if(calculateAge(staff.DoB) >= ride.minAgeToOperate && ride.prefNumAtt > 0){
                                                    untrainedRides.add(ride.Name + " Op")
                                                }
                                            }
                                        }

                                        // Update the ride spinner with the untrained rides
                                        val rideAdapter = ArrayAdapter(
                                            this@MainActivity,
                                            android.R.layout.simple_spinner_item,
                                            untrainedRides
                                        )
                                        rideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                        rideSelect.adapter = rideAdapter
                                    }
                                }
                            }
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Optional: Handle no selection
                    }
                }

                // Now handle the ride selection
                rideSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedRide = untrainedRides[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Optional: Handle no selection
                    }
                }

                hideLoading()
                dialog.show()
            }

            addStaffCancel.setOnClickListener {
                dialog.dismiss()
            }

            addStaffConfirm.setOnClickListener {
                if (selectedStaff != "Select Staff" && selectedRide != "Select Ride") {
                    // Proceed with adding the staff to the ride
                    fh.getDocumentFromName(selectedStaff) { staff ->
                        if (staff != null) {
                            val updatedRidesList: MutableList<Any> = staff.RidesTrained.toMutableList()
                            var foundRide: Ride? = null
                            fh.getAllRides { rideArray ->
                                for (ride in rideArray) {
                                    if (ride.Name == selectedRide) {
                                        foundRide = ride
                                        break
                                    }
                                }

                                // At this point you have a Staff and Ride object
                                if (foundRide != null && foundRide!!.minAgeToOperate <= calculateAge(staff.DoB)) {//if staff able to operate
                                    var alreadyTrained = false
                                    for (r in staff.RidesTrained) {
                                        if (r.equals(selectedRide + " Op")) {
                                            alreadyTrained = true
                                        }
                                    }
                                    if (!alreadyTrained) {
                                        updatedRidesList.add(selectedRide + " Op")
                                        if(foundRide!!.minAgeToOperate <= calculateAge(staff.DoB) && foundRide!!.prefNumAtt > 0)
                                        {
                                            updatedRidesList.add(selectedRide + " Att")
                                        }
                                        val currentMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesList)
                                        db.collection("Staff").document(staff.Id).update(currentMap).addOnSuccessListener {
                                            fh.getRideFromName(selectedRide) { ride ->
                                                if (ride != null) {
                                                    var updatedStaffTrained: MutableList<Any> = ride.staffTrained.toMutableList()
                                                    updatedStaffTrained.add(selectedStaff + " Op")
                                                    if(foundRide!!.minAgeToOperate <= calculateAge(staff.DoB) && foundRide!!.prefNumAtt > 0)
                                                    {
                                                        updatedStaffTrained.add(selectedStaff + " Att")
                                                    }
                                                    val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)
                                                    db.collection("Rides").document(ride.Id).update(currentRideMap).addOnSuccessListener {
                                                        Toast.makeText(this@MainActivity, "Added $selectedStaff to $selectedRide", Toast.LENGTH_SHORT).show()
                                                        dialog.dismiss()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(this@MainActivity, "$selectedStaff is already trained on $selectedRide", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else
                                {
                                    if(foundRide != null && foundRide!!.minAgeToAttend <= calculateAge(staff.DoB) && foundRide!!.prefNumAtt > 0)
                                    {
                                        updatedRidesList.add(selectedRide + " Att")

                                        val currentMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesList)
                                        db.collection("Staff").document(staff.Id).update(currentMap).addOnSuccessListener {
                                            fh.getRideFromName(selectedRide) { ride ->
                                                if (ride != null) {
                                                    var updatedStaffTrained: MutableList<Any> = ride.staffTrained.toMutableList()
                                                    updatedStaffTrained.add(selectedStaff + " Att")
                                                    val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)
                                                    db.collection("Rides").document(ride.Id).update(currentRideMap).addOnSuccessListener {
                                                        Toast.makeText(this@MainActivity, "Added $selectedStaff to $selectedRide", Toast.LENGTH_SHORT).show()
                                                        dialog.dismiss()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        for (ride in rideArray) {
                                            if (selectedRide.contains(ride.Name)) {
                                                if(ride.prefNumAtt > 0)
                                                {
                                                    if(selectedRide.contains("Op"))
                                                    {
                                                        //add the op to both lists
                                                        updatedRidesList.add(selectedRide)

                                                        val currentMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesList)
                                                        db.collection("Staff").document(staff.Id).update(currentMap).addOnSuccessListener {
                                                            val strippedRide = selectedRide.removeSuffix(" Op")

                                                            fh.getRideFromName(strippedRide) { ride ->
                                                                if (ride != null) {
                                                                    var updatedStaffTrained: MutableList<Any> = ride.staffTrained.toMutableList()
                                                                    updatedStaffTrained.add(selectedStaff + " Op")
                                                                    val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)
                                                                    db.collection("Rides").document(ride.Id).update(currentRideMap).addOnSuccessListener {
                                                                        Toast.makeText(this@MainActivity, "Added $selectedStaff to $selectedRide", Toast.LENGTH_SHORT).show()
                                                                        dialog.dismiss()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        break


                                                        Toast.makeText(this, "ADDING OP", Toast.LENGTH_SHORT).show()
                                                        break
                                                    }
                                                    else if(selectedRide.contains("Att"))
                                                    {
                                                        updatedRidesList.add(selectedRide)

                                                        val currentMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesList)
                                                        db.collection("Staff").document(staff.Id).update(currentMap).addOnSuccessListener {
                                                            val strippedRide = selectedRide.removeSuffix(" Att")

                                                            fh.getRideFromName(strippedRide) { ride ->
                                                                if (ride != null) {
                                                                    var updatedStaffTrained: MutableList<Any> = ride.staffTrained.toMutableList()
                                                                    updatedStaffTrained.add(selectedStaff + " Att")
                                                                    val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)
                                                                    db.collection("Rides").document(ride.Id).update(currentRideMap).addOnSuccessListener {
                                                                        Toast.makeText(this@MainActivity, "Added $selectedStaff to $selectedRide", Toast.LENGTH_SHORT).show()
                                                                        dialog.dismiss()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        break
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Staff or Ride not selected", Toast.LENGTH_SHORT).show()
                }
            }
        })

        //removing staff from ride button + popup function
        removeStaffButton.setOnClickListener(View.OnClickListener {
            showLoading()
            dialog.setContentView(R.layout.remove_staff_dialog)

            // Initialize views in remove_staff_dialog layout
            removeStaffCancel = dialog.findViewById(R.id.RemoveStaffCancel)
            removeStaffConfirm = dialog.findViewById(R.id.RemoveStaffConfirm)
            rideRemoveSelect = dialog.findViewById(R.id.rideRemoveSelect)
            staffRemoveSelect = dialog.findViewById(R.id.staffRemoveSelect)

            val staffMembers = mutableListOf<String>()
            var selectedStaff: Staff? = null
                fh.getAllStaff { staffArray ->
                    staffMembers.add("Select Staff")
                    for (s in staffArray) {
                        staffMembers.add(s.Name)
                    }

                    // Set up staff spinner adapter
                    val staffAdapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        staffMembers
                    )
                    staffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    staffRemoveSelect.adapter = staffAdapter

                    // Handle staff selection
                    staffRemoveSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            if (position == 0) {
                                // Reset selected staff and clear the ride spinner
                                selectedStaff = null
                                updateRideSpinner(emptyList())
                            } else {
                                // Update selected staff and fetch their rides
                                selectedStaff = staffArray[position - 1] // Account for "Select Staff" at index 0
                                val ridesTrained = selectedStaff?.RidesTrained?.map { it.toString() } ?: emptyList()
                                updateRideSpinner(ridesTrained)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Optional: Handle no selection
                        }
                    }

                    hideLoading()
                    dialog.show()
                }

            removeStaffCancel.setOnClickListener {
                dialog.dismiss()
            }

            removeStaffConfirm.setOnClickListener {
                // Add logic to handle staff and ride removal
                if (selectedStaff == null || selectedRide.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please select both staff and a ride.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Confirm removal (to be implemented in the next steps)

                val rideNameWithType = selectedRide // Assuming selectedRide is the ride name with " Op" or " Att" at the end

                var rideBaseName = rideNameWithType
                var rideType = ""

                if (rideNameWithType.endsWith(" Op", ignoreCase = true)) {
                    rideBaseName = rideNameWithType.replace(" Op", "").trim()
                    rideType = " Op"
                } else if (rideNameWithType.endsWith(" Att", ignoreCase = true)) {
                    rideBaseName = rideNameWithType.replace(" Att", "").trim()
                    rideType = " Att"
                }

                fh.getRideFromName(rideBaseName){RideObj ->
                    if(RideObj != null){
                        //remove ride from STAFF list
                        val ridesTrained = selectedStaff?.RidesTrained?.toMutableList() ?: mutableListOf() // Get a mutable copy of RidesTrained
                        ridesTrained.remove(rideNameWithType)
                        val updatedRidesMap = hashMapOf<String, Any>("ridesTrained" to ridesTrained)
                        db.collection("Staff").document(selectedStaff!!.Id).update(updatedRidesMap).addOnSuccessListener {

                            val staffToRemove = selectedStaff!!.Name + rideType
                            val updatedStaffTrained = RideObj.staffTrained.toMutableList()
                            updatedStaffTrained.remove(staffToRemove)
                            val updatedRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)
                            db.collection("Rides").document(RideObj.Id).update(updatedRideMap).addOnSuccessListener {
                                // Handle success (e.g., show a message, dismiss dialog)
                                Toast.makeText(this@MainActivity, "Removed ${selectedStaff!!.Name} from $selectedRide", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        }
                    }
                }

            }


        })

        //create new staff button + pop up function
        createNewStaff.setOnClickListener(View.OnClickListener {
            dialog.setContentView(R.layout.create_new_staff)

            createStaffCancel = dialog.findViewById(R.id.createStaffCancel)
            createStaffConfirm = dialog.findViewById(R.id.createStaffConfirm)
            nameInput = dialog.findViewById(R.id.staffNameInput)
            dobInput = dialog.findViewById(R.id.staffDobInput)

            createStaffCancel.setOnClickListener {
                dialog.dismiss()
            }

            createStaffConfirm.setOnClickListener {
                val name = nameInput.text.toString()
                val dob = dobInput.text.toString()


                if(name.isEmpty() || dob.isEmpty() )
                {
                    Toast.makeText(this, "Please fill in fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(!convertStrToTime(dob).toDate().before(Calendar.getInstance().time) || !dob.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$".toRegex() ))
                {
                    Toast.makeText(this, "Invalid Date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.dismiss()
                fh.getColectionSize("Staff") {
                    //are we missing a document number? yes use that number, no use next number
                    fh.missingDocNum("Staff") {

                        val staffId = it.toString()
                        val s = Staff(
                            Id = staffId,
                            Name = name,
                            PreviousRide = "",
                            DoB = convertStrToTime(dob),
                            RidesTrained = ArrayList<String>(),
                            Category = when {
                                calculateAge(convertStrToTime(dob)) < 18 -> "Attendant"
                                calculateAge(convertStrToTime(dob)) in 18..20 -> "Fairground"
                                calculateAge(convertStrToTime(dob)) > 20 -> "SRO"
                                else -> ""
                            }
                        )
                        db.collection("Staff").document(staffId).set(s).addOnSuccessListener {

                            Toast.makeText(this, "Staff created", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            dialog.show()
        })


        viewStaffButton = findViewById(R.id.viewStaffButton)
        viewStaffButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ViewStaffRide::class.java)
            startActivity(intent)
        })

        //delete staff button + pop up function
        deleteStaffbtn = findViewById(R.id.deleteStaffButton)
        deleteStaffbtn.setOnClickListener(View.OnClickListener {
            dialog.setContentView(R.layout.delete_staff_dialog)
            deleteStaffCancel = dialog.findViewById(R.id.deleteStaffCancel)
            deleteStaffConfirm = dialog.findViewById(R.id.deleteStaffConfirm)
            staffDeleteSelect = dialog.findViewById(R.id.staffDeleteSelect)

            val staffMembers = mutableListOf<String>()
            fh.getAllStaff { staffArray ->
                staffMembers.add("Select Staff")
                for(s in staffArray){
                    staffMembers.add(s.Name)
                }
                // Set up the Adapter
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,  // Default spinner layout
                    staffMembers
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Dropdown style
                staffDeleteSelect.adapter = adapter
                // Handle item selection
                var selectedItem = ""
                staffDeleteSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedItem = staffMembers[position]

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Optional: Handle no selection
                    }
                }

                deleteStaffCancel.setOnClickListener {
                    dialog.dismiss()
                }
                deleteStaffConfirm.setOnClickListener {
                    //are you sure you want to delete this staff
                    //close both
                    //show toast "staff deleted"
                    dialog2 = Dialog(this)
                    dialog2.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    dialog2.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
                    dialog2.setCancelable(false)
                    dialog2.setContentView(R.layout.deletion_confirmation)
                    conDeleteCancel = dialog2.findViewById(R.id.ConDeleteStaffCancel)
                    conDeleteConfirm = dialog2.findViewById(R.id.ConDeleteStaffConfirm)

                    conDeleteCancel.setOnClickListener(View.OnClickListener {
                        dialog2.dismiss()
                    })
                    conDeleteConfirm.setOnClickListener(View.OnClickListener {
                        fh.getDocumentFromName(selectedItem){staff ->
                            if(staff!=null) {
                                db.collection("Staff").document(staff.Id).delete().addOnSuccessListener {
                                    fh.getAllRides { result ->
                                        for(ride in result)
                                        {
                                            for(nameListed in ride.staffTrained)
                                            {
                                                if(nameListed.equals(selectedItem))
                                                {
                                                    var updatedStaffTrained: MutableList<Any> = ride.staffTrained.toMutableList()
                                                    updatedStaffTrained.remove(selectedItem)
                                                    val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)//create the map to update
                                                    db.collection("Rides").document(ride.Id).update(currentRideMap).addOnSuccessListener {
                                                        Toast.makeText(this@MainActivity, "Deleted: $selectedItem", Toast.LENGTH_SHORT).show()
                                                        dialog.dismiss()
                                                        dialog2.dismiss()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }



                    })
                    if (selectedItem != "Select Staff") {
                        dialog2.show()
                    }
                    else
                    {
                        Toast.makeText(this@MainActivity, "Staff not selected", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.show()
            }
        })


        createRideBtn = findViewById(R.id.createRideButton)
        createRideBtn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CreateNewRidePage::class.java)
            startActivity(intent)

        })

        DeleteRideBtn = findViewById(R.id.deleteRideButton)
        DeleteRideBtn.setOnClickListener(View.OnClickListener {
            dialog.setContentView(R.layout.delete_ride_dialogue)
            conDeleteRideCancel = dialog.findViewById(R.id.deleteRideCancel)
            conDeleteRideConfirm = dialog.findViewById(R.id.deleteRideConfirm)
            deleteRideSpn = dialog.findViewById(R.id.rideDeleteSelect)
            val rides = mutableListOf<String>()
            fh.getAllRides { rideArray ->
                rides.add("Select Ride")
                for(s in rideArray){
                    rides.add(s.Name)
                }
                // Set up the Adapter
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,  // Default spinner layout
                    rides
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Dropdown style
                deleteRideSpn.adapter = adapter
                // Handle item selection
                var selectedItem = ""
                deleteRideSpn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedItem = rides[position]

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Optional: Handle no selection
                    }
                }

                conDeleteRideCancel.setOnClickListener {
                    dialog.dismiss()
                }
                conDeleteRideConfirm.setOnClickListener {
                    dialog2 = Dialog(this)
                    dialog2.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    dialog2.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
                    dialog2.setCancelable(false)
                    dialog2.setContentView(R.layout.deletion_confirmation)
                    conDeleteCancel = dialog2.findViewById(R.id.ConDeleteStaffCancel)
                    conDeleteConfirm = dialog2.findViewById(R.id.ConDeleteStaffConfirm)

                    conDeleteCancel.setOnClickListener(View.OnClickListener {
                        dialog2.dismiss()
                    })
                    conDeleteConfirm.setOnClickListener(View.OnClickListener {

                        fh.getRideFromName(selectedItem){ride ->
                            if(ride!=null) {
                                db.collection("Rides").document(ride.Id).delete().addOnSuccessListener {

                                    fh.getAllStaff { result->
                                        for(staff in result)
                                        {
                                            for(staffList in staff.RidesTrained)
                                            {
                                                if(staffList.equals(selectedItem))
                                                {
                                                    var updatedRidesTrained: MutableList<Any> = staff.RidesTrained.toMutableList()
                                                    updatedRidesTrained.remove(selectedItem)
                                                    val currentStaffMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesTrained)//create the map to update
                                                    db.collection("Staff").document(staff.Id).update(currentStaffMap).addOnSuccessListener {
                                                        Toast.makeText(this@MainActivity, "Deleted: $selectedItem", Toast.LENGTH_SHORT).show()
                                                        dialog.dismiss()
                                                        dialog2.dismiss()

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                    if(selectedItem != "Select Ride")
                    {
                        dialog2.show()
                    }
                    else
                    {
                        Toast.makeText(this@MainActivity, "Ride not selected", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.show()
            }


        })
    }

    // Override onTouchEvent to detect swipe gestures
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    // Custom GestureDetector to detect swipe gestures
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        // Detect swipe left or right
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean
        {
            val SWIPE_THRESHOLD = 100
            val SWIPE_VELOCITY_THRESHOLD = 100

            try {
                if (e1 == null || e2 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                // Check if the swipe is horizontal (left or right)
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        // Swipe left or right
                        if (diffX > 0) {
                            // Swipe right -> Show Staff
                            toggleGroup.check(R.id.staffViewButton)
                        } else {
                            // Swipe left -> Show Rides
                            toggleGroup.check(R.id.ridesViewButton)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
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

    private fun showLoading() {
        // Create and show the dialog
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_overlay)
        loadingDialog.setCancelable(false) // Prevent dismissal by tapping outside
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Make dialog background transparent
        loadingDialog.show()
    }

    private fun hideLoading() {
        // Hide the dialog when the task is done
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
    fun updateRideSpinner(rides: List<String>) {
        val rideList = mutableListOf<String>()
        rideList.add("Select Ride") // Default entry
        rideList.addAll(rides)

        val rideAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            rideList
        )
        rideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rideRemoveSelect.adapter = rideAdapter

        // Handle ride selection
        rideRemoveSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedRide = if (position == 0) "" else rideList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: Handle no selection
            }
        }
    }

}
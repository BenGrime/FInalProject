package com.example.timetabler

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

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
    private lateinit var dialog : Dialog
    private lateinit var trainStaff: Button

    private lateinit var loadingDialog: Dialog
    private lateinit var textViewBulk: TextView
    private lateinit var dropdownsBulk: GridLayout
    private lateinit var addStaffCancel: MaterialButton
    private lateinit var addStaffConfirm: MaterialButton
    private lateinit var addRideToListBtn: MaterialButton

    private val db = FirebaseFirestore.getInstance()

    var fh = FirebaseHandler()
    var selectedRide: Ride? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_ride)


        val rideSelected = intent.getStringExtra("rideName")

        // Initialize views
        backBtnViewRide = findViewById(R.id.backBtnViewRide)
        trainStaff = findViewById(R.id.trainStaffOnRideBtn)
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

        trainStaff.setOnClickListener{
            if(selectedRide != null)
            {
                //show pop-up or page with "add ride(s) to selected Staff
                dialog = Dialog(this)
                dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.train_staff_bulk)
                addRideToListBtn = dialog.findViewById(R.id.addStaffButtonBulk)
                textViewBulk = dialog.findViewById(R.id.textViewBulkRides)
                dropdownsBulk = dialog.findViewById(R.id.dropdownsBulkRides)
                addStaffCancel = dialog.findViewById(R.id.AddRidesCancelBulk)
                addStaffConfirm = dialog.findViewById(R.id.AddRidesConfirmBulk)
                //on it, have a add another ride which adds a textView and Spinner to grid layout

                textViewBulk.text = "Add Rides to " + selectedRide!!.Name
                // Get the height of the screen


                addRideToListBtn.setOnClickListener {
                    val screenHeight = Resources.getSystem().displayMetrics.heightPixels

                    val dialogHeight = dialog.window?.decorView?.height ?: 0
                    if(dialogHeight <= screenHeight-100){
                        addRideElement()
                    }
                    else
                    {
                        addRideToListBtn.alpha = 0.5f
                        addRideToListBtn.isEnabled = false
                        Toast.makeText(this, "Max amount of rides reached", Toast.LENGTH_SHORT).show()
                    }

                }
                addStaffCancel.setOnClickListener{
                    dialog.dismiss()
                }

                addStaffConfirm.setOnClickListener {
                    showLoading()
                    // List to store selected items from the spinners
                    val selectedItems = mutableSetOf<String>()

                    // Boolean to track duplicates
                    var hasDuplicates = false

                    // Iterate through the GridLayout's children
                    for (i in 0 until dropdownsBulk.childCount) {
                        val view = dropdownsBulk.getChildAt(i)
                        if (view is LinearLayout) { // Check if the view is a LinearLayout (which contains the TextView and Spinner)
                            // Iterate through the child views of the LinearLayout
                            for (j in 0 until view.childCount) {
                                val innerView = view.getChildAt(j)
                                if (innerView is Spinner) { // Check if the child view is a Spinner
                                    val selectedItem = innerView.selectedItem?.toString()
                                    if (!selectedItem.isNullOrEmpty() && selectedItem != "Select Ride") {
                                        // Check if the item is already in the set
                                        if (!selectedItems.add(selectedItem)) {
                                            hasDuplicates = true
                                            break
                                        }
                                    } else {
                                        Toast.makeText(this, "Please select rides.", Toast.LENGTH_SHORT).show()
                                        break
                                    }
                                }
                            }
                        }
                    }

                    // Handle result
                    if (hasDuplicates) {
                        Toast.makeText(this, "Each ride must be unique. Please select different rides.", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        val currentRidesStaff: MutableList<Any> = selectedRide?.staffTrained!!.toMutableList()
                        var completedStaff = 0
                        val totalStaff = selectedItems.size

                        // Function to handle ride completion
                        fun onRideUpdated() {
                            completedStaff++
                            if (completedStaff == totalStaff) {
                                // Update staff only after all rides are updated
                                val updateMap = hashMapOf<String, Any>("staffTrained" to currentRidesStaff)
                                db.collection("Rides").document(selectedRide!!.Id).update(updateMap).addOnSuccessListener {
                                    Toast.makeText(this, "All updated", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                    hideLoading()
                                    recreate()

                                }
                            }
                        }

                        // Iterate through selected rides
                        for (item in selectedItems) {
                            fh.getDocumentFromName(item) { staff ->
                                staff?.let {
                                    val currentStaffTrained: MutableList<Any> = it.RidesTrained.toMutableList()
                                    when (staff.Category) {
                                        "SRO" -> {
                                            currentStaffTrained.add("${selectedRide?.Name} Op")
                                            currentRidesStaff.add("${staff.Name} Op")
                                            if (selectedRide?.prefNumAtt!! > 0) {
                                                currentStaffTrained.add("${selectedRide?.Name} Att")
                                                currentRidesStaff.add("${staff.Name} Att")
                                            }
                                        }
                                        "Fairground" -> {
                                            if (selectedRide?.minAgeToOperate!! <= calculateAge(staff.DoB)) {
                                                currentStaffTrained.add("${selectedRide?.Name} Op")
                                                currentRidesStaff.add("${staff.Name} Op")
                                            }
                                            if (selectedRide?.prefNumAtt!! > 0 && selectedRide?.minAgeToAttend!! <= calculateAge(staff.DoB)) {
                                                currentStaffTrained.add("${selectedRide?.Name} Att")
                                                currentRidesStaff.add("${staff.Name} Att")
                                            }
                                        }
                                        else -> {
                                            if (selectedRide?.minAgeToOperate!! == 16) {
                                                currentStaffTrained.add("${selectedRide?.Name} Op")
                                                currentRidesStaff.add("${staff.Name} Op")
                                            } else {
                                                currentStaffTrained.add("${selectedRide?.Name} Att")
                                                currentRidesStaff.add("${staff.Name} Att")
                                            }
                                        }
                                    }
                                    println("BREAK")
                                    val updateRideMap = hashMapOf<String, Any>("ridesTrained" to currentStaffTrained)
                                    db.collection("Staff").document(staff.Id).update(updateRideMap).addOnSuccessListener {
                                        onRideUpdated()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Failed to update ride: ${staff.Name}", Toast.LENGTH_SHORT).show()
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

        // Handle ride selection
        rideSelectView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedRide = rideList[position - 1]  // Account for the "Select Ride" option
                    editBtnRide.isEnabled = true
                    editBtnRide.alpha = 1f

                    trainStaff.isEnabled = true
                    trainStaff.alpha = 1f

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
                    trainStaff.isEnabled = false
                    trainStaff.alpha = 0.5f
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Disable the edit button if no ride is selected
                editBtnRide.isEnabled = false
                editBtnRide.alpha = 0.5f
                trainStaff.isEnabled = false
                trainStaff.alpha = 0.5f
            }
        }
    }


    private fun addRideElement() {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.MATCH_PARENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(6, 10, 6, 10)
            }
            val border = ShapeDrawable().apply {
                paint.color = resources.getColor(R.color.black, theme)  // Set the border color
                paint.strokeWidth = 2f  // Set the border width
                paint.style = Paint.Style.STROKE  // Only draw the border, not filled
            }

            // Set the border as background
            background = border
        }

        // Create TextView and Spinner (use AppCompat versions)
        val textView = AppCompatTextView(this).apply {
            text = "Staff Name"
            textSize = 18f
            setTextColor(resources.getColor(R.color.black, theme))
            setPadding(6, 6, 6, 6)
            // Set width to match_parent and height to wrap_content
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(6, 6, 6, 6)
            }
        }

        val spinner = AppCompatSpinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(6, 6, 6, 6)
            }
            setPadding(6, 6, 6, 6)
        }
        val untrainedStaff = mutableListOf<String>()
        var trainedStaff = selectedRide?.staffTrained

        fh.getAllStaff { staffList ->
            untrainedStaff.add("Select Ride")
            for (staff in staffList) {
                if (!trainedStaff?.contains(staff.Name + " Op")!! && !trainedStaff.contains(staff.Name + " Att")) {
                    // if it doesnt contain the ride name
                    if(calculateAge(staff.DoB) >= selectedRide?.minAgeToOperate!! || ((calculateAge(staff.DoB) >= selectedRide?.minAgeToAttend!!) && selectedRide?.prefNumAtt!! > 0))
                    {//if they are old enough to operate OR old enough to attend AND the ride needs attendants

                        //this add attendant rides to 18+
                        if(staff.Category != "Attendant" && selectedRide?.minAgeToOperate!! == 16)
                        {

                        }
                        else
                        {
                            untrainedStaff.add(staff.Name)
                        }
                    }

                }
                //what if they are only the Att or Op and we want to add the other to a ride that has the option for the other
                else if(trainedStaff.contains(selectedRide?.Name!! + " Op") && !trainedStaff.contains(selectedRide?.Name!! + " Att"))
                {
                    if(calculateAge(staff.DoB) >= selectedRide?.minAgeToOperate!! && selectedRide?.prefNumAtt!! > 0){
                        untrainedStaff.add(staff.Name + " Att")
                    }


                }
                else if(!trainedStaff.contains(selectedRide?.Name!! + " Op") && !trainedStaff.contains(selectedRide?.Name!! + " Att"))
                {
                    if(calculateAge(staff.DoB) >= selectedRide?.minAgeToOperate!! && selectedRide?.prefNumAtt!! > 0){
                        untrainedStaff.add(staff.Name + " Op")
                    }
                }
            }
            // Add dummy items to the Spinner
            val adapter = ArrayAdapter(
                this@ViewRide,
                android.R.layout.simple_spinner_item,
                untrainedStaff
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Add TextView and Spinner to the row
            rowLayout.addView(textView)
            rowLayout.addView(spinner)

            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val dialogHeight = dialog.window?.decorView?.height ?: 0
            if(dialogHeight >= screenHeight-100){
                addRideToListBtn.alpha = 0.5f
                addRideToListBtn.isEnabled = false
                Toast.makeText(this, "Max amount of rides reached", Toast.LENGTH_SHORT).show()
            }
        }



        // Gesture Detector for swipe-to-delete
        val gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - (e1?.x ?: 0f)
                if (Math.abs(diffX) > Math.abs(e2.y - (e1?.y ?: 0f))) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX < 0) { // Swipe left
                            animateRowRemoval(rowLayout)
                            addRideToListBtn.alpha = 1f
                            addRideToListBtn.isEnabled = true
                            return true
                        }
                    }
                }
                return false
            }
        })

        // Attach swipe listener
        val swipeListener = View.OnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick() // Trigger performClick for accessibility
            }
            true
        }

        // Apply swipe listener to both TextView and Spinner
        textView.setOnTouchListener(swipeListener)
        spinner.setOnTouchListener(swipeListener)

        // Add the row to GridLayout
        dropdownsBulk.addView(rowLayout)
    }


    // Smooth slide-out animation before removal
    private fun animateRowRemoval(rowLayout: LinearLayout) {
        rowLayout.animate()
            .translationX(-rowLayout.width.toFloat()) // Slide left
            .alpha(0.0f) // Fade out
            .setDuration(300)
            .withEndAction {
                dropdownsBulk.removeView(rowLayout) // Remove after animation
            }
            .start()
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
}

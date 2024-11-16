package com.example.timetabler

import android.app.Dialog
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


class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    var fh = FirebaseHandler()

    //set dialog
    private lateinit var dialog : Dialog

    //main menu buttons
    private lateinit var addStaffButton : Button
    private lateinit var removeStaffButton : Button
    private lateinit var createNewStaff : Button
    private lateinit var deleteStaffbtn : Button
    private lateinit var viewStaffButton : Button

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


    //pop up for delete staff
    private lateinit var staffDeleteSelect : Spinner
    private lateinit var deleteStaffCancel : Button
    private lateinit var  deleteStaffConfirm : Button


    //other
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //THIS IS FOR THE TOGGLE

        staffPage = findViewById(R.id.staffPage)
        ridesPage = findViewById(R.id.ridesPage)
        toggleGroup = findViewById(R.id.toggleGroup)
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

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


        //adding staff to ride button + pop up function
        addStaffButton.setOnClickListener(View.OnClickListener {
            dialog.setContentView(R.layout.add_staff_dialogue)
            addStaffCancel = dialog.findViewById(R.id.AddStaffCancel)
            addStaffConfirm = dialog.findViewById(R.id.AddStaffConfirm)
            staffSelect = dialog.findViewById(R.id.staffSelect)
            rideSelect = dialog.findViewById(R.id.rideSelect)

            val staffMembers = mutableListOf<String>()
            fh.getAllStaff { staffArray ->
                staffMembers.add("Select Staff")
                for (s in staffArray) {
                    staffMembers.add(s.Name)
                }
                // Set up the Adapter
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,  // Default spinner layout
                    staffMembers
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Dropdown style
                staffSelect.adapter = adapter
                // Handle item selection
                var selectedItem = ""
                staffSelect.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedItem = staffMembers[position]

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Optional: Handle no selection
                        }
                    }

                //do rides now


                addStaffCancel.setOnClickListener {
                    dialog.dismiss()
                }
                addStaffConfirm.setOnClickListener{
                    if (selectedItem != "Select Staff") {
                        Toast.makeText(this@MainActivity, "Deleted: $selectedItem", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    Toast.makeText(this@MainActivity, "Staff not selected", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.show()
            }



        })

        //removing staff from ride button + popup function
        removeStaffButton.setOnClickListener(View.OnClickListener {
            dialog.setContentView(R.layout.remove_staff_dialog)

            // Initialize views in remove_staff_dialog layout
            removeStaffCancel = dialog.findViewById(R.id.RemoveStaffCancel)
            removeStaffConfirm = dialog.findViewById(R.id.RemoveStaffConfirm)

            removeStaffCancel.setOnClickListener {
                dialog.dismiss()
            }
            removeStaffConfirm.setOnClickListener{
                //delete Staff
                Toast.makeText(this, "FAKE: staff Removed from Ride", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            dialog.show()

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

                if(name.isEmpty() || dob.isEmpty())
                {
                    Toast.makeText(this, "Please fill in fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dialog.dismiss()

            }

            dialog.show()

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

                    if (selectedItem != "Select Staff") {
                        Toast.makeText(this@MainActivity, "Deleted: $selectedItem", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    Toast.makeText(this@MainActivity, "Staff not selected", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
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
}
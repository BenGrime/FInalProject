package com.example.timetabler

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup


class MainActivity : AppCompatActivity() {

    private lateinit var dialog : Dialog
    private lateinit var addStaffCancel : Button
    private lateinit var addStaffConfirm : Button

    private lateinit var addStaffButton : Button
    private lateinit var removeStaffButton : Button

    private lateinit var removeStaffCancel : Button
    private lateinit var removeStaffConfirm : Button

    private lateinit var staffPage : GridLayout
    private lateinit var ridesPage : GridLayout

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


        addStaffButton = findViewById(R.id.addStaffButton)
        removeStaffButton = findViewById(R.id.removeStaffButton)



        addStaffButton.setOnClickListener(View.OnClickListener {
            dialog.setContentView(R.layout.add_staff_dialogue)
            addStaffCancel = dialog.findViewById(R.id.AddStaffCancel)
            addStaffConfirm = dialog.findViewById(R.id.AddStaffConfirm)

            addStaffCancel.setOnClickListener {
                dialog.dismiss()
            }
            addStaffConfirm.setOnClickListener{
                Toast.makeText(this, "FAKE: staff added to Ride", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            dialog.show()

        })


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
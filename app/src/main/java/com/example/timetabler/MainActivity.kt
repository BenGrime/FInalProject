package com.example.timetabler

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //THIS IS FOR THE TOGGLE

        staffPage = findViewById(R.id.staffPage)
        ridesPage = findViewById(R.id.ridesPage)

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
        dialog.setCancelable(false)


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
}
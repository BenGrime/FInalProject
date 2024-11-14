package com.example.timetabler

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp


class MainActivity : AppCompatActivity() {

    private lateinit var dialog : Dialog
    private lateinit var addStaffCancel : Button
    private lateinit var addStaffConfirm : Button

    private lateinit var addStaffButton : Button
    private lateinit var removeStaffButton : Button

    private lateinit var removeStaffCancel : Button
    private lateinit var removeStaffConfirm : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
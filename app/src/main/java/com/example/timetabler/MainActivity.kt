package com.example.timetabler

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class MainActivity : AppCompatActivity() {

    private lateinit var dialog : Dialog
    private lateinit var addStaffCancel : Button
    private lateinit var addStaffConfirm : Button

    private lateinit var addStaffButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.add_staff_dialogue)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
        dialog.setCancelable(false)


        addStaffCancel = dialog.findViewById(R.id.AddStaffCancel)
        addStaffConfirm = dialog.findViewById(R.id.AddStaffConfirm)

        addStaffCancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })

        addStaffButton = findViewById(R.id.addStaffButton)
        addStaffButton.setOnClickListener(View.OnClickListener {
            dialog.show()

        })



    }
}
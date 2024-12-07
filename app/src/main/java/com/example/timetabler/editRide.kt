package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Locale

class editRide : AppCompatActivity() {

    private lateinit var cancelBtn : Button
    private lateinit var confrimBtn : Button
    private lateinit var  rideNameInput : TextInputEditText
    private lateinit var  rideMinOpInput : TextInputEditText
    private lateinit var  rideMinAttInput : TextInputEditText
    private lateinit var  minAttNumInput : TextInputEditText
    private lateinit var  minOpNumInput : TextInputEditText
    private lateinit var  prefNumAttInput : TextInputEditText
    private lateinit var  prefNumOpInput : TextInputEditText
    private lateinit var  openInput : Spinner

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ride)

        val ride = intent.getSerializableExtra("rideObject") as Ride

        confrimBtn = findViewById(R.id.editRideConfirm)
        cancelBtn = findViewById(R.id.editRideCancel)
        rideNameInput = findViewById(R.id.editrideNameInput)
        rideMinOpInput = findViewById(R.id.editrideMinOpInput)
        rideMinAttInput = findViewById(R.id.editrideMinAttInput)
        minAttNumInput = findViewById(R.id.editminAttNumInput)
        minOpNumInput = findViewById(R.id.editminOpNumInput)
        prefNumAttInput = findViewById(R.id.editprefNumAttInput)
        prefNumOpInput = findViewById(R.id.editprefNumOpInput)
        openInput = findViewById(R.id.editopenInput)

        rideNameInput.setText(ride.Name)
        rideMinOpInput.setText(ride.minAgeToOperate.toString())
        rideMinAttInput.setText(ride.minAgeToAttend.toString())
        minAttNumInput.setText(ride.minNumAtt.toString())
        minOpNumInput.setText(ride.minNumOp.toString())
        prefNumAttInput.setText(ride.prefNumAtt.toString())
        prefNumOpInput.setText(ride.prefNumOp.toString())

        val booleanArray = listOf("Select Option", "Yes", "No")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,  // Default spinner layout
            booleanArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        openInput.adapter = adapter
        val selectedItemPosition = when (ride.open) {
            true -> booleanArray.indexOf("Yes")
            false -> booleanArray.indexOf("No")
            else -> 0 // Default to "Select Option" if something unexpected happens
        }
        if (selectedItemPosition != -1) {
            openInput.setSelection(selectedItemPosition)
        } else {
            openInput.setSelection(0) // Fallback to "Select Option"
        }



        cancelBtn.setOnClickListener{
            finish()
        }
        confrimBtn.setOnClickListener{

            if (rideMinOpInput.text?.toString() == ride.minAgeToOperate.toString()
                && rideMinAttInput.text?.toString() == ride.minAgeToAttend.toString()
                && minAttNumInput.text?.toString() == ride.minNumAtt.toString()
                && minOpNumInput.text?.toString() == ride.minNumOp.toString()
                && prefNumAttInput.text?.toString() == ride.prefNumAtt.toString()
                && prefNumOpInput.text?.toString() == ride.prefNumOp.toString()
                && openInput.selectedItem.toString() == ride.open.toString()
            )
            {
                Toast.makeText(this, "Fields not been changed", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val openValue = when (openInput.selectedItem.toString() == "Yes"){
                    true -> true
                    false -> false
                }
                val updatedFields = mapOf(
                    "minAgeToOperate" to rideMinOpInput.text?.toString()?.toInt(),
                    "minAgeToAttend" to rideMinAttInput.text?.toString()?.toInt(),
                    "minNumAtt" to minAttNumInput.text?.toString()?.toInt(),
                    "minNumOp" to minOpNumInput.text?.toString()?.toInt(),
                    "prefNumAtt" to prefNumAttInput.text?.toString()?.toInt(),
                    "prefNumOp" to prefNumOpInput.text?.toString()?.toInt(),
                    "open" to openValue
                )
                db.collection("Rides").document(ride.Id).update(updatedFields).addOnSuccessListener {
                    Toast.makeText(this, "Ride Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
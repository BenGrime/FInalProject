package com.example.timetabler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class CreateNewRidePage : AppCompatActivity() {

    //set cancel and confirm buttons
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

    var fh = FirebaseHandler()
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_ride_page)

        cancelBtn = findViewById(R.id.createRideCancel)
        cancelBtn.setOnClickListener(View.OnClickListener {
            finish()
        })

        confrimBtn = findViewById(R.id.createRideConfirm)
        rideNameInput = findViewById(R.id.rideNameInput)
        rideMinOpInput = findViewById(R.id.rideMinOpInput)
        rideMinAttInput = findViewById(R.id.rideMinAttInput)
        minAttNumInput = findViewById(R.id.minAttNumInput)
        minOpNumInput = findViewById(R.id.minOpNumInput)
        prefNumAttInput = findViewById(R.id.prefNumAttInput)
        prefNumOpInput = findViewById(R.id.prefNumOpInput)
        openInput = findViewById(R.id.openInput)

        val booleanArray = listOf("Select Option", "Yes", "No")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,  // Default spinner layout
            booleanArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        openInput.adapter = adapter

        confrimBtn.setOnClickListener(View.OnClickListener {
            if (rideNameInput.text.isNullOrEmpty() ||
                rideMinOpInput.text.isNullOrEmpty() ||
                rideMinAttInput.text.isNullOrEmpty() ||
                minAttNumInput.text.isNullOrEmpty() ||
                minOpNumInput.text.isNullOrEmpty() ||
                prefNumAttInput.text.isNullOrEmpty() ||
                prefNumOpInput.text.isNullOrEmpty() ||
                openInput.selectedItem == "Select Option") {
                // Do something when all inputs are empty
                // For example, show a toast message or disable a button
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val selectedItem = openInput.selectedItem.toString()
                var openBool = selectedItem == "Yes"
                fh.getColectionSize("Rides"){
                    fh.missingDocNum("Rides") {

                        val rideId = it.toString()
                        val r = Ride(
                            Id = rideId,
                            Name =rideNameInput.text.toString(),
                            minAgeToOperate = rideMinOpInput.text.toString().toInt(),
                            minAgeToAttend = rideMinAttInput.text.toString().toInt(),
                            minNumAtt= minAttNumInput.text.toString().toInt(),
                            minNumOp= minOpNumInput.text.toString().toInt(),
                            open = openBool,
                            prefNumAtt= prefNumAttInput.text.toString().toInt(),
                            prefNumOp= prefNumOpInput.text.toString().toInt(),
                            staffTrained = ArrayList<String>()
                        )

                        db.collection("Rides").document(rideId).set(r).addOnSuccessListener {
                            fh.getPriority { p ->

                                p.add(Pair(r.Name, 3))
                                val newMap = p.map { innerList -> mapOf("ride" to innerList.first, "value" to innerList.second)
                                } as ArrayList<Map<String, Int>>
                                db.collection("Settings").document("RidePriority").set(mapOf("priorityList" to newMap)).addOnSuccessListener {
                                        Toast.makeText(this, "Ride created", Toast.LENGTH_SHORT).show()
                                        finish()
                                }
                            }
                        }

                    }

                }

            }

        })


    }
}
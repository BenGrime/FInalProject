package com.example.timetabler

import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class Settings_Page : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var priorityList : LinearLayout
    private lateinit var savePreferences : LinearLayout
    private lateinit var settingsGrid : GridLayout
    private lateinit var deleteData : LinearLayout
    private lateinit var priorityText : TextView
    private lateinit var backBtn : ImageView
    private lateinit var evalGrid : GridLayout
    private lateinit var evalList : LinearLayout
    private lateinit var evalText : TextView

    private lateinit var tv : TextView
    private lateinit var cancel : MaterialButton
    private lateinit var confirm : MaterialButton
    private lateinit var dialog: Dialog


    private lateinit var auth: FirebaseAuth

    private var spinnerList : ArrayList<Spinner> = ArrayList()

    var fh = FirebaseHandler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)
        auth = Firebase.auth
        savePreferences = findViewById(R.id.savePreferences)
        backBtn = findViewById(R.id.backBtnSettings)
        backBtn.setOnClickListener{
            finish()
        }
        settingsGrid = findViewById(R.id.settingsGrid)
        deleteData = findViewById(R.id.deleteStaffData)
        priorityList = findViewById(R.id.priorityListBtn)
        priorityText = findViewById(R.id.priorityText)
        deleteData.setOnClickListener{
            dialog = Dialog(this)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.deletion_confirmation)
            tv = dialog.findViewById(R.id.textViewDelete)
            tv.text = "Are you sure you want to delete ALL staff, TL and board Data?"
            cancel = dialog.findViewById(R.id.ConDeleteStaffCancel)
            confirm = dialog.findViewById(R.id.ConDeleteStaffConfirm)
            cancel.setOnClickListener{ dialog.dismiss()}
            confirm.setOnClickListener {
//                db.collection("Staff").get().addOnSuccessListener { result ->
//                    for (document in result) {
//                        db.collection("Staff").document(document.id).delete().addOnSuccessListener {
//                            db.collection("Board").get().addOnSuccessListener { result1 ->
//                                for (document in result1) {
//                                    db.collection("Board").document(document.id).delete().addOnSuccessListener {
//                                        db.collection("Managers").get().addOnSuccessListener { result1 ->
//                                            for (document in result1) {
//                                                if ((document.getLong("accessLevel")?.toInt() ?: -1) == 4) {
//                                                    db.collection("Managers").document(document.id).delete().addOnSuccessListener {
//                                                        fh.getAllRides { rideList ->
//                                                            for (r in rideList) {
//                                                                var emptyList = ArrayList<String>()
//                                                                val updateMap = hashMapOf<String, Any>("staffTrained" to emptyList)
//                                                                db.collection("Rides").document(r.Id).update(updateMap).addOnSuccessListener {
//                                                                    Toast.makeText(this, "All Deleted", Toast.LENGTH_SHORT).show()
//                                                                }
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
            }
            dialog.show()
        }
        priorityList.setOnClickListener{
            settingsGrid.visibility = if (settingsGrid.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if(settingsGrid.visibility == View.GONE){
                priorityText.text = "Edit Priority List"
            }
            else
            {
                priorityText.text = "Close Priority List"
            }
        }

        evalGrid = findViewById(R.id.evaluationGrid)
        evalList = findViewById(R.id.evaluationBtn)
        evalText = findViewById(R.id.evaluationText)
        evalList.setOnClickListener{
            evalGrid.visibility = if (evalGrid.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if(evalGrid.visibility == View.GONE){
                evalText.text = "Edit Priority List"
            }
            else
            {
                evalText.text = "Close Priority List"
            }
        }




        savePreferences.setOnClickListener{
            //save stuff
            //get all rides, using the ride add it to a pair with "5" add that to board
            var priorityList : ArrayList<Pair<String, Int>> = ArrayList()
            var counter = 0

            fh.getAllRides {
                for(r in it)
                {
                    var selected = spinnerList[counter].selectedItem as? Int

                    priorityList.add(Pair(r.Name, selected) as Pair<String, Int>)
                    counter++
                }
                val firebaseBoard = priorityList.map { innerList -> mapOf("ride" to innerList.first, "value" to innerList.second) } as ArrayList<Map<String, Int>>
                firebaseBoard.forEach { row -> println("Row: $row, Type: ${row::class.simpleName}") }
                db.collection("Settings").document("RidePriority").set(mapOf("priorityList" to firebaseBoard)).addOnSuccessListener{
                    var pointList : ArrayList<Pair<String, Int>> = ArrayList()
                    for (child in evalGrid) {
                        if (child is LinearLayout) {
                            var text: String? = null
                            var selectedValue: Int? = null

                            for (j in 0 until child.childCount) {
                                val innerChild = child.getChildAt(j)
                                when (innerChild) {
                                    is TextView -> {
                                        text = innerChild.text.toString()
                                    }
                                    is Spinner -> {
                                        selectedValue = innerChild.selectedItem as Int
                                    }
                                }
                            }

                            if (text != null && selectedValue != null) {
                                pointList.add(Pair(text, selectedValue))
                            }
                        }
                    }

                    val firebaseEval = pointList.map { innerList -> mapOf("name" to innerList.first, "value" to innerList.second) } as ArrayList<Map<String, Int>>
                    db.collection("Settings").document("EvaluationPoints").set(mapOf("evalList" to firebaseEval)).addOnSuccessListener{
                        Toast.makeText(this, "UPDATED", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fh.getPriority{result ->
            for(pair in result)
            {
                val name = pair.first
                val value = pair.second

                val rideLinearLayout = LinearLayout(this@Settings_Page).apply {
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
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.white) // Tint background
                }

                val rideTextView = TextView(this@Settings_Page).apply {
                    text = name // Set the ride name
                    textSize = 16f // Text size in SP
                    setTextColor(ContextCompat.getColor(context, R.color.black)) // Set text color
                    setPadding(8, 8, 8, 8) // Padding for the TextView
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                val spinner = Spinner(this@Settings_Page)
                val selection = listOf(1, 2, 3)
                val adapter = ArrayAdapter(this@Settings_Page, android.R.layout.simple_spinner_item, selection)
                adapter.setDropDownViewResource(R.layout.spinner_custom_dropdown)
                spinner.adapter = adapter
                val index = selection.indexOf(value)
                if (index >= 0) { // Ensure the value exists in the selection
                    spinner.setSelection(index)
                }

                // Add TextView and ImageView to the LinearLayout
                rideLinearLayout.addView(rideTextView)
                rideLinearLayout.addView(spinner)

                spinnerList.add(spinner)

                // Optionally add this LinearLayout to a parent layout (like GridLayout)
                settingsGrid.addView(rideLinearLayout)
            }
        }

        fh.getEvalPoints{evalPoints ->
            for(pair in evalPoints)
            {
                val name = pair.first
                val value = pair.second

                val rideLinearLayout = LinearLayout(this@Settings_Page).apply {
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
                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.white) // Tint background
                }

                val rideTextView = TextView(this@Settings_Page).apply {
                    text = name // Set the ride name
                    textSize = 16f // Text size in SP
                    setTextColor(ContextCompat.getColor(context, R.color.black)) // Set text color
                    setPadding(8, 8, 8, 8) // Padding for the TextView
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                val spinner = Spinner(this@Settings_Page)
                val selection = listOf(1, 2, 3, 4, 5)
                val adapter = ArrayAdapter(this@Settings_Page, android.R.layout.simple_spinner_item, selection)
                adapter.setDropDownViewResource(R.layout.spinner_custom_dropdown)
                spinner.adapter = adapter
                val index = selection.indexOf(value)
                if (index >= 0) { // Ensure the value exists in the selection
                    spinner.setSelection(index)
                }

                // Add TextView and ImageView to the LinearLayout
                rideLinearLayout.addView(rideTextView)
                rideLinearLayout.addView(spinner)

                // Optionally add this LinearLayout to a parent layout (like GridLayout)
                evalGrid.addView(rideLinearLayout)
            }

        }
    }
}
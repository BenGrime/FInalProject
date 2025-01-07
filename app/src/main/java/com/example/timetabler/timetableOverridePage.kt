package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class timetableOverridePage : AppCompatActivity() {

    var fh = FirebaseHandler()
    private val db = Firebase.firestore
    private var generateTimetable = GenerateTimetable()
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable_override_page)
        val selectedStaffList = intent.getSerializableExtra("RideStaffList") as? ArrayList<ArrayList<String>>
        val staffSelected = intent.getSerializableExtra("staffSelected") as? ArrayList<String>
        showLoading()


        textView = findViewById(R.id.label)

        if(selectedStaffList != null && staffSelected!= null)
        {
            fh.getSelectedStaffObjs(staffSelected) { staffList ->
                fh.getAllRides { rides ->
                    (rides as? ArrayList<Ride>)?.let {
                        generateTimetable.timetable1(selectedStaffList, staffSelected, staffList,
                            it
                        ){ result ->

                            result.forEach { row ->
                                val ride = row[0]
                                val staff = row[1]
                                textView.text = textView.text.toString() + "\nRide: " + ride + " ,Staff: " + staff //obviously this will be changed
                            }
                            //updateStaff(result)
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun updateStaff(result: ArrayList<ArrayList<String>>)
    {
        if(!result.isEmpty())
        {
            // Get the first element from the list
            val row = result.removeAt(0)
            val ride = row[0]
            val staff = row[1]
            //now you have the complete board....update the "previous ride" for all of them
            fh.getDocumentFromName(staff) { it ->
                if (it != null) {
                    val updateMap = mapOf("previousRide" to ride)
                    db.collection("Staff").document(it.Id).update(updateMap)
                        .addOnSuccessListener {

                            updateStaff(result)
                        }
                }
            }
        }
        hideLoading()
    }

    private fun showLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.GONE
    }
}
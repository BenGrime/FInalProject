package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.concurrent.atomic.AtomicInteger

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
                            val copy = ArrayList(result)
                            updateStaff(copy)
//                            hideLoading()
                        }
                    }
                }
            }
        }
    }

//    private fun updateStaff(list: ArrayList<ArrayList<String>>)
//    {
//        //create a copy of list into result
//        if(!list.isEmpty())
//        {
//            // Get the first element from the list
//            val row = list.removeAt(0)
//            val ride = row[0]
//            val staff = row[1]
//            //now you have the complete board....update the "previous ride" for all of them
//            fh.getDocumentFromName(staff) {staff->
//                if (staff != null) {
//                    val strippedRide = ride.replace(Regex("(Op|Att)\$"), "").trim()
//                    val updateMap = mapOf("previousRide" to strippedRide)
//                    db.collection("Staff").document(staff.Id).update(updateMap)
//                        .addOnSuccessListener {
//
//                            updateStaff(list)
//                        }
//                }
//            }
//        }
//        hideLoading()
//    }
    private fun updateStaff(list: ArrayList<ArrayList<String>>) {
        val validStaffList = list.filter { it[1] != "Select Staff" }
        val pendingTasks = AtomicInteger(validStaffList.size) // Track pending tasks

        fun processNext() {
            if (list.isNotEmpty()) {
                // Get the first element from the list
                val row = list.removeAt(0)
                val ride = row[0]
                val staff = row[1]

                // Process the current element
                fh.getDocumentFromName(staff) { staffObj ->
                    if (staffObj != null) {
                        val strippedRide = ride.replace(Regex("(Op|Att)\$"), "").trim()
                        val updateMap = mapOf("previousRide" to strippedRide)
                        db.collection("Staff").document(staffObj.Id).update(updateMap)
                            .addOnSuccessListener {
                                // Decrease the pending task count and process next task
                                if (pendingTasks.decrementAndGet() == 0) {
                                    hideLoading() // All tasks are complete, hide loading
                                } else {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    processNext() // Continue processing the next task
                                }
                            }
                            .addOnFailureListener {
                                // Handle failure if necessary
                                if (pendingTasks.decrementAndGet() == 0) {
                                    hideLoading() // All tasks are complete, hide loading
                                } else {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    processNext() // Continue despite failure
                                }
                            }
                    } else {
                        processNext() // Skip and process the next item
                    }
                }
            } else if (pendingTasks.decrementAndGet() == 0) {
                println("Task completed. Remaining tasks: $pendingTasks")
                hideLoading() // Call hideLoading when all tasks are complete
            }
        }

        // Start processing the list
        if (list.isNotEmpty()) {
            processNext()
        } else {
            hideLoading() // If the list is empty from the start
        }
    }


    private fun showLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.GONE
    }
}
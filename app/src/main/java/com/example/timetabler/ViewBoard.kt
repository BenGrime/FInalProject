package com.example.timetabler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

class ViewBoard : AppCompatActivity()
{

    var fh = FirebaseHandler()
    private val db = Firebase.firestore
    private lateinit var grid : GridLayout
    private lateinit var back : MaterialButton
    private lateinit var edit : MaterialButton
    private lateinit var title : TextView
    private lateinit var des : TextView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_board)
        showLoading()
        grid = findViewById(R.id.viewBoardGrid)
        back = findViewById(R.id.viewBack)
        edit = findViewById(R.id.viewBoardEdit)
        title = findViewById(R.id.viewBoardTitle)
        des = findViewById(R.id.viewBoardDes)
        fh.getBoard { b-> viewBoard(b) }
    }

    private fun viewBoard(board : ArrayList<Pair<String, String>>){
        back.setOnClickListener{finish()}
        edit.setOnClickListener{
            showLoading()
            grid.removeAllViews()
            editBoard(board)
        }
        title.text = "View Board"
        des.text = "The Current Board"
        back.text = "Back"
        edit.text = "Edit"
        for(pair in board)
        {
            val rideTextView = TextView(this).apply {
                text = pair.first
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@ViewBoard, R.color.black))
                setBackgroundColor(ContextCompat.getColor(this@ViewBoard, R.color.white))
                setPadding(16, 16, 16, 16) // Padding inside the TextView
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // Equal distribution
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(0, 1f) // Ride in first column
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED) // Dynamic row position
                    setMargins(8, 20, 8, 20) // Add margins between elements
                }
            }


            val staffTextView = TextView(this).apply {
                text = pair.second
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@ViewBoard, R.color.black))
                setBackgroundColor(ContextCompat.getColor(this@ViewBoard, R.color.white))
                setPadding(16, 16, 16, 16) // Padding inside the TextView
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0 // Equal distribution
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(1, 1f) // Ride in first column
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED) // Dynamic row position
                    setMargins(8, 20, 8, 20) // Add margins between elements
                }
            }
            grid.addView(rideTextView)
            grid.addView(staffTextView)
        }
        hideLoading()
    }

    private fun editBoard(board : ArrayList<Pair<String, String>>){
        fh.getAllStaff {staffList ->

            title.text = "Edit Board"
            des.text = "Edit The Current Board"
            back.text = "Cancel"
            edit.text = "Confirm"
            back.setOnClickListener {
                showLoading()
                grid.removeAllViews()
                viewBoard(board)
            }
            edit.setOnClickListener {
                showLoading()
                var newBoard : ArrayList<ArrayList<String>> = ArrayList()
                var tempList : ArrayList<String> = ArrayList()
                for (i in 0 until grid.childCount) {
                    val view = grid.getChildAt(i)


                    when (view) {
                        is TextView -> {
                            val text = view.text.toString()
                            tempList.add(text)
                        }
                        is Spinner -> {
                            val selectedItem = view.selectedItem.toString()
                            tempList.add(selectedItem)
                        }
                    }
                    if((i+1)%2 ==0){
                        newBoard.add(tempList)
                        tempList = ArrayList()
                    }

                }
               updateStaff(newBoard)
            }
            var trainedList = ArrayList<String>()
            for (pair in board) {

                val rideTextView = TextView(this).apply {
                    text = pair.first
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(this@ViewBoard, R.color.black))
                    setBackgroundColor(ContextCompat.getColor(this@ViewBoard, R.color.white))
                    setPadding(16, 16, 16, 16) // Padding inside the TextView
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0 // Equal distribution
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(0, 1f) // Occupy first column
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED) // Dynamic row position
                        setMargins(8, 20, 8, 20) // Add margins between elements
                    }
                }


                val staffSpinner = Spinner(this).apply {
                    setPadding(16, 16, 16, 16)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0 // Equal distribution
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(1, 1f) // Occupy second column
                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED) // Dynamic row position
                        setMargins(8, 20, 8, 20) // Add margins between elements
                        setBackgroundColor(ContextCompat.getColor(this@ViewBoard, R.color.white))
                    }

                    tag = pair.first
                }
                val strippedRide = when {
                    pair.first.endsWith(" Op") -> pair.first.removeSuffix(" Op")
                    pair.first.endsWith(" Att") -> pair.first.removeSuffix(" Att")
                    else -> pair.first
                }
                fh.getRideFromName(strippedRide) {
                    if (it != null) {
                        trainedList = it.staffTrained as ArrayList<String>
                        var list : ArrayList<String> = ArrayList()
                        list.add("Select Staff")
                        for(i in trainedList){
                            if(i != "Select Staff") {
                                list.add(
                                    when {
                                        i.endsWith(" Op") -> i.removeSuffix(" Op")
                                        i.endsWith(" Att") -> i.removeSuffix(" Att")
                                        else -> i
                                    }
                                )
                            }
                        }

                        staffSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }

                        val position = list.indexOf(pair.second)
                        if (position != -1) {
                            staffSpinner.setSelection(position) // Set the personOnRide as the selected item
                        }

                    }
                    else{

                        staffSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, staffList.map { it.Name }).apply { setDropDownViewResource(R.layout.spinner_custom_dropdown) }
                        val position = staffList.map { it.Name }.indexOf(pair.second)
                        if (position != -1) {
                            staffSpinner.setSelection(position) // Set the personOnRide as the selected item
                        }
                    }
                    grid.addView(rideTextView)
                    grid.addView(staffSpinner)
                }
            }
            hideLoading()
        }
    }

    private fun updateStaff(list: ArrayList<ArrayList<String>>)
    {
        val tempList = ArrayList(list)
        val validStaffList = list.filter { it[1] != "Select Staff" }
        val pendingTasks = AtomicInteger(validStaffList.size) // Track pending tasks

        fun processNext() {
            if (tempList.isNotEmpty()) {
                // Get the first element from the list
                val row = tempList.removeAt(0)
                val ride = row[0]
                val staff = row[1]

                // Process the current element
                fh.getDocumentFromName(staff) { staffObj ->
                    if (staffObj != null) {//if name is "Select Staff" it'll return null
                        val strippedRide = ride.replace(Regex("(Op|Att)\$"), "").trim()
                        val updateMap = mapOf("previousRide" to strippedRide)
                        db.collection("Staff").document(staffObj.Id).update(updateMap)
                            .addOnSuccessListener {
                                // Decrease the pending task count and process next task
                                if (pendingTasks.decrementAndGet() == 0)
                                {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    saving(list)
                                }
                                else
                                {
                                    println("Task completed. Remaining tasks: $pendingTasks")
                                    processNext() // Continue processing the next task
                                }
                            }
                            .addOnFailureListener {
                                // Handle failure if necessary
                                val tempList = ArrayList<String>()
                                tempList.add(staffObj.Name)
                                tempList.add(strippedRide)
                                if (pendingTasks.decrementAndGet() == 0) {
                                    // All tasks are complete, hide loading
                                    saving(list)
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
                saving(list)

            }
        }

        // Start processing the list
        if (tempList.isNotEmpty()) {
            processNext()
        } else {
            if(validStaffList.isNotEmpty()){
                saving(list)
            }
            else{
                val pairList: ArrayList<Pair<String, String>> = arrayListOf()

                for (innerList in list) {
                    if (innerList.size >= 2) { // Ensure there are at least 2 elements
                        pairList.add(Pair(innerList[0], innerList[1]))
                    }
                }
                viewBoard(pairList)
            }

        }
    }

    private fun saving(board : ArrayList<ArrayList<String>>)
    {
        println("Storing Board Now")
        //store board on firebase
        val firebaseBoard = board.map { innerList -> mapOf("ride" to innerList[0], "staff" to innerList[1]) } as ArrayList<Map<String, String>>
        firebaseBoard.forEach { row -> println("Row: $row, Type: ${row::class.simpleName}") }
        db.collection("Board").document("completeBoard").set(mapOf("Board" to firebaseBoard)).addOnSuccessListener{
            hideLoading() // Call hideLoading when all tasks are complete
            Toast.makeText(this, "Board Successfully saved", Toast.LENGTH_SHORT).show()

            //update any previous ride changes!!!!!

            val pairList: ArrayList<Pair<String, String>> = arrayListOf()

            for (innerList in board) {
                if (innerList.size >= 2) { // Ensure there are at least 2 elements
                    pairList.add(Pair(innerList[0], innerList[1]))
                }
            }
            viewBoard(pairList)
        }


    }

    private fun showLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.VISIBLE
    }

    private fun hideLoading() {
        findViewById<RelativeLayout>(R.id.loadingOverlay).visibility = View.GONE
    }
}
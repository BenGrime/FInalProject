package com.example.timetabler

import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class FirebaseHandler {

    private val db = FirebaseFirestore.getInstance()

    private val collectionStaff = db.collection("Staff")
    private val collectionRides = db.collection("Rides")
    private val inst = FirebaseFirestore.getInstance()

    fun getAllStaff(callback: (List<Staff>) -> Unit) {
        val staffList = mutableListOf<Staff>()
        db.collection("Staff").get().addOnSuccessListener{result ->

            for (staff in result){
                val Id = staff.getString("id")
                val Name = staff.getString("name")
                val prev = staff.getString("previousRide")
                val dob = staff.getTimestamp("doB")  ?: Timestamp.now()
                val ridesTrained = staff.get("ridesTrained") as? ArrayList<String> ?: emptyList()

                val s = Staff(
                    Id = Id.toString(),
                    Name = Name.toString(),
                    PreviousRide = prev.toString(),
                    DoB = dob,
                    RidesTrained = ArrayList(ridesTrained),
                    Category = when {
                        calculateAge(dob.toDate()) < 18 -> "Attendant"
                        calculateAge(dob.toDate()) in 18..20 -> "Fairground"
                        calculateAge(dob.toDate()) > 20 -> "SRO"
                        else -> ""
                    }
                )
                staffList.add(s)
            }
            callback(staffList)
        }
    }

    fun getAllRides(callback: (List<Ride>) -> Unit){
        val rideList = mutableListOf<Ride>()
        db.collection("Rides").get().addOnSuccessListener{result ->

            for (ride in result){
                val id = ride.getString("id")
                val name = ride.getString("name")
                val minAgeToOperate = ride.getLong("minAgeToOperate")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                val minAgeToAttend = ride.getLong("minAgeToAttend")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                val minNumAtt = ride.getLong("minNumAtt")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                val minNumOp = ride.getLong("minNumOp")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                val open = ride.getBoolean("open") ?: false // Retrieve as Boolean
                val prefNumAtt = ride.getLong("prefNumAtt")?.toInt() ?: 0// Safely parse string to Int
                val prefNumOp = ride.getLong("prefNumOp")?.toInt() ?: 0 // Safely parse string to Int
                val staffTrained = ride.get("staffTrained") as? ArrayList<String> ?: arrayListOf() // Retrieve list or default to empty

                val r = Ride(
                    Id = id.toString(),
                    Name = name.toString(),
                    minAgeToOperate = minAgeToOperate,
                    minAgeToAttend = minAgeToAttend,
                    minNumAtt = minNumAtt,
                    minNumOp = minNumOp,
                    open = open,
                    prefNumAtt = prefNumAtt,
                    prefNumOp = prefNumOp,
                    staffTrained = ArrayList(staffTrained)
                )
                rideList.add(r)
            }
            callback(rideList)
        }


    }

    fun getColectionSize(collection: String, callback: (Int) -> Unit){

        val db = FirebaseFirestore.getInstance()
        db.collection(collection).get()
            .addOnSuccessListener { result ->
                val collectionSize = result.size() // Gets the number of documents in the collection
                callback(collectionSize) // Pass the size to the callback
            }
            .addOnFailureListener { e ->
                callback(-1) // Pass -1 to indicate an error
            }
    }

    // Function to calculate age
    fun calculateAge(birthDate: Date): Int {
        val calendarNow = Calendar.getInstance()
        val calendarBirth = Calendar.getInstance()
        calendarBirth.time = birthDate

        var age = calendarNow.get(Calendar.YEAR) - calendarBirth.get(Calendar.YEAR)

        // If their birthday hasn't occurred yet this year, subtract 1 from age
        if (calendarNow.get(Calendar.DAY_OF_YEAR) < calendarBirth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    fun getDocumentFromName(name: String, callback: (Staff?) -> Unit){
        var s: Staff? = null
        db.collection("Staff").get().addOnSuccessListener(){result ->
            for (document in result) {
                if (document.getString("name") == name) {
                    // Found the matching document
                    val Id = document.getString("id")
                    val Name = document.getString("name")
                    val prev = document.getString("previousRide")
                    val dob = document.getTimestamp("doB")  ?: Timestamp.now()
                    val ridesTrained = document.get("ridesTrained") as? ArrayList<String> ?: emptyList()

                    s = Staff(
                        Id = Id.toString(),
                        Name = Name.toString(),
                        PreviousRide = prev.toString(),
                        DoB = dob,
                        RidesTrained = ArrayList(ridesTrained),
                        Category = when {
                            calculateAge(dob.toDate()) < 18 -> "Attendant"
                            calculateAge(dob.toDate()) in 18..20 -> "Fairground"
                            calculateAge(dob.toDate()) > 20 -> "SRO"
                            else -> ""
                        }
                    )
                    break
                }
            }
            callback(s)
        } .addOnFailureListener { e ->
            callback(null) // In case of error, pass null
        }
    }

    fun getRideFromName(name: String, callback: (Ride?) -> Unit){
        var r : Ride? = null
        db.collection("Rides").get().addOnSuccessListener(){result ->
            for (ride in result){
                if(ride.getString("name") == name)
                {
                    val id = ride.getString("id")
                    val name = ride.getString("name")
                    val minAgeToOperate = ride.getLong("minAgeToOperate")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                    val minAgeToAttend = ride.getLong("minAgeToAttend")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                    val minNumAtt = ride.getLong("minNumAtt")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                    val minNumOp = ride.getLong("minNumOp")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                    val open = ride.getBoolean("open") ?: false // Retrieve as Boolean
                    val prefNumAtt = ride.getLong("prefNumAtt")?.toInt() ?: 0// Safely parse string to Int
                    val prefNumOp = ride.getLong("prefNumOp")?.toInt() ?: 0 // Safely parse string to Int
                    val staffTrained = ride.get("staffTrained") as? ArrayList<String> ?: arrayListOf() // Retrieve list or default to empty

                    r = Ride(
                        Id = id.toString(),
                        Name = name.toString(),
                        minAgeToOperate = minAgeToOperate,
                        minAgeToAttend = minAgeToAttend,
                        minNumAtt = minNumAtt,
                        minNumOp = minNumOp,
                        open = open,
                        prefNumAtt = prefNumAtt,
                        prefNumOp = prefNumOp,
                        staffTrained = ArrayList(staffTrained)
                    )
                    break
                }

            }
            callback(r)

        }
    }

    fun missingDocNum(collection : String, callback: (Int?) -> Unit){
        db.collection(collection).get().addOnSuccessListener { result ->
            val numList = mutableListOf<Int>()
            for (document in result) {
                numList.add(document.id.toInt())
            }
            numList.sort()
            var missingNumber : Int
            if(numList.size == 0){
                missingNumber = 1
            }
            else{
                missingNumber = numList.size + 1 // Default to the next number after the end of the list
                for (i in 1..numList.size) { // Expected sequence: 1 to numList.size
                    if (i != numList[i - 1]) { // Compare expected number with actual number
                        missingNumber = i
                        break
                    }
                }
            }
            callback(missingNumber)
        }
    }

    fun getSelectedStaffObjs(selected : ArrayList<String>, callback: (ArrayList<Staff>) -> Unit){
        db.collection("Staff").get().addOnSuccessListener(OnSuccessListener {
            val staffList = ArrayList<Staff>()
            db.collection("Staff").get().addOnSuccessListener{result ->

                for (staff in result)
                {
                    for(name in selected)
                    {
                        if(staff.getString("name").equals(name)) {
                            val Id = staff.getString("id")
                            val Name = staff.getString("name")
                            val prev = staff.getString("previousRide")
                            val dob = staff.getTimestamp("doB") ?: Timestamp.now()
                            val ridesTrained =
                                staff.get("ridesTrained") as? ArrayList<String> ?: emptyList()

                            val s = Staff(
                                Id = Id.toString(),
                                Name = Name.toString(),
                                PreviousRide = prev.toString(),
                                DoB = dob,
                                RidesTrained = ArrayList(ridesTrained),
                                Category = when {
                                    calculateAge(dob.toDate()) < 18 -> "Attendant"
                                    calculateAge(dob.toDate()) in 18..20 -> "Fairground"
                                    calculateAge(dob.toDate()) > 20 -> "SRO"
                                    else -> ""
                                }
                            )
                            staffList.add(s)
                        }
                    }

                }
                callback(staffList)
            }
        })
    }

    fun getPriority(callback: (ArrayList<Pair<String ,Int>>) -> Unit){
        val priorityList = ArrayList<Pair<String, Int>>()
        db.collection("Settings").document("RidePriority").get().addOnSuccessListener { document ->
            if (document != null) {
                // Log the document data to verify the contents
                Log.d("getPriority", "Document data: ${document.data}")

                // Correct the field name to "priortiyList"
                val data = document.data?.get("priorityList") as? List<Map<String, Any>>  // Access the correct field

                // Log the priorityList to check its content
                Log.d("getPriority", "PriorityList: $data")

                if (data != null) {
                    // Loop through the list and extract the pairs
                    data.forEach { pair ->
                        // Extract the values
                        val rideName = pair["ride"] as? String  // Extract "ride" value
                        val value = pair["value"] as? Long     // Extract "value" (using Long for Firestore's number type)

                        // Add the pair (String, Int) to the list if both values exist
                        if (rideName != null && value != null) {
                            priorityList.add(Pair(rideName, value.toInt()))
                        }
                    }
                }
            }

            // Return the list of pairs via the callback
            callback(priorityList)
        }

    }
    fun getBoard(callback: (ArrayList<Pair<String ,String>>) -> Unit){
        val board = ArrayList<Pair<String, String>>()
        db.collection("Board").document("completeBoard").get().addOnSuccessListener { document ->
            if (document != null) {
                // Log the document data to verify the contents

                // Correct the field name to "priortiyList"
                val data = document.data?.get("Board") as? List<Map<String, Any>>  // Access the correct field


                if (data != null) {
                    // Loop through the list and extract the pairs
                    data.forEach { pair ->
                        // Extract the values
                        val rideName = pair["ride"] as? String  // Extract "ride" value
                        val staffName = pair["staff"] as? String     // Extract "value" (using Long for Firestore's number type)

                        // Add the pair (String, Int) to the list if both values exist
                        if (rideName != null && staffName != null) {
                            board.add(Pair(rideName, staffName))
                        }
                    }
                }
            }

            // Return the list of pairs via the callback
            callback(board)
        }

    }

    fun getManager(id : String, callback: (Manager) -> Unit){
        db.collection("Managers").document(id).get().addOnSuccessListener{
            val m = it.get("accessLevel").toString().toIntOrNull()
                ?.let { it1 -> Manager(it1, it.get("name").toString()) }

            if (m != null) {
                callback(m)
            }
        }
    }

    fun getManagers(callback: (ArrayList<Manager>) -> Unit){
        db.collection("Managers").get().addOnSuccessListener{
            var list = ArrayList<Manager>()
            for(m in it){
                val accessLevel = m.getLong("accessLevel")
                val name = m.getString("name")

                val manager = Manager(
                    accessLevel = accessLevel!!.toInt(),
                    name = name.toString())

                list.add(manager)
            }
            callback(list)

        }

    }

    fun doesManagerExist(id : String, callback: (Boolean) -> Unit){
        db.collection("Managers").get().addOnSuccessListener{
            for(m in it){
                if(m.id == id)
                {
                    callback(true)
                    return@addOnSuccessListener // Exit early after finding a match
                }
            }
            callback(false)
        }
    }

    fun getEvalPoints(callback: (ArrayList<Pair<String ,Int>>) -> Unit){
        var evalList = ArrayList<Pair<String, Int>>()
        db.collection("Settings").document("EvaluationPoints").get().addOnSuccessListener{document ->
            if (document != null) {
                // Correct the field name to "priortiyList"
                val data = document.data?.get("evalList") as? List<Map<String, Any>>  // Access the correct field

                if (data != null) {
                    // Loop through the list and extract the pairs
                    data.forEach { pair ->
                        // Extract the values
                        val name = pair["first"] as? String  // Extract "ride" value
                        val value = pair["second"] as? Long     // Extract "value" (using Long for Firestore's number type)

                        // Add the pair (String, Int) to the list if both values exist
                        if (name != null && value != null) {
                            evalList.add(Pair(name, value.toInt()))
                        }
                    }
                }
            }
            callback(evalList)
        }
    }

    fun getPreset(callback : (String) -> Unit){
        db.collection("Settings").document("chosenPreset").get().addOnSuccessListener{
            callback(it.get("preset").toString())
        }
    }

}

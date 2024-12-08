package com.example.timetabler

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
                val prev = staff.getString("previous ride")
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
                    val prev = document.getString("previous ride")
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
                    val minAgeToOperate = ride.getLong("minAgeToOp")?.toInt() ?: 0 // Retrieve as Long and convert to Int
                    val minAgeToAttend = ride.getLong("minAgeToOp")?.toInt() ?: 0 // Retrieve as Long and convert to Int
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
                            val prev = staff.getString("previous ride")
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
}

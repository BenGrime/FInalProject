package com.example.timetabler

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHandler {

    private val db = FirebaseFirestore.getInstance()

    private val collectionStaff = db.collection("Staff")
    private val collectionRides = db.collection("Rides")
    private val inst = FirebaseFirestore.getInstance()

    fun getAllStaff(callback: (List<Staff>) -> Unit) {
        val staffList = mutableListOf<Staff>()
        db.collection("Staff").get().addOnSuccessListener{result ->

            for (staff in result){
                val Id = staff.getString("Id")
                val Name = staff.getString("Name")
                val prev = staff.getString("Previous Ride")
                val dob = staff.getTimestamp("DoB")  ?: Timestamp.now()
                val ridesTrained = staff.get("RidesTrained") as? ArrayList<String> ?: emptyList()

                val s = Staff(
                    Id = Id.toString(),
                    Name = Name.toString(),
                    PreviousRide = prev.toString(),
                    DoB = dob,
                    RidesTrained = ArrayList(ridesTrained)
                )
                staffList.add(s)
            }
            callback(staffList)
        }
    }
}

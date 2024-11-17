package com.example.timetabler

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

    // Function to calculate age
    private fun calculateAge(birthDate: Date): Int {
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
}

package com.example.timetabler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.github.javafaker.Faker
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import kotlin.random.Random

class Notifications_Page : AppCompatActivity() {

    private var fh = FirebaseHandler()
    private lateinit var generateDatabase : Button
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_page)

        generateDatabase = findViewById(R.id.generateDatabaseButton)
        generateDatabase.setOnClickListener(View.OnClickListener {
//            val numberOfStaff = 1 // Number of staff to generate
//            generateRandomStaff(numberOfStaff)
            assignStaffToRidesInBulk()

        })
    }

    fun assignStaffToRidesInBulk() {
        val rideList = mutableListOf<Ride>()
        val staffList = mutableListOf<Staff>()

        // Fetch all rides and staff from the database
        fh.getAllRides { rides ->
            rideList.addAll(rides)

            fh.getAllStaff { staff ->
                staffList.addAll(staff)

                // Loop through all rides
                for (rideinList in rideList) {
                    // Get a random number of staff (between 10 and 20)
                    val staffCount = Random.nextInt(10, 21) // Generates a number between 10 and 20
                    val selectedStaff = staffList.shuffled().take(1) // Randomly select 'staffCount' number of staff

                    // Loop through selected staff and check if they meet the requirements
                    for (staff in selectedStaff) {
                        //val staffAge = calculateAge(staff.DoB) // Get staff's age

                        fh.getDocumentFromName(staff.Name){staff ->
                            if(staff!=null){
                                val updatedRidesList: MutableList<Any> = staff.RidesTrained.toMutableList()
                                var foundRide : Ride? = null
                                fh.getAllRides{ride ->
                                    for(r in ride){
                                        if(r.Name == rideinList.Name){
                                            foundRide = r
                                            break
                                        }
                                    }

                                    //at this point you have a Staff and Ride object

                                    if(foundRide!!.minAgeToOperate <= calculateAge(staff.DoB))
                                    {
                                        var alreadyTrained = false
                                        for(r in staff.RidesTrained){
                                            if(r.equals(rideinList.Name + " Op"))
                                            {
                                                alreadyTrained = true
                                            }

                                        }
                                        if(!alreadyTrained) {
                                            updatedRidesList.add(rideinList.Name + " Op")

                                            val currentMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesList)
                                            db.collection("Staff").document(staff.Id).update(currentMap).addOnSuccessListener {//update staff list
                                                fh.getRideFromName(rideinList.Name){//get document from ride name
                                                    if(it!=null) {
                                                        var updatedStaffTrained: MutableList<Any> = it.staffTrained.toMutableList()
                                                        updatedStaffTrained.add(selectedStaff + " Op")
                                                        val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)//create the map to update
                                                        db.collection("Rides").document(it.Id.toString()).update(currentRideMap).addOnSuccessListener {
                                                            //Toast.makeText(this@Notifications_Page, "Added $selectedStaff to $rideinList.Name", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }

                                                }

                                            }
                                        }
                                        else
                                        {
                                            //Toast.makeText(this@Notifications_Page, "$selectedStaff is already trained on $selectedRide", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else
                                    {
                                        if(foundRide!!.minAgeToAttend <= calculateAge(staff.DoB))
                                        {
                                            var alreadyTrained = false
                                            for(r in staff.RidesTrained){
                                                if(r.equals(rideinList.Name + " Att"))
                                                {
                                                    alreadyTrained = true
                                                }

                                            }
                                            if(!alreadyTrained) {
                                                updatedRidesList.add(rideinList.Name + " Att")

                                                val currentMap = hashMapOf<String, Any>("ridesTrained" to updatedRidesList)
                                                db.collection("Staff").document(staff.Id).update(currentMap).addOnSuccessListener {//update staff list
                                                    fh.getRideFromName(rideinList.Name){//get document from ride name
                                                        if(it!=null) {
                                                            var updatedStaffTrained: MutableList<Any> = it.staffTrained.toMutableList()
                                                            updatedStaffTrained.add(selectedStaff + " Att")
                                                            val currentRideMap = hashMapOf<String, Any>("staffTrained" to updatedStaffTrained)//create the map to update
                                                            db.collection("Rides").document(it.Id.toString()).update(currentRideMap).addOnSuccessListener {
//                                                                Toast.makeText(this@Notifications_Page, "Added $selectedStaff to $rideinList.Name", Toast.LENGTH_SHORT).show()
//                                                                dialog.dismiss()
                                                            }
                                                        }

                                                    }

                                                }
                                            }
                                            else{
                                                Toast.makeText(this@Notifications_Page, "$selectedStaff is already trained on $rideinList.Name", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        else
                                        {
                                            Toast.makeText(this@Notifications_Page, "$selectedStaff is not old enough to run $rideinList.Name", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }



    fun calculateAge(birthDate: Timestamp): Int {
        // Get the current date as a Calendar instance
        val calendarNow = Calendar.getInstance()

        // Convert the Firebase Timestamp to a Date and set it in a Calendar instance
        val calendarBirth = Calendar.getInstance()
        calendarBirth.time = birthDate.toDate() // Convert Timestamp to Date

        // Calculate the age
        var age = calendarNow.get(Calendar.YEAR) - calendarBirth.get(Calendar.YEAR)

        // If their birthday hasn't occurred yet this year, subtract 1 from age
        if (calendarNow.get(Calendar.DAY_OF_YEAR) < calendarBirth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }
    private fun generateRandomStaff(count: Int) {
        val faker = Faker() // For generating random names
        val today = Calendar.getInstance()

        for (i in 1..count) {
            val firstName = faker.name().firstName() // Generate a random first name
            val lastName = faker.name().lastName() // Generate a random last name
            val name = "$firstName $lastName" // Combine first name and last name

            // Generate a random DoB making the age 16 or 17
            val randomAge = Random.nextInt(16, 18) // Either 16 or 17
            val randomYear = Calendar.getInstance().get(Calendar.YEAR) - randomAge // Get the year minus 16 or 17
            val randomMonth = Random.nextInt(1, 13) // Random month (1 to 12)
            val randomDay = Random.nextInt(1, 28) // Random day (1 to 28 to ensure valid date)

            val dateCustom = "$randomDay/$randomMonth/$randomYear"
            val dobTimestamp = convertStrToTime(dateCustom)

            // Use the existing staff creation logic
            fh.getColectionSize("Staff") {
                fh.missingDocNum("Staff") { staffId ->
                    val staff = Staff(
                        Id = staffId.toString(),
                        Name = name,
                        PreviousRide = "",
                        DoB = dobTimestamp,
                        RidesTrained = ArrayList<String>(),
                        Category = "Attendant" // Since they are 16-17, the category is fixed
                    )

                    // Add staff to Firebase
                    db.collection("Staff").document(staff.Id).set(staff).addOnSuccessListener {

                    }.addOnFailureListener {

                    }
                }
            }
        }
    }
    fun convertStrToTime(dateStr : String) : Timestamp{
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val date = formatter.parse(dateStr)
        return Timestamp(date) // Returns a Firebase Timestamp object

    }
}
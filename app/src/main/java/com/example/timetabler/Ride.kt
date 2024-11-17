package com.example.timetabler

import com.google.firebase.Timestamp
import java.io.Serializable

data class Ride(
    var Id: String,
    var Name: String,
    var minAgeToOperate : Int, //minimum age for operator to be, 16, 18, 21
    var minNumAtt : Int, //minimum number of attendants - could be 1, so 1 attendant of the age of attribute above. could be more
    var minNumOp : Int, //minimum number of operators - could be 1, so 1 operator of the age of attribute above. could be more
    var open : Boolean, //is the ride open or closed
    var prefNumAtt : Int, //whats the preferred number of attendants on this ride
    var prefNumOp : Int, //whats the preferred number of operators on this ride
    var StaffTrained: ArrayList<*> //list of staff trained on ride

) : Serializable
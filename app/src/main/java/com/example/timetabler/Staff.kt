package com.example.timetabler

import com.google.firebase.Timestamp
import java.io.Serializable

data class Staff(
    var Id: String = "",
    var Name: String = "",
    var PreviousRide: String = "",
    var DoB: Timestamp,
    var RidesTrained: ArrayList<*>,
    var Category: String  = ""//attendant, Fairground or SRO
) : Serializable
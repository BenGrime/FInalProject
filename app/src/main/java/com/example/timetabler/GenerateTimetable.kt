package com.example.timetabler

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class GenerateTimetable {


    private fun setRequirements(map : HashMap<String, String>): Boolean {
        for ((tag, selectedStaff) in map) {
            if(selectedStaff != "Select Staff")
            {
                return true
            }
        }
        return false
    }

    fun timetable1(map : HashMap<String, String> , callback: (HashMap<String, String>) -> Unit)
    {
        var anyRequirements = setRequirements(map) // are there any requirements
        
    }


}

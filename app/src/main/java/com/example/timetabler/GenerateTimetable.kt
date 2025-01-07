package com.example.timetabler

import kotlin.random.Random

class GenerateTimetable {

    var fh = FirebaseHandler()
    private fun anyRequirements(list : ArrayList<ArrayList<String>>): Boolean {
        list.forEach{row ->
            val staff = row[1]

            if(staff != "Select Staff")
            {
                return true
            }
        }
        return false

    }

    fun getRandomNumber(staffSelected: ArrayList<String>): Int {
        if (staffSelected.isEmpty()) {
            throw IllegalArgumentException("The completeBoard cannot be empty.")
        }
        if(staffSelected.size == 1){
            return 0
        }
        return Random.nextInt(0, staffSelected.size -1)
    }

    fun getRandomStaff(staffSelected: ArrayList<String>, currentRide: String, staffObjList : ArrayList<Staff>, rides : ArrayList<Ride>) : String {
        var randomStaff: String? = null


        do {
            val staffForRide = ArrayList<Staff>()
            for(r in staffObjList)
            {
                for(i in r.RidesTrained)
                {
                    if(i.toString().contains(currentRide)){
                        staffForRide.add(r)
                    }
                }
            }
            if(staffForRide.size >= 1) {


                val staffNames = staffForRide.map { it.Name }
                val randomIndex = getRandomNumber(ArrayList(staffNames))
                randomStaff = staffNames[randomIndex]

                for (s in staffForRide) {
                    if (s.Name == randomStaff) {

                        if (s.PreviousRide != currentRide)//were they on it before
                        {
                            for (r in s.RidesTrained)//
                            {
                                if (r.toString().contains(currentRide))//are they trained as Op or Att
                                {
                                    //we only want the ops on the operate rides and the Atts on rides where its an attendant labelled
                                    if (r.toString().contains("Op")
                                    )//are the extensions Op or Att the same
                                    {
                                        return s.Name
                                    } else if (r.toString().contains("Att")) {
                                        return s.Name
                                    }
                                }
                            }
                            randomStaff = null // Retry if the staff is invalid
                        } else {
                            randomStaff = null // Retry if the staff is invalid
                        }
                    }
                }
            }

        } while (randomStaff == null  && staffForRide.size != 0)

        return randomStaff ?: ""
    }

    fun timetable1(list : ArrayList<ArrayList<String>>, staffSelected : ArrayList<String> , staffObjList : ArrayList<Staff>, rides : ArrayList<Ride>, callback: (ArrayList<ArrayList<String>>) -> Unit) //THIS WILL FOCUS ON .......
    {
        if(anyRequirements(list))
        {
            list.forEach{row ->
                val staff = row[1]

                if(staff != "Select Staff")
                {
                    staffSelected.remove(staff)
                }
            }
        }
        val newStaffObjList = ArrayList<Staff>()
        for(s in staffObjList)
        {
            for(n in staffSelected){
                if(s.Name == n)
                {
                    newStaffObjList.add(s)
                }
            }
        }
        val assignedStaff = mutableSetOf<String>()
        // Create an ArrayList of ArrayLists to represent the 2D structure
        val completeBoard = ArrayList<ArrayList<String>>()
        //create copy for the full board
        list.forEach { row ->
            val ride = row[0]
            val staff = row[1]
            completeBoard.add(arrayListOf(ride, staff))

        }

        completeBoard.forEachIndexed { index, row ->
            val ride = row[0]
            val staff = row[1]
            if (staff == "Select Staff")//check its already been set, if it has go to next one
            {
                if(staffSelected.size != 0 || newStaffObjList.size != 0)
                {
                    var staffName = getRandomStaff(staffSelected, ride, newStaffObjList, rides)
                    if(staffName != "") {
                        while (assignedStaff.contains(staffName)) {
                            staffName = getRandomStaff(staffSelected, ride, newStaffObjList, rides)

                        }
                        completeBoard[index][1] = staffName
                        assignedStaff.add(staffName)
                        staffSelected.remove(staffName)
                        newStaffObjList.removeIf { it.Name == staffName }
                    }
                }

            }
        }

        //deal with missing values and spares.
        var spareStaff : ArrayList<String> = ArrayList()
        for (name in staffSelected) {
            if (!assignedStaff.contains(name)) {
                spareStaff.add(name)
            }
        }
        var unassignedRides : ArrayList<String> = ArrayList()
        completeBoard.forEach { row ->
            val ride = row[0]
            val staff = row[1]
            if(staff.equals("Select Staff"))
            {
                unassignedRides.add(ride)
            }
        }

        if(unassignedRides.size != 0)//BE AWARE OF STAFF SHORTAGES
        {
            //assign to car park
            //currently no one is assigned to car park since they arent trained
            //so, lets say we have a limit of 8 for Ops + SRO's, 5 for attendants.

            //check the spare are trained on that amount

            //if they are assign them
            //if not, swap them RANDOMLY with a RANDOM ride they are trained on, and see if that person can do car park
            //if they can, swap them, if they cant find someone else
        }
        if(spareStaff.size != 0)
        {
            //deal with them
        }

        //all sorted
        //no spare staff, no rides unassigned that im not ok with

        //now check the board is ok for 1 final time


        //when it gets to the end of the rows
        completeBoard.forEach { row ->
            val ride = row[0]
            val staff = row[1]
            println("Ride: "+ride +" ,Staff: "+staff)
        }
        callback(completeBoard)
    }
}

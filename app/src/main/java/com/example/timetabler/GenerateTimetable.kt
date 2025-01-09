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
        val staffForRide = ArrayList<Staff>()

        // Filter staff trained for the current ride
        for (r in staffObjList) {
            for (i in r.RidesTrained) {
                if (i.toString().contains(currentRide)) {
                    staffForRide.add(r)
                }
            }
        }

        while (staffForRide.isNotEmpty()) {
            val randomIndex = getRandomNumber(ArrayList(staffForRide.map { it.Name }))
            val randomStaff = staffForRide[randomIndex]

            // Check if the selected staff meets the conditions
            if (!currentRide.contains(randomStaff.PreviousRide) || randomStaff.PreviousRide == "")
            { // Were they on it before?
                for (r in randomStaff.RidesTrained)
                {
                    if (r.toString().contains(currentRide))
                    { // Are they trained for the ride?
                        if (r.toString().contains("Op"))
                        { // Check if it's an "Op"
                            return randomStaff.Name
                        }
                        else if (r.toString().contains("Att"))
                        { // Check if it's an "Att"
                            val strippedRide = currentRide.replace(Regex("(Op|Att)\$"), "").trim()
                            if (!randomStaff.RidesTrained.any { it.toString().contains(strippedRide) && it.toString().contains("Op") })
                            {
                                return randomStaff.Name
                            }
                        }
                    }
                }
            }
            // If the staff member doesn't meet the conditions, remove them
            staffForRide.removeAt(randomIndex)
        }
        // Return an empty string if no valid staff member is found
        return ""
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
            if(spareStaff.size == 0)//i did not run out of staff - meaning no spare staff WITH spare rides
            {
                //start taking people off rides we can afford. but using the minimum op/att number

                //how many op rides left

                //how many att rides left

                //EXCLUDE CAR PARK FROM THE 2 ABOVE


            }
            else//spare rides and staff - go and check and assign
            {
                completeBoard.forEach{row ->
                    val ride = row[0]
                    val staff = row[1]

                    if(staff == "Select Staff" && ride != "Car park")
                    {
                        //are there any rides that haven't been assigned

                        //INVOLVES MOVING STAFF AROUND
                    }
                }
                completeBoard.forEachIndexed{index, row ->
                    val ride = row[0]
                    val staff = row[1]

                    if(ride == "Car Park" && staffSelected.size > 0)
                    {
                        //are there any rides that havent been assigned
                        completeBoard[index][1] = staffSelected[0]
                        assignedStaff.add(staffSelected[0])
                        newStaffObjList.removeIf { it.Name == staffSelected[0] }
                        staffSelected.remove(staffSelected[0])
                    }
                }

            }


        }
        if(spareStaff.size != 0)
        {
            //deal with the spares
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

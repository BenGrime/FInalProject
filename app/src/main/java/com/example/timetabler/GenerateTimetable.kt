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
        //The first go at setting as many rides as possible
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

        //get missing values and spares if any
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
            if(staff == "Select Staff")
            {
                unassignedRides.add(ride)
            }
        }



        //deal with them
        if (unassignedRides.size != 0)//BE AWARE OF STAFF SHORTAGES
        {
            if (spareStaff.size == 0)
            {
                //STAFF SHORTAGE
            }
            else//spare rides and staff - go and check and assign
            {
                //use the list of unassigned rides
                for(u in unassignedRides)
                {
                    if(u != "Car Park") //as no one is trained skip
                    {
                        var ride = u
                        var staffTrained: ArrayList<String> = ArrayList()
                        var role = "Op" // Default value
                        if (u.contains("Op") || u.contains("Att")) {
                            // Remove "Op" or "Att" from the string and set the role
                            role = u.split(" ").last { it == "Op" || it == "Att" }
                            ride = u.replace(Regex("(Op|Att)\$"), "").trim()
                        }
                        for (r in rides) {
                            if (r.Name == ride) {
                                for (s in r.staffTrained) {
                                    if (s.toString().contains(role)) {
                                        staffTrained.add(s.toString())
                                    }
                                }

                            }
                        }
                        //now I have a list of staff that are trained on the unassigned ride


                        staffTrained.shuffle()//to make this random
                        completeBoard.forEachIndexed() { index, row ->

                            val ride = row[0]
                            val staff = row[1]

                            if (staff == staffTrained[0])//is this staff trained on the unassigned ride?
                            {
                                //is the spare trained on the ride
                                for (s in spareStaff) {
                                    for (a in staffObjList) {
                                        if (s == a.Name)//get the object of that SPARE staff name
                                        {
                                            for (t in a.RidesTrained) {
                                                if (t == ride) {
                                                    //THE SPARE IS TRAINED ON THE RIDE AND THE STAFF ON THIS RIDE IS TRAINED ON THE UNASSIGNED ONE

                                                    //swap them
                                                    completeBoard[index][1] = s

                                                    // Assign the current staff to the unassigned ride
                                                    val unassignedIndex =
                                                        completeBoard.indexOfFirst { it[0] == u }
                                                    if (unassignedIndex != -1) {
                                                        completeBoard[unassignedIndex][1] = staff
                                                    }

                                                    // Update spareStaff and assignedStaff lists
                                                    spareStaff.remove(s)
                                                    assignedStaff.add(s)
                                                    staffSelected.remove(s)
                                                    newStaffObjList.removeIf { it.Name == s }

                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }



                //put the rest on car park
                completeBoard.forEachIndexed { index, row ->
                    val ride = row[0]
                    val staff = row[1]

                    if (ride == "Car Park" && spareStaff.size > 0) {
                        //are there any rides that havent been assigned
                        completeBoard[index][1] = spareStaff[0]
                        assignedStaff.add(spareStaff[0])
                        newStaffObjList.removeIf { it.Name == spareStaff[0] }
                        staffSelected.remove(spareStaff[0])
                        spareStaff.remove(spareStaff[0])
                    }
                }

            }


        }
        if (spareStaff.size != 0) {
            //deal with the spares
        }

        //all sorted
        //no spare staff, no rides unassigned that im not ok with

        //now check the board is ok for 1 final time


        //when it gets to the end of the rows
        completeBoard.forEach { row ->
            val ride = row[0]
            val staff = row[1]
            println("Ride: " + ride + " ,Staff: " + staff)
        }
        callback(completeBoard)

    }
}

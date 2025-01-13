package com.example.timetabler

import kotlin.math.min
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
        var completeBoard = ArrayList<ArrayList<String>>()
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

        completeBoard.forEach { row ->
            val ride = row[0]
            val staff = row[1]
            println("Ride: " + ride + " ,Staff: " + staff)
        }
        println("AFTER SWAPS:")



        //deal with them
        if (unassignedRides.size != 0)//BE AWARE OF STAFF SHORTAGES
        {
            if (spareStaff.size == 0)
            {
                //create short board based on complete board minus a few rows if staff was on a removed row, add to spare staff list

                var shortBoard = ArrayList<ArrayList<String>>()
                for(r in rides)
                {
                    var row = ArrayList<String>()
                    var iterator = 1
                    if(r.minNumAtt + r.minNumOp > 1)
                    {
                        while(iterator <= r.minNumOp)
                        {
                            row.add(r.Name + " Op")
                            row.add("Select Staff")
                            shortBoard.add(row)
                            row = ArrayList()
                            iterator++
                        }
                        iterator = 1
                        while(iterator <= r.minNumAtt)
                        {
                            row.add(r.Name + " Att")
                            row.add("Select Staff")
                            shortBoard.add(row)
                            row = ArrayList()
                            iterator++
                        }
                    }
                    else
                    {
                        row.add(r.Name)
                        row.add("Select Staff")
                        shortBoard.add(row)
                    }
                }
                //short board done, with empty staff
                var skipped = 0
                completeBoard.forEachIndexed{index, row ->
                    var ride = row[0]
                    var staff = row[1]
                    if(index - skipped < shortBoard.size)
                    {
                        if (shortBoard[index - skipped][1] == "Select Staff" && ride == shortBoard[index - skipped][0]) {
                            shortBoard[index - skipped][1] = staff
                        } else {
                            if(shortBoard[index - skipped][1] != "Select Staff")
                            {
                                spareStaff.add(staff)
                            }
                            skipped++
                        }
                    }
                }
                completeBoard = shortBoard//override it
            }


            if(spareStaff.size != 0)//spare rides and staff - go and check and assign
            {
                //use the list of unassigned rides
                for(u in unassignedRides)
                {
                    if (u != "Car Park") //as no one is trained skip
                    {
                        var rideSpare = u
                        var staffTrained: ArrayList<String> = ArrayList()
                        var role = "Op" // Default value
                        if (u.contains("Op") || u.contains("Att")) {
                            // Remove "Op" or "Att" from the string and set the role
                            role = u.split(" ").last { it == "Op" || it == "Att" }
                            rideSpare = u.replace(Regex("(Op|Att)\$"), "").trim()
                        }
                        for (r in rides) {
                            if (r.Name == rideSpare) {
                                for (s in r.staffTrained) {
                                    if (s.toString().contains(role)) {
                                        staffTrained.add(s.toString())
                                    }
                                }

                            }
                        }
                        staffTrained.shuffle()
                        spareStaff.shuffle()

                        //filter spare list, so only ones that matches rhe role are in the list
                        //spareStaff.filter { it.endsWith(role) }
                        var staffChosen: Staff? = null
                        var rideObj : Ride? = null
                        for(sol in staffObjList)
                        {
                            staffChosen = sol
                        }
                        for(r in rides)
                        {
                            if(r.Name == u)
                            {
                                rideObj = r
                            }
                        }
                        if(staffChosen!=null && rideObj!=null)
                        {
                            var minAge = when (staffChosen.Category) {
                                "Attendant" -> {
                                    16
                                }
                                "Fairground" -> {
                                    18
                                }
                                else -> {
                                    21
                                }
                            }
                            if(role == "Op")
                            {
                                if(rideObj.minAgeToOperate <= minAge)
                                {
                                    findAndSwapStaff(staffChosen.Name, u, completeBoard, staffTrained, rides, staffObjList)
                                    spareStaff.removeAt(0)//remove them after they have been added
                                }
                            }
                            else
                            {
                                if((minAge == 18 || minAge == 21) && rideObj.minAgeToAttend > 16)
                                {
                                    findAndSwapStaff(staffChosen.Name, u, completeBoard, staffTrained, rides, staffObjList)
                                    spareStaff.removeAt(0)//remove them after they have been added
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
                        unassignedRides.remove(unassignedRides[0])
                    }
                }

            }


        }
        if (spareStaff.size != 0) {
            //deal with the spares

            //make more car park slots and assign
        }

        //all sorted
        //no spare staff, no rides unassigned that im not ok with

        //now check the board is ok for 1 final time???


        //when it gets to the end of the rows
        completeBoard.forEach { row ->
            val ride = row[0]
            val staff = row[1]
            println("Ride: " + ride + " ,Staff: " + staff)
        }
        callback(completeBoard)

    }

    fun findAndSwapStaff(
        spare: String,
        unassignedRide: String,
        completedBoard: ArrayList<ArrayList<String>>,
        staffTrained: ArrayList<String>,
        rides: ArrayList<Ride>,
        staffObjList: ArrayList<Staff>
    ): Boolean {
        val stack = mutableListOf<Pair<String, String>>() // Stack to hold (staff, rideToAssign)
        val visited = mutableSetOf<Pair<String, String>>() // Set to track visited (staff, ride) pairs
        stack.add(Pair(spare, unassignedRide)) // Start with the spare and unassigned ride

        while (stack.isNotEmpty()) {
            val (currentSpare, currentUnassignedRide) = stack.removeAt(stack.lastIndex) // Pop the stack

            if (visited.contains(Pair(currentSpare, currentUnassignedRide))) {
                continue // Skip if already processed
            }
            visited.add(Pair(currentSpare, currentUnassignedRide)) // Mark this pair as visited

            // Find a ride the currentSpare is trained on that has staff assigned
            val trainedRide = findTrainedRideWithStaff(currentSpare, completedBoard, staffTrained, rides, staffObjList)
            if (trainedRide != null) {
                for (staffA in trainedRide.subList(1, trainedRide.size)) { // Skip the first element (ride name)
                    if (isTrainedFor(staffA, currentUnassignedRide, staffTrained, rides, staffObjList)) {
                        // Perform swaps
                        assignStaffToRide(staffA, currentUnassignedRide, completedBoard) // Staff A -> unassigned ride
                        assignStaffToRide(currentSpare, trainedRide[0], completedBoard) // Spare -> trained ride

                        // Process remaining stack to finish assignment
                        workBackThroughStack(stack, completedBoard, staffObjList)
                        return true
                    } else {
                        // Add staffA and the ride we want to assign them to into the stack if not already visited
                        val possibleRide = findTrainedRideWithStaff(staffA, completedBoard, staffTrained, rides, staffObjList)
                        if (possibleRide != null && !visited.contains(Pair(staffA, possibleRide[0]))) {
                            stack.add(Pair(staffA, possibleRide[0]))
                        }
                    }
                }
            }
        }
        return false // If no valid swaps are found
    }

    fun workBackThroughStack(stack: MutableList<Pair<String, String>>, completedBoard: ArrayList<ArrayList<String>>, staffObjList : ArrayList<Staff>) {
        while (stack.isNotEmpty()) {
            val (staff, ride) = stack.removeAt(stack.lastIndex) // Pop the stack
            assignStaffToRide(staff, ride, completedBoard) // Assign staff to their respective rides
        }
    }

    // Find a ride the staff member is trained for that has staff assigned
    fun findTrainedRideWithStaff(staff: String, completedBoard: ArrayList<ArrayList<String>>, staffTrained : ArrayList<String>, rides : ArrayList<Ride>, staffObjList : ArrayList<Staff>): List<String>? {
        for (row in completedBoard) {
            if (isTrainedFor(staff, row[0], staffTrained, rides, staffObjList) && row.size > 1) { // Ensure there are staff assigned

                return row
            }
        }
        return null
    }

    // Check if a staff member is trained for a specific ride
    fun isTrainedFor(staff: String, ride: String, staffTrained : ArrayList<String>, rides : ArrayList<Ride>, staffObjList : ArrayList<Staff>): Boolean {
       for(r in rides)
       {
            if(ride.contains(r.Name))
            {
                for(s in r.staffTrained)
                {
                    if (s.toString().contains(staff))//are they trained as Op OR Att
                    {
                       for(sol in staffObjList)
                       {
                           if(sol.Name == staff)
                           {
                               if(sol.Category == "SRO" || sol.Category == "Fairground")
                               {
                                   return r.minAgeToAttend >= 18
                               }
                               else
                               {
                                   if(ride.endsWith("Att"))
                                   {
                                       return true
                                   }
                               }
                           }

                       }

                    }
                }
            }
       }
        return false
    }

    // Assign a staff member to a specific ride
    fun assignStaffToRide(staff: String, ride: String, completedBoard: ArrayList<ArrayList<String>>) {
        // Remove staff from their current ride and add them to the new ride
        for (rideList in completedBoard) {
            rideList.remove(staff) // Remove staff from their current ride
            if (rideList[0] == ride) {
                rideList.add(staff) // Add staff to the target ride
            }
        }
    }
}

package com.example.timetabler

import android.text.BoringLayout
import java.util.jar.Attributes.Name
import kotlin.math.min
import kotlin.random.Random

class GenerateTimetable {

    var fh = FirebaseHandler()
    var shortBoardUsed = false

    fun getShortBoardUsed(callback: (Boolean) -> Unit){
        callback(shortBoardUsed)
    }
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
        return Random.nextInt(0, staffSelected.size)
    }

    fun getRandomStaff(staffSelected: ArrayList<String>, currentRide: String, staffObjList : ArrayList<Staff>, rides : ArrayList<Ride>, skip : Boolean) : String {
        val staffForRide = ArrayList<Staff>()

        // Filter staff trained for the current ride
        for (r in staffObjList) {
            for (i in r.RidesTrained) {
                if (i.toString().contains(currentRide))
                {
                    //check the category and age
                    for(ride in rides){
                        if(currentRide.contains(ride.Name)){
                            if(currentRide.endsWith(" Att"))
                            {
                                val minAgeAtt = ride.minAgeToAttend
                                val minAgeOp = ride.minAgeToOperate
                                val age = fh.calculateAge(r.DoB.toDate())
                                if(age in minAgeAtt..<minAgeOp){//they are old enough to attend
                                    staffForRide.add(r)
                                }
                            }
                            else
                            {
                                val minAge = ride.minAgeToOperate
                                if(fh.calculateAge(r.DoB.toDate()) >= minAge){//they are old enough to operate
                                    staffForRide.add(r)
                                }
                            }
                        }
                    }
                }
            }
        }

        while (staffForRide.isNotEmpty()) {
            val randomIndex = getRandomNumber(ArrayList(staffForRide.map { it.Name }))
            val randomStaff = staffForRide[randomIndex]

            // Check if the selected staff meets the conditions
            if(skip)
            {
                for (r in randomStaff.RidesTrained)
                {
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
            else
            {
                if (!currentRide.contains(randomStaff.PreviousRide) || randomStaff.PreviousRide == "")
                { // Were they on it before?
                    for (r in randomStaff.RidesTrained)
                    {
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

        val indices = completeBoard.indices.shuffled() // Shuffle indices to randomize order
        val usedIndices = mutableSetOf<Int>() // Keep track of used indices

        indices.forEach { index ->
            if (usedIndices.contains(index)) return@forEach // Skip if already processed

            val row = completeBoard[index]
            val ride = row[0]
            val staff = row[1]

            if (staff == "Select Staff") {
                if (staffSelected.isNotEmpty() || newStaffObjList.isNotEmpty()) {
                    var staffName = getRandomStaff(staffSelected, ride, newStaffObjList, rides, false)
                    if (staffName.isNotEmpty()) {
                        while (assignedStaff.contains(staffName)) {
                            staffName = getRandomStaff(staffSelected, ride, newStaffObjList, rides, false)
                        }
                        // Update the board with the selected staff
                        completeBoard[index][1] = staffName
                        assignedStaff.add(staffName)
                        staffSelected.remove(staffName)
                        newStaffObjList.removeIf { it.Name == staffName }
                        usedIndices.add(index) // Mark this index as processed
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
                shortBoardUsed = true
                completeBoard = shortBoard//override it
            }


            if(spareStaff.size != 0)//spare rides and staff - go and check and assign
            {
                //use the list of unassigned rides
//                for(u in unassignedRides)
//                {
//                    if (u != "Car Park") //as no one is trained skip
//                    {
//                        var rideSpare = u
//                        var staffTrained: ArrayList<String> = ArrayList()
//                        var role = "Op" // Default value
//                        if (u.contains("Op") || u.contains("Att")) {
//                            // Remove "Op" or "Att" from the string and set the role
//                            role = u.split(" ").last { it == "Op" || it == "Att" }
//                            rideSpare = u.replace(Regex("(Op|Att)\$"), "").trim()
//                        }
//                        for (r in rides) {
//                            if (r.Name == rideSpare) {
//                                for (s in r.staffTrained) {
//                                    if (s.toString().contains(role)) {
//                                        staffTrained.add(s.toString())
//                                    }
//                                }
//
//                            }
//                        }
//                        staffTrained.shuffle()
//                        spareStaff.shuffle()
//
//                        //filter spare list, so only ones that matches rhe role are in the list
//                        //spareStaff.filter { it.endsWith(role) }
//                        var staffChosen: Staff? = null
//                        var rideObj : Ride? = null
//                        val randomStaff = spareStaff[0]
//                        var exitWhile = false
//                        val stack = mutableListOf<Pair<String, String>>() // Stack to hold (staff, rideToAssign)
//                        val visited = mutableListOf<Pair<Int, String>>() // List to track visited (staff, ride) pairs
//                        stack.clear()
//                        visited.clear()
//                        staffTrained.retainAll(spareStaff.toSet())
//                        staffTrained.shuffle()
//                        if(staffTrained.size ==0)
//                        {
//                            exitWhile = true
//                        }
//                        while(!exitWhile)
//                        {
//                            for (sol in staffObjList) {
//                                for(st in staffTrained)
//                                {
//                                    if(st == randomStaff){
//                                        if (randomStaff == sol.Name)
//                                        {
//                                            staffChosen = sol
//                                            break
//                                        }
//                                    }
//                                }
//                            }
//                            for (r in rides) {
//                                if (r.Name == u) {
//                                    rideObj = r
//                                    break
//                                }
//                            }
//                            if (staffChosen != null && rideObj != null) {
//                                for(r in staffChosen.RidesTrained)
//                                {
//                                    if(r.toString().contains(rideObj.Name))
//                                    {
//                                        if (findAndSwapStaff(staffChosen, u, completeBoard, staffTrained, rides, staffObjList, stack, visited))
//                                        {
//                                            spareStaff.removeAt(0)//swap has been made, remove the spare staff from the list and go again
//                                            exitWhile = true
//                                        }
//                                    }
//                                }
//
//                            }
//                        }
//
//                    }
//                }



                //put the rest on car park
                completeBoard.forEachIndexed { index, row ->
                    val ride = row[0]

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
            while (spareStaff.size != 0) {
                completeBoard.add(arrayListOf("Car Park", spareStaff[0]))
                spareStaff.removeAt(0)
            }
        }

        callback(completeBoard)

    }

    fun findAndSwapStaff(staff: Staff,
                         unassignedRide: String,
                         completedBoard: ArrayList<ArrayList<String>>,
                         staffTrained: ArrayList<String>, rides: ArrayList<Ride>,
                         staffObjList: ArrayList<Staff>,
                         stack: MutableList<Pair<String, String>>,
                         visited: MutableList<Pair<Int, String>>): Boolean {


        if(staff.RidesTrained.contains(unassignedRide))
        {
            stack.add(Pair(staff.Name, unassignedRide))
            workBackThroughStack(stack, completedBoard, staffObjList)
            return true
        }
        else
        {
            val randomRide = staff.RidesTrained.get(Random.nextInt(0, staff.RidesTrained.size - 1)).toString()
            var staffOnRandomRide = ""
            var indexOfRide = 0
            var staffOnRideObj: Staff? = null
            completedBoard.forEachIndexed { index, row ->
                if (randomRide.contains(row[0])) {
                    staffOnRandomRide = row[1]
                    indexOfRide = index

                }
            }
            val isVisited = visited.any { pair -> pair.first == indexOfRide }
            if (isVisited) {

            }
            else
            {
                if (staffOnRandomRide != "")
                {
                    for (s in staffObjList)
                    {
                        if (s.Name == staffOnRandomRide)
                        {
                            staffOnRideObj = s
                            break
                        }
                    }
                }
            }
            //check if the staff are able able to swap by category
            if (isSwapAllowed(staff, staffOnRideObj!!, completedBoard, unassignedRide))
            {
                visited.add(Pair(indexOfRide, staff.Name))
                if (isTrained(staffOnRideObj, unassignedRide))
                {
                    stack.add(Pair(unassignedRide, staffOnRideObj.Name))
                    workBackThroughStack(stack, completedBoard, staffObjList)
                    return true
                }
                else
                {
                    stack.add(Pair(randomRide, staff.Name))
                    //go back to finding a random ride for this staff
                    findAndSwapStaff(staffOnRideObj, unassignedRide, completedBoard, staffTrained, rides,  staffObjList, stack, visited)//RESETS STACK
                }
            }
            findAndSwapStaff(staff, unassignedRide, completedBoard, staffTrained, rides,  staffObjList, stack, visited)//RESETS STACK

        }
        return false
    }

    fun workBackThroughStack(stack: MutableList<Pair<String, String>>, completedBoard: ArrayList<ArrayList<String>>, staffObjList : ArrayList<Staff>) {
        while (stack.isNotEmpty()) {
            val (staff, ride) = stack.removeAt(stack.lastIndex) // Pop the stack
            assignStaffToRide(staff, ride, completedBoard) // Assign staff to their respective rides
        }
    }

    // Find a ride the staff member is trained for that has staff assigned
    fun isSwapAllowed(staff: Staff, staffOnRideObj : Staff, completedBoard: ArrayList<ArrayList<String>>, unasignedRide : String): Boolean {
        for(r in staffOnRideObj.RidesTrained)
        {
            if(r.toString().contains(unasignedRide))
            {
                if(staff.Category == "Attendant")
                {
                    return staffOnRideObj.Category == "Attendant"
                }
                else
                {
                    return staffOnRideObj.Category != "Attendant"
                }
            }
        }
        return false
    }

    fun isTrained(staff: Staff, ride:String) : Boolean
    {
        for(r in staff.RidesTrained)
        {
            if(r.toString().contains(ride))
            {
                return true
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

    fun timetable2(list : ArrayList<ArrayList<String>>, staffSelected : ArrayList<String> , staffObjList : ArrayList<Staff>, rides : ArrayList<Ride>, callback: (ArrayList<ArrayList<String>>) -> Unit)
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
        fh.getPriority { priorityList ->
            priorityList.sortByDescending { it.second } // Sort by the integer value in the pair
            val assignedStaff = mutableSetOf<String>()
            // Create an ArrayList of ArrayLists to represent the 2D structure
            var completeBoard = ArrayList<ArrayList<String>>()
            //create copy for the full board
            list.forEach { row ->
                val ride = row[0]
                val staff = row[1]
                completeBoard.add(arrayListOf(ride, staff))

            }

            if(priorityList[priorityList.size -1].second == priorityList[0].second){

                val indices = completeBoard.indices.shuffled() // Shuffle indices to randomize order
                val usedIndices = mutableSetOf<Int>() // Keep track of used indices

                indices.forEach { index ->
                    if (usedIndices.contains(index)) return@forEach // Skip if already processed

                    val row = completeBoard[index]
                    val ride = row[0]
                    val staff = row[1]

                    if (staff == "Select Staff") {
                        if (staffSelected.isNotEmpty() || newStaffObjList.isNotEmpty()) {
                            var staffName = getRandomStaff(staffSelected, ride, newStaffObjList, rides, true)
                            if (staffName.isNotEmpty()) {
                                while (assignedStaff.contains(staffName)) {
                                    staffName = getRandomStaff(staffSelected, ride, newStaffObjList, rides, true)
                                }
                                // Update the board with the selected staff
                                completeBoard[index][1] = staffName
                                assignedStaff.add(staffName)
                                staffSelected.remove(staffName)
                                newStaffObjList.removeIf { it.Name == staffName }
                                usedIndices.add(index) // Mark this index as processed
                            }
                        }
                    }
                }
            }
            else
            {
                for(p in priorityList)
                {
                    completeBoard.forEachIndexed{index, row ->

                        if(completeBoard[index][0].contains(p.first))
                        {
                            if (completeBoard[index][1] == "Select Staff") {
                                if (staffSelected.isNotEmpty() || newStaffObjList.isNotEmpty()) {
                                    var staffName = getRandomStaff(staffSelected, completeBoard[index][0], newStaffObjList, rides, true)
                                    if (staffName.isNotEmpty()) {
                                        while (assignedStaff.contains(staffName)) {
                                            staffName = getRandomStaff(staffSelected, completeBoard[index][0], newStaffObjList, rides, true)
                                        }
                                        // Update the board with the selected staff
                                        completeBoard[index][1] = staffName
                                        assignedStaff.add(staffName)
                                        staffSelected.remove(staffName)
                                        newStaffObjList.removeIf { it.Name == staffName }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //by this point either the board is randomly done, or in priority order, but staff may be on previous ride

            //get missing values and spares if any
            var spareStaff : ArrayList<String> = ArrayList()
            staffSelected.shuffle()
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
            spareStaff.shuffle()
            completeBoard.forEachIndexed {index, row->

                if(row[0] == "Select Staff")
                {
                    for(s in spareStaff)
                    {
                        for(so in staffObjList)
                        {
                            if(so.Name == s)
                            {
                                if(isTrained(so, row[0]))
                                {
                                    completeBoard[index][1] = s
                                    assignedStaff.add(s)
                                    staffSelected.remove(s)
                                    newStaffObjList.removeIf { it.Name == s }
                                    unassignedRides.remove(row[1])
                                }
                            }
                        }
                    }
                }
            }
            spareStaff = ArrayList()
            for (name in staffSelected) {
                if (!assignedStaff.contains(name)) {
                    spareStaff.add(name)
                }
            }


            //put the rest on car park
            completeBoard.forEachIndexed { index, row ->
                val ride = row[0]

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

//            //find a way to limit the amount of staff on previous rides
//            var count = 0
//            completeBoard.forEach{ row->
//                for(s in staffObjList){
//                    if(s.Name == row[1]){
//                        if(row[0].contains(s.PreviousRide))
//                        {
//                            count++
//                            break
//                        }
//                    }
//                }
//            }
            callback(completeBoard)


        }

    }

    fun timetable3(list : ArrayList<ArrayList<String>>, staffSelected : ArrayList<String> , staffObjList : ArrayList<Staff>, rides : ArrayList<Ride>, callback: (ArrayList<ArrayList<String>>) -> Unit){
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

        val indices = completeBoard.indices.shuffled() // Shuffle indices to randomize order
        val usedIndices = mutableSetOf<Int>() // Keep track of used indices

        indices.forEach { index ->
            if (usedIndices.contains(index)) return@forEach // Skip if already processed

            val row = completeBoard[index]
            val ride = row[0]
            val staff = row[1]

            if (staff == "Select Staff") {
                if (staffSelected.isNotEmpty() || newStaffObjList.isNotEmpty()) {

                    var staffName = newStaffObjList.filter { it.Name.isNotEmpty() }[getRandomNumber(ArrayList(newStaffObjList.map { it.Name }))].Name
                    if (staffName.isNotEmpty()) {
                        while (assignedStaff.contains(staffName)) {
                            staffName = newStaffObjList.filter { it.Name.isNotEmpty() }[getRandomNumber(ArrayList(newStaffObjList.map { it.Name }))].Name
                        }
                        // Update the board with the selected staff
                        completeBoard[index][1] = staffName
                        assignedStaff.add(staffName)
                        staffSelected.remove(staffName)
                        newStaffObjList.removeIf { it.Name == staffName }
                        usedIndices.add(index) // Mark this index as processed
                    }
                }
            }
        }

        callback(completeBoard)
    }

    fun evaluation(t1 : ArrayList<ArrayList<String>>,
                   t2 : ArrayList<ArrayList<String>>,
                   t3 : ArrayList<ArrayList<String>>,
                   staffObjList : ArrayList<Staff>,
                   priorityList : ArrayList<Pair<String, Int>>,
                   rides : ArrayList<Ride>,
                   requirements : ArrayList<ArrayList<String>>,
                   callback: (ArrayList<ArrayList<String>>) -> Unit){
        fh.getEvalPoints {
            val staffOnPrev = it.firstOrNull { it.first == "Staff on previous ride" }?.second as? Int ?: 0
            val reqNotMet = it.firstOrNull { it.first == "Requirement not met" }?.second as? Int ?: 0
            val ridePri1 = it.firstOrNull { it.first == "Ride priority is 1" }?.second as? Int ?: 0
            val ridePri2 = it.firstOrNull { it.first == "Ride priority is 2" }?.second as? Int ?: 0
            val ridePri3 = it.firstOrNull { it.first == "Ride priority is 3" }?.second as? Int ?: 0
            val lessThanPref = it.firstOrNull { it.first == "Ride has less than preferred number of staff" }?.second as? Int ?: 0



            var t1Score = 0
            var t2Score = 0
            var t3Score = 0
            println("-----------------------------------------------------------------------------------------------------")
            t1.forEach {
                println("Ride: " + it[0] + " Staff: " + it[1])
            }
            t2.forEach {
                println("Ride: " + it[0] + " Staff: " + it[1])
            }

            t1.forEachIndexed { index, row ->
                for (s in staffObjList) {
                    if (s.Name == row[1]) {
                        if (row[0] == s.PreviousRide) {
                            t1Score += staffOnPrev
                        }
                    }
                }
                if (row[1] == "Select Staff") {
                    priorityList.forEach { pair ->
                        if (pair.first == row[0]) {
                            if (pair.second == 3) {
                                t1Score += ridePri3
                            } else if (pair.second == 2) {
                                t1Score += ridePri2
                            } else {
                                t1Score += ridePri1
                            }
                        }

                    }
                }
                requirements.forEach { requirement ->
                    if (requirement[0] == row[0]) {
                        if (requirement[1] != row[1] && requirement[1] != "Select Staff") {
                            t2Score += reqNotMet
                        }
                    }
                }
            }

            t2.forEachIndexed { index, row ->
                for (s in staffObjList) {
                    if (s.Name == row[1]) {
                        if (row[0] == s.PreviousRide) {
                            t2Score += staffOnPrev
                        }
                    }
                }
                if (row[1] == "Select Staff") {
                    priorityList.forEach { pair ->
                        if (pair.first == row[0]) {
                            if (pair.second == 3) {
                                t2Score += ridePri3
                            } else if (pair.second == 2) {
                                t2Score += ridePri2
                            } else {
                                t2Score += ridePri1
                            }
                        }

                    }
                }
                requirements.forEach { requirement ->
                    if (requirement[0] == row[0]) {
                        if (requirement[1] != row[1] && requirement[1] != "Select Staff") {
                            t2Score += reqNotMet
                        }
                    }
                }
            }
        t3.forEachIndexed {  index, row->
            for(s in staffObjList){
                if(s.Name == row[1]){
                    if(row[0] == s.PreviousRide){
                        t3Score += staffOnPrev
                    }
                }
            }
            if(row[1] == "Select Staff")
            {
                priorityList.forEach{ pair ->
                    if(pair.first == row[0]) {
                        if (pair.second == 3) {
                            t3Score += ridePri3
                        } else if (pair.second == 2) {
                            t3Score += ridePri2
                        } else {
                            t3Score += ridePri1
                        }
                    }

                }
            }
            requirements.forEach{ requirement ->
                if(requirement[0] == row[0])
                {
                    if(requirement[1] != row[1]){
                        t3Score += reqNotMet
                    }
                }
            }
        }

            println(t1Score)
            println(t2Score)
            println(t3Score)
            if (t1Score <= t2Score && t1Score <= t3Score) {
                println("NUMBER 1")
                callback(t1)
            } else if (t2Score <= t1Score && t2Score <= t3Score) {
                println("NUMBER 2")
                callback(t2)
            } else {
                println("NUMBER 3")
                callback(t3)
            }
        }

    }
}

package com.example.timetabler

import java.io.Serializable

data class Manager (
    var accessLevel: Int,
    var name: String = ""
): Serializable

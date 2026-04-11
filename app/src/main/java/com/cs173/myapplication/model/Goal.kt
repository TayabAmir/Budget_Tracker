package com.cs173.myapplication.model

import java.io.Serializable

data class Goal(
    val id: Int,
    var name: String,
    var targetAmount: Double,
    var savedAmount: Double,
    var deadline: String,
    var imageUrl: String
) : Serializable

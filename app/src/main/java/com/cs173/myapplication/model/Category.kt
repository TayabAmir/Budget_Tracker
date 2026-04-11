package com.cs173.myapplication.model

import java.io.Serializable

data class Category(
    val id: Int,
    var name: String,
    var limit: Double,
    var colorHex: String,
    var iconUrl: String
) : Serializable

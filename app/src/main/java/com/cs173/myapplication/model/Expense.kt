package com.cs173.myapplication.model

import java.io.Serializable

data class Expense(
    val id: Int,
    var title: String,
    var amount: Double,
    var categoryId: Int,
    var date: String,
    var isRecurring: Boolean,
    var imageUrl: String,
    var notes: String
) : Serializable

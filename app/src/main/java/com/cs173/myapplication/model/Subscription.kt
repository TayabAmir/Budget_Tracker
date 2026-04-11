package com.cs173.myapplication.model

import java.io.Serializable

data class Subscription(
    val id: Int,
    var name: String,
    var amount: Double,
    var billingCycle: String, // Monthly/Yearly/Weekly
    var nextBillingDate: String,
    var iconUrl: String,
    var isActive: Boolean
) : Serializable

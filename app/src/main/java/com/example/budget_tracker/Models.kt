package com.example.budget_tracker

import java.io.Serializable

data class Expense(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val imageUrl: String? = null
) : Serializable

data class Subscription(
    val id: Int,
    val name: String,
    val price: Double,
    val billingCycle: String, // Monthly, Yearly
    val nextBillingDate: String,
    val imageUrl: String? = null
) : Serializable

data class Category(
    val name: String,
    val limit: Double,
    var spent: Double = 0.0
)

data class SavingsGoal(
    val id: Int,
    val title: String,
    val targetAmount: Double,
    var currentAmount: Double,
    val imageUrl: String? = null
) : Serializable

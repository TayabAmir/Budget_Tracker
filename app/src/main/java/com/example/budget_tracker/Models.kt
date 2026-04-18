package com.example.budget_tracker

import java.io.Serializable
import java.util.Date

data class AppUser(
    val id: Int,
    val username: String,
    val email: String,
    val password: String,
    var balance: Double = 1000.0 // Starting balance
) : Serializable

data class Expense(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val linkedSubscriptionId: Int? = null
) : Serializable

data class Subscription(
    val id: Int,
    val name: String,
    val price: Double,
    val billingCycle: String, // Monthly, Yearly
    var nextBillingDate: String,
    var nextBillingTimestamp: Long,
    val imageUrl: String? = null,
    var isActive: Boolean = true
) : Serializable

data class Category(
    val name: String,
    var limit: Double,
    var spent: Double = 0.0
)

data class SavingsGoal(
    val id: Int,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val imageUrl: String? = null
) : Serializable

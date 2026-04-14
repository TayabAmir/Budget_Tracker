package com.cs173.myapplication

import android.content.Context
import com.cs173.myapplication.model.Category
import com.cs173.myapplication.model.Expense
import com.cs173.myapplication.model.Goal
import com.cs173.myapplication.model.Subscription
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppData {
    var expenses = ArrayList<Expense>()
    var subscriptions = ArrayList<Subscription>() // Legacy, we'll favor expenses.isRecurring
    var goals = ArrayList<Goal>()
    var categories = ArrayList<Category>()
    var monthlyBudgetLimit: Double = 50000.0 // Default limit

    private const val PREFS_NAME = "SmartBudgetPrefs"
    private val gson = Gson()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        val expensesJson = prefs.getString("expenses", null)
        val subscriptionsJson = prefs.getString("subscriptions", null)
        val goalsJson = prefs.getString("goals", null)
        val categoriesJson = prefs.getString("categories", null)
        monthlyBudgetLimit = prefs.getFloat("monthlyBudgetLimit", 50000.0f).toDouble()

        if (expensesJson != null) {
            val type = object : TypeToken<ArrayList<Expense>>() {}.type
            expenses = gson.fromJson(expensesJson, type)
        }
        if (subscriptionsJson != null) {
            val type = object : TypeToken<ArrayList<Subscription>>() {}.type
            subscriptions = gson.fromJson(subscriptionsJson, type)
        }
        if (goalsJson != null) {
            val type = object : TypeToken<ArrayList<Goal>>() {}.type
            goals = gson.fromJson(goalsJson, type)
        }
        if (categoriesJson != null) {
            val type = object : TypeToken<ArrayList<Category>>() {}.type
            categories = gson.fromJson(categoriesJson, type)
        }

        if (categories.isEmpty()) {
            loadDefaultCategories()
            save(context)
        }
    }

    private fun loadDefaultCategories() {
        categories.add(Category(1, "Food", 15000.0, "#FF5722", "https://cdn-icons-png.flaticon.com/512/706/706164.png"))
        categories.add(Category(2, "Transport", 8000.0, "#2196F3", "https://cdn-icons-png.flaticon.com/512/744/744465.png"))
        categories.add(Category(3, "Entertainment", 5000.0, "#9C27B0", "https://cdn-icons-png.flaticon.com/512/3163/3163478.png"))
        categories.add(Category(4, "Health", 6000.0, "#4CAF50", "https://cdn-icons-png.flaticon.com/512/2966/2966486.png"))
        categories.add(Category(5, "Shopping", 20000.0, "#FFC107", "https://cdn-icons-png.flaticon.com/512/3081/3081840.png"))
        categories.add(Category(6, "Utilities", 10000.0, "#607D8B", "https://cdn-icons-png.flaticon.com/512/3105/3105807.png"))
        categories.add(Category(7, "Travel", 12000.0, "#00BCD4", "https://cdn-icons-png.flaticon.com/512/201/201623.png"))
        categories.add(Category(8, "Education", 5000.0, "#3F51B5", "https://cdn-icons-png.flaticon.com/512/167/167707.png"))
        categories.add(Category(9, "Gifts", 3000.0, "#E91E63", "https://cdn-icons-png.flaticon.com/512/1170/1170611.png"))
        categories.add(Category(10, "Others", 4000.0, "#795548", "https://cdn-icons-png.flaticon.com/512/570/570223.png"))
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("expenses", gson.toJson(expenses))
        editor.putString("subscriptions", gson.toJson(subscriptions))
        editor.putString("goals", gson.toJson(goals))
        editor.putString("categories", gson.toJson(categories))
        editor.putFloat("monthlyBudgetLimit", monthlyBudgetLimit.toFloat())
        editor.apply()
    }
}

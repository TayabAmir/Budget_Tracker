package com.cs173.myapplication

import com.cs173.myapplication.model.Category
import com.cs173.myapplication.model.Expense
import com.cs173.myapplication.model.Goal
import com.cs173.myapplication.model.Subscription

object AppData {
    val expenses = ArrayList<Expense>()
    val subscriptions = ArrayList<Subscription>()
    val goals = ArrayList<Goal>()
    val categories = ArrayList<Category>()

    init {
        // Pre-populate Categories
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

        // Pre-populate Expenses
        expenses.add(Expense(1, "Groceries", 3500.0, 1, "2023-10-25", false, "", "Weekly groceries"))
        expenses.add(Expense(2, "Netflix", 1000.0, 3, "2023-10-20", true, "", "Monthly subscription"))
        expenses.add(Expense(3, "Electricity", 2200.0, 6, "2023-10-15", false, "", "Monthly bill"))
        expenses.add(Expense(4, "Fuel", 1800.0, 2, "2023-10-22", false, "", "Petrol for car"))
        expenses.add(Expense(5, "Dining Out", 2800.0, 1, "2023-10-24", false, "", "Dinner with friends"))
        expenses.add(Expense(6, "Gym", 1500.0, 4, "2023-10-01", true, "", "Monthly membership"))
        expenses.add(Expense(7, "Amazon Purchase", 4500.0, 5, "2023-10-18", false, "", "New headphones"))
        expenses.add(Expense(8, "Mobile Bill", 700.0, 6, "2023-10-12", false, "", "Monthly recharge"))
        expenses.add(Expense(9, "Medicine", 900.0, 4, "2023-10-10", false, "", "Vitamins"))
        expenses.add(Expense(10, "Spotify", 250.0, 3, "2023-10-05", true, "", "Music subscription"))

        // Pre-populate Subscriptions
        subscriptions.add(Subscription(1, "Netflix", 1000.0, "Monthly", "2023-11-20", "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg", true))
        subscriptions.add(Subscription(2, "Spotify", 250.0, "Monthly", "2023-11-05", "https://upload.wikimedia.org/wikipedia/commons/1/19/Spotify_logo_without_text.svg", true))
        subscriptions.add(Subscription(3, "iCloud", 1200.0, "Yearly", "2024-05-15", "", true))
        subscriptions.add(Subscription(4, "YouTube Premium", 180.0, "Monthly", "2023-11-12", "", true))
        subscriptions.add(Subscription(5, "Adobe CC", 3200.0, "Monthly", "2023-11-28", "", true))
        subscriptions.add(Subscription(6, "Gym", 1500.0, "Monthly", "2023-11-01", "", true))
        subscriptions.add(Subscription(7, "Disney+", 800.0, "Monthly", "2023-11-15", "", false))
        subscriptions.add(Subscription(8, "Amazon Prime", 1499.0, "Yearly", "2024-01-10", "", true))
        subscriptions.add(Subscription(9, "Xbox Live", 700.0, "Monthly", "2023-11-05", "", true))
        subscriptions.add(Subscription(10, "Office 365", 4000.0, "Yearly", "2024-03-22", "", true))

        // Pre-populate Goals
        goals.add(Goal(1, "iPhone 16", 250000.0, 45000.0, "2024-12-01", "https://m.media-amazon.com/images/I/61f4S3AL31L._SL1500_.jpg"))
        goals.add(Goal(2, "Emergency Fund", 100000.0, 62000.0, "2024-06-30", ""))
        goals.add(Goal(3, "Vacation", 80000.0, 15000.0, "2024-08-15", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e"))
        goals.add(Goal(4, "New Laptop", 150000.0, 120000.0, "2023-12-25", ""))
        goals.add(Goal(5, "Car Downpayment", 500000.0, 100000.0, "2025-01-01", ""))
        goals.add(Goal(6, "Wedding", 1000000.0, 200000.0, "2025-10-10", ""))
        goals.add(Goal(7, "Retirement", 5000000.0, 500000.0, "2045-01-01", ""))
        goals.add(Goal(8, "Home Renovation", 300000.0, 50000.0, "2024-05-01", ""))
        goals.add(Goal(9, "Christmas Gifts", 20000.0, 18000.0, "2023-12-15", ""))
        goals.add(Goal(10, "Education Fund", 200000.0, 30000.0, "2026-09-01", ""))
    }
}

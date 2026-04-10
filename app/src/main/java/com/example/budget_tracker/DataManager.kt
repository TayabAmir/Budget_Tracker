package com.example.budget_tracker

object DataManager {
    val expenses = mutableListOf<Expense>()
    val subscriptions = mutableListOf<Subscription>()
    val savingsGoals = mutableListOf<SavingsGoal>()
    val categories = mutableListOf<Category>()

    init {
        // Sample Data
        categories.add(Category("Food", 500.0))
        categories.add(Category("Transport", 200.0))
        categories.add(Category("Entertainment", 300.0))
        categories.add(Category("Shopping", 400.0))

        expenses.add(Expense(1, "Lunch", 15.0, "Food", "2023-10-25"))
        expenses.add(Expense(2, "Bus Fare", 2.5, "Transport", "2023-10-25"))
        expenses.add(Expense(3, "Movie Ticket", 12.0, "Entertainment", "2023-10-24"))

        subscriptions.add(Subscription(1, "Netflix", 15.99, "Monthly", "2023-11-01"))
        subscriptions.add(Subscription(2, "Spotify", 9.99, "Monthly", "2023-11-05"))

        savingsGoals.add(SavingsGoal(1, "New Laptop", 1200.0, 450.0))
        savingsGoals.add(SavingsGoal(2, "Vacation", 3000.0, 1200.0))
    }

    fun addExpense(expense: Expense) {
        expenses.add(expense)
        updateCategorySpent(expense.category, expense.amount)
    }

    private fun updateCategorySpent(categoryName: String, amount: Double) {
        categories.find { it.name == categoryName }?.let {
            it.spent += amount
        }
    }

    fun getFilteredExpenses(query: String): List<Expense> {
        return expenses.filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
    }
}

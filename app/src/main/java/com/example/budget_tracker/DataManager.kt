package com.example.budget_tracker

object DataManager {
    val expenses = mutableListOf<Expense>()
    val subscriptions = mutableListOf<Subscription>()
    val savingsGoals = mutableListOf<SavingsGoal>()
    val categories = mutableListOf<Category>()
    private val users = mutableListOf<AppUser>()

    var currentUser: AppUser? = null
        private set

    init {
        categories.add(Category("Food", 500.0))
        categories.add(Category("Transport", 200.0))
        categories.add(Category("Entertainment", 300.0))
        categories.add(Category("Shopping", 400.0))
    }

    fun addExpense(expense: Expense) {
        expenses.add(expense)
        updateCategorySpent(expense.category, expense.amount)
    }

    fun addSubscription(subscription: Subscription) {
        subscriptions.add(subscription)
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        savingsGoals.add(goal)
    }

    private fun updateCategorySpent(categoryName: String, amount: Double) {
        categories.find { it.name == categoryName }?.let {
            it.spent += amount
        }
    }

    fun getFilteredExpenses(query: String): List<Expense> {
        return expenses.filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
    }

    fun signIn(email: String, password: String): String? {
        val normalizedEmail = normalizeEmail(email)
        val user = users.find { normalizeEmail(it.email) == normalizedEmail && it.password == password }
            ?: return "Invalid email or password"

        currentUser = user
        return null
    }

    fun signUp(username: String, email: String, password: String): String? {
        val normalizedEmail = normalizeEmail(email)
        if (users.any { normalizeEmail(it.email) == normalizedEmail }) {
            return "An account with this email already exists"
        }

        val newUser = AppUser(
            id = users.size + 1,
            username = username.trim(),
            email = normalizedEmail,
            password = password
        )
        users.add(newUser)
        currentUser = newUser
        return null
    }

    fun signOut() {
        currentUser = null
    }

    fun isSignedIn(): Boolean {
        return currentUser != null
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }
}

package com.example.budget_tracker

import java.util.Calendar

object DataManager {
    val expenses = mutableListOf<Expense>()
    val subscriptions = mutableListOf<Subscription>()
    val categories = mutableListOf<Category>()
    val savingsGoals = mutableListOf<SavingsGoal>()
    private val users = mutableListOf<AppUser>()

    var currentUser: AppUser? = null
        private set

    var globalLimit: Double = 2000.0

    init {
        categories.add(Category("Food", 400.0))
        categories.add(Category("Transport", 200.0))
        categories.add(Category("Entertainment", 300.0))
        categories.add(Category("Shopping", 400.0))
        categories.add(Category("Subscription", 700.0))
    }

    fun addExpense(expense: Expense): Boolean {
        val currentTotalSpent = expenses.sumOf { it.amount }
        if (currentTotalSpent + expense.amount > globalLimit) {
            return false
        }
        
        // Check category limit
        val category = categories.find { it.name == expense.category }
        if (category != null && category.spent + expense.amount > category.limit) {
            return false
        }

        expenses.add(expense)
        updateCategorySpent(expense.category, expense.amount)
        currentUser?.let { it.balance -= expense.amount }
        return true
    }

    fun deleteExpense(expenseId: Int) {
        val expense = expenses.find { it.id == expenseId } ?: return
        expenses.remove(expense)
        updateCategorySpent(expense.category, -expense.amount)
        currentUser?.let { it.balance += expense.amount }

        expense.linkedSubscriptionId?.let { subId ->
            subscriptions.removeAll { it.id == subId }
        }
    }

    fun updateExpense(updatedExpense: Expense): Boolean {
        val index = expenses.indexOfFirst { it.id == updatedExpense.id }
        if (index != -1) {
            val oldExpense = expenses[index]
            val currentTotalWithoutOld = expenses.sumOf { it.amount } - oldExpense.amount
            if (currentTotalWithoutOld + updatedExpense.amount > globalLimit) {
                return false
            }
            
            // Check category limit
            val category = categories.find { it.name == updatedExpense.category }
            if (category != null) {
                val categorySpentWithoutOld = if (oldExpense.category == updatedExpense.category) {
                    category.spent - oldExpense.amount
                } else {
                    category.spent
                }
                if (categorySpentWithoutOld + updatedExpense.amount > category.limit) {
                    return false
                }
            }
            
            updateCategorySpent(oldExpense.category, -oldExpense.amount)
            currentUser?.let { it.balance += oldExpense.amount }
            
            expenses[index] = updatedExpense
            updateCategorySpent(updatedExpense.category, updatedExpense.amount)
            currentUser?.let { it.balance -= updatedExpense.amount }

            // Sync with subscription if linked
            updatedExpense.linkedSubscriptionId?.let { subId ->
                subscriptions.find { it.id == subId }?.let { sub ->
                    // Remove "Subscription: " prefix if present in the expense title for the sub name
                    val subName = if (updatedExpense.title.startsWith("Subscription: ")) {
                        updatedExpense.title.removePrefix("Subscription: ")
                    } else {
                        updatedExpense.title
                    }
                    
                    val updatedSub = sub.copy(
                        name = subName,
                        price = updatedExpense.amount
                    )
                    val subIndex = subscriptions.indexOf(sub)
                    if (subIndex != -1) {
                        subscriptions[subIndex] = updatedSub
                    }
                }
            }
            return true
        }
        return false
    }

    fun addSubscription(subscription: Subscription) {
        subscriptions.add(subscription)
    }

    fun toggleSubscriptionActive(subscriptionId: Int) {
        subscriptions.find { it.id == subscriptionId }?.let {
            it.isActive = !it.isActive
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        savingsGoals.add(goal)
    }

    fun updateSavingsGoal(updatedGoal: SavingsGoal) {
        val index = savingsGoals.indexOfFirst { it.id == updatedGoal.id }
        if (index != -1) {
            savingsGoals[index] = updatedGoal
        }
    }

    fun deleteSavingsGoal(goalId: Int) {
        savingsGoals.removeAll { it.id == goalId }
    }

    private fun updateCategorySpent(categoryName: String, amount: Double) {
        categories.find { it.name == categoryName }?.let {
            it.spent += amount
        } ?: run {
            // Add category if not exists (e.g. for dynamic categories)
            val newCat = Category(categoryName, 500.0)
            newCat.spent = amount
            categories.add(newCat)
        }
    }

    fun getFilteredExpenses(query: String, sortBy: String = "Date"): List<Expense> {
        val filtered = expenses.filter { 
            it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) 
        }
        return when (sortBy) {
            "Price (Low to High)" -> filtered.sortedBy { it.amount }
            "Price (High to Low)" -> filtered.sortedByDescending { it.amount }
            "Date (Newest)" -> filtered.sortedByDescending { it.timestamp }
            "Date (Oldest)" -> filtered.sortedBy { it.timestamp }
            else -> filtered
        }
    }

    fun getSubscriptionsFiltered(activeOnly: Boolean?, daysLimit: Int?): List<Subscription> {
        var filtered = subscriptions.toList()
        if (activeOnly != null) {
            filtered = filtered.filter { it.isActive == activeOnly }
        }
        if (daysLimit != null) {
            val limitTime = System.currentTimeMillis() + (daysLimit.toLong() * 24 * 60 * 60 * 1000)
            filtered = filtered.filter { it.nextBillingTimestamp <= limitTime }
        }
        return filtered
    }

    fun checkAndProcessSubscriptions() {
        val now = System.currentTimeMillis()
        subscriptions.filter { it.isActive && it.nextBillingTimestamp <= now }.forEach { sub ->
            val expense = Expense(
                id = (expenses.maxOfOrNull { it.id } ?: 0) + 1,
                title = "Subscription: ${sub.name}",
                amount = sub.price,
                category = "Subscription",
                date = "Auto-deduct",
                timestamp = now,
                linkedSubscriptionId = sub.id
            )
            if (addExpense(expense)) {
                // Update next billing date
                val cal = Calendar.getInstance()
                cal.timeInMillis = sub.nextBillingTimestamp
                if (sub.billingCycle == "Monthly") cal.add(Calendar.MONTH, 1)
                else cal.add(Calendar.YEAR, 1)
                sub.nextBillingTimestamp = cal.timeInMillis
                // Update date string (simple format for demo)
                sub.nextBillingDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.time)
            }
        }
    }

    fun signIn(email: String, password: String): String? {
        val normalizedEmail = email.trim().lowercase()
        val user = users.find { it.email.trim().lowercase() == normalizedEmail && it.password == password }
            ?: return "Invalid email or password"

        currentUser = user
        return null
    }

    fun signUp(username: String, email: String, password: String): String? {
        val normalizedEmail = email.trim().lowercase()
        if (users.any { it.email.trim().lowercase() == normalizedEmail }) {
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
}

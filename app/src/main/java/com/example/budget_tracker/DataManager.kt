package com.example.budget_tracker

import java.util.Calendar

object DataManager {
    private val allExpenses = mutableListOf<Expense>()
    private val allSubscriptions = mutableListOf<Subscription>()
    private val allCategories = mutableListOf<Category>()
    private val allSavingsGoals = mutableListOf<SavingsGoal>()
    private val users = mutableListOf<AppUser>()

    var currentUser: AppUser? = null
        private set

    val expenses: List<Expense>
        get() = allExpenses.filter { it.userId == currentUser?.id }

    val subscriptions: List<Subscription>
        get() = allSubscriptions.filter { it.userId == currentUser?.id }

    val categories: List<Category>
        get() = allCategories.filter { it.userId == currentUser?.id }

    val savingsGoals: List<SavingsGoal>
        get() = allSavingsGoals.filter { it.userId == currentUser?.id }

    var globalLimit: Double
        get() = currentUser?.globalLimit ?: 2000.0
        set(value) {
            currentUser?.globalLimit = value
        }

    fun addExpense(expense: Expense): Boolean {
        val user = currentUser ?: return false
        val currentTotalSpent = expenses.sumOf { it.amount }
        if (currentTotalSpent + expense.amount > globalLimit) {
            return false
        }
        
        // Check category limit
        val category = categories.find { it.name == expense.category }
        if (category != null && category.spent + expense.amount > category.limit) {
            return false
        }

        allExpenses.add(expense.copy(userId = user.id))
        updateCategorySpent(expense.category, expense.amount)
        user.balance -= expense.amount
        return true
    }

    fun deleteExpense(expenseId: Int) {
        val expense = allExpenses.find { it.id == expenseId && it.userId == currentUser?.id } ?: return
        allExpenses.remove(expense)
        updateCategorySpent(expense.category, -expense.amount)
        currentUser?.let { it.balance += expense.amount }

        expense.linkedSubscriptionId?.let { subId ->
            allSubscriptions.removeAll { it.id == subId && it.userId == currentUser?.id }
        }
    }

    fun updateExpense(updatedExpense: Expense): Boolean {
        val index = allExpenses.indexOfFirst { it.id == updatedExpense.id && it.userId == currentUser?.id }
        if (index != -1) {
            val oldExpense = allExpenses[index]
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
            
            allExpenses[index] = updatedExpense.copy(userId = currentUser!!.id)
            updateCategorySpent(updatedExpense.category, updatedExpense.amount)
            currentUser?.let { it.balance -= updatedExpense.amount }

            // Sync with subscription if linked
            updatedExpense.linkedSubscriptionId?.let { subId ->
                allSubscriptions.find { it.id == subId && it.userId == currentUser?.id }?.let { sub ->
                    val subName = if (updatedExpense.title.startsWith("Subscription: ")) {
                        updatedExpense.title.removePrefix("Subscription: ")
                    } else {
                        updatedExpense.title
                    }
                    
                    val updatedSub = sub.copy(
                        name = subName,
                        price = updatedExpense.amount
                    )
                    val subIndex = allSubscriptions.indexOf(sub)
                    if (subIndex != -1) {
                        allSubscriptions[subIndex] = updatedSub
                    }
                }
            }
            return true
        }
        return false
    }

    fun addSubscription(subscription: Subscription) {
        currentUser?.let {
            allSubscriptions.add(subscription.copy(userId = it.id))
        }
    }

    fun toggleSubscriptionActive(subscriptionId: Int) {
        allSubscriptions.find { it.id == subscriptionId && it.userId == currentUser?.id }?.let {
            it.isActive = !it.isActive
        }
    }

    fun addSavingsGoal(goal: SavingsGoal) {
        currentUser?.let {
            allSavingsGoals.add(goal.copy(userId = it.id))
        }
    }

    fun updateSavingsGoal(updatedGoal: SavingsGoal) {
        val index = allSavingsGoals.indexOfFirst { it.id == updatedGoal.id && it.userId == currentUser?.id }
        if (index != -1) {
            allSavingsGoals[index] = updatedGoal.copy(userId = currentUser!!.id)
        }
    }

    fun deleteSavingsGoal(goalId: Int) {
        allSavingsGoals.removeAll { it.id == goalId && it.userId == currentUser?.id }
    }

    private fun updateCategorySpent(categoryName: String, amount: Double) {
        val user = currentUser ?: return
        allCategories.find { it.name == categoryName && it.userId == user.id }?.let {
            it.spent += amount
        } ?: run {
            val newCat = Category(user.id, categoryName, 500.0)
            newCat.spent = amount
            allCategories.add(newCat)
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
        val user = currentUser ?: return
        allSubscriptions.filter { it.userId == user.id && it.isActive && it.nextBillingTimestamp <= now }.forEach { sub ->
            val expense = Expense(
                id = (allExpenses.maxOfOrNull { it.id } ?: 0) + 1,
                userId = user.id,
                title = "Subscription: ${sub.name}",
                amount = sub.price,
                category = "Subscription",
                date = "Auto-deduct",
                timestamp = now,
                linkedSubscriptionId = sub.id
            )
            if (addExpense(expense)) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = sub.nextBillingTimestamp
                if (sub.billingCycle == "Monthly") cal.add(Calendar.MONTH, 1)
                else cal.add(Calendar.YEAR, 1)
                sub.nextBillingTimestamp = cal.timeInMillis
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
        initializeUserData(newUser)
        currentUser = newUser
        return null
    }

    private fun initializeUserData(user: AppUser) {
        allCategories.add(Category(user.id, "Food", 400.0))
        allCategories.add(Category(user.id, "Transport", 200.0))
        allCategories.add(Category(user.id, "Entertainment", 300.0))
        allCategories.add(Category(user.id, "Shopping", 400.0))
        allCategories.add(Category(user.id, "Subscription", 700.0))
    }

    fun signOut() {
        currentUser = null
    }

    fun isSignedIn(): Boolean {
        return currentUser != null
    }
}

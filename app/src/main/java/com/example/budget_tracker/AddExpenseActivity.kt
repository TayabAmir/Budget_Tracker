package com.example.budget_tracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budget_tracker.databinding.ActivityAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var editingExpense: Expense? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingExpense = intent.getSerializableExtra("expense") as? Expense

        setupCategories()
        populateFields()

        binding.btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun setupCategories() {
        val categories = DataManager.categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun populateFields() {
        editingExpense?.let { expense ->
            binding.etTitle.setText(expense.title)
            binding.etAmount.setText(expense.amount.toString())
            binding.etImageUrl.setText(expense.imageUrl ?: "")
            val categories = DataManager.categories.map { it.name }
            val index = categories.indexOf(expense.category)
            if (index != -1) binding.spinnerCategory.setSelection(index)
            binding.cbIsSubscription.visibility = android.view.View.GONE // Don't show for edit
            binding.btnSave.text = "Update Expense"
            setTitle("Edit Expense")
        }
    }

    private fun saveExpense() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val isSubscription = binding.cbIsSubscription.isChecked
        val imageUrl = binding.etImageUrl.text.toString().trim()

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in title and amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val timestamp = System.currentTimeMillis()
        val user = DataManager.currentUser ?: return

        if (editingExpense != null) {
            val updated = editingExpense!!.copy(
                title = title,
                amount = amount,
                category = category,
                imageUrl = imageUrl.ifBlank { null }
            )
            if (DataManager.updateExpense(updated)) {
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Update failed! Limit exceeded or category limit reached.", Toast.LENGTH_LONG).show()
            }
        } else {
            val id = (DataManager.expenses.maxOfOrNull { it.id } ?: 0) + 1
            var subId: Int? = null
            
            val newExpense = Expense(id, user.id, title, amount, category, date, timestamp, imageUrl.ifBlank { null }, subId)
            
            if (DataManager.addExpense(newExpense)) {
                if (isSubscription) {
                    val actualSubId = (DataManager.subscriptions.maxOfOrNull { it.id } ?: 0) + 1
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, 1)
                    val newSub = Subscription(
                        id = actualSubId,
                        userId = user.id,
                        name = title,
                        price = amount,
                        billingCycle = "Monthly",
                        nextBillingDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time),
                        nextBillingTimestamp = cal.timeInMillis,
                        imageUrl = imageUrl.ifBlank { null }
                    )
                    DataManager.addSubscription(newSub)
                    
                    // Link the subId back to the expense
                    val updatedExpense = newExpense.copy(linkedSubscriptionId = actualSubId)
                    DataManager.updateExpense(updatedExpense)
                }
                Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed! Limit exceeded or category limit reached.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

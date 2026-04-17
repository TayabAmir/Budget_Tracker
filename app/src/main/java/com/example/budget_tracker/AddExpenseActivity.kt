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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categories = DataManager.categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun saveExpense() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val isSubscription = binding.cbIsSubscription.isChecked
        val imageUrl = binding.etImageUrl.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, getString(R.string.expense_title_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.expense_amount_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            Toast.makeText(this, getString(R.string.expense_amount_invalid_error), Toast.LENGTH_SHORT).show()
            return
        }

        val normalizedImageUrl = if (imageUrl.isBlank()) {
            null
        } else {
            ImageUrlUtils.normalizeGoogleImageUrl(imageUrl)
        }

        if (imageUrl.isNotBlank() && normalizedImageUrl == null) {
            Toast.makeText(this, getString(R.string.invalid_google_image_url), Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val id = (DataManager.expenses.size + 1)

        val newExpense = Expense(id, title, amount, category, date, normalizedImageUrl)
        DataManager.addExpense(newExpense)

        if (isSubscription) {
            val subId = (DataManager.subscriptions.size + 1)
            val newSub = Subscription(subId, title, amount, "Monthly", date, normalizedImageUrl)
            DataManager.addSubscription(newSub)
            Toast.makeText(this, "Subscription added too!", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Expense Saved Successfully", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}

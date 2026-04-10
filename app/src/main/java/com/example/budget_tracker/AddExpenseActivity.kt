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
        val title = binding.etTitle.text.toString()
        val amountStr = binding.etAmount.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val isSubscription = binding.cbIsSubscription.isChecked
        val imageUrl = binding.etImageUrl.text.toString()

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val id = (DataManager.expenses.size + 1)

        val newExpense = Expense(id, title, amount, category, date, if(imageUrl.isNotEmpty()) imageUrl else null)
        DataManager.addExpense(newExpense)

        if (isSubscription) {
            val subId = (DataManager.subscriptions.size + 1)
            val newSub = Subscription(subId, title, amount, "Monthly", "2023-11-25", if(imageUrl.isNotEmpty()) imageUrl else null)
            DataManager.subscriptions.add(newSub)
            Toast.makeText(this, "Subscription added too!", Toast.LENGTH_SHORT).show()
        }

        Toast.makeText(this, "Expense Saved Successfully", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}

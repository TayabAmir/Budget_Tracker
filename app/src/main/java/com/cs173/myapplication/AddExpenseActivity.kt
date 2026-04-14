package com.cs173.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cs173.myapplication.databinding.ActivityAddExpenseBinding
import com.cs173.myapplication.model.Expense
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var expenseId: Int = -1
    private var selectedCategoryId: Int = 1

    private val intervalOptions = arrayOf(
        "No Recurring" to 0,
        "1 Day" to 1,
        "3 Days" to 3,
        "7 Days" to 7,
        "14 Days" to 14,
        "21 Days" to 21,
        "Monthly (30 Days)" to 30
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseId = intent.getIntExtra("expenseId", -1)

        setupSpinners()
        setupDatePicker()
        setupImageSearch()

        if (expenseId != -1) {
            binding.tvTitleScreen.text = "Edit Expense"
            binding.btnSave.text = "Update Expense"
            loadExpenseData()
        }

        binding.cbRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.spinnerBillingCycle.isEnabled = isChecked
            if (!isChecked) {
                binding.spinnerBillingCycle.setSelection(0)
            }
        }

        binding.btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun setupSpinners() {
        val categories = AppData.categories.map { it.name }
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = catAdapter
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                selectedCategoryId = AppData.categories[position].id
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val cycleDisplayNames = intervalOptions.map { it.first }
        val cycleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cycleDisplayNames)
        cycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBillingCycle.adapter = cycleAdapter
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                binding.etDate.setText(String.format("%d-%02d-%02d", year, month + 1, day))
                binding.etDate.error = null
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupImageSearch() {
        binding.btnGoogleImages.setOnClickListener {
            val query = binding.etTitle.text.toString().trim()
            val url = if (query.isNotEmpty()) {
                "https://www.google.com/search?tbm=isch&q=${Uri.encode(query + " logo direct link")}"
            } else {
                "https://images.google.com"
            }
            Toast.makeText(this, "Tip: Long press image and 'Copy image address'", Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        binding.etImageUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString().trim()
                if (url.isNotEmpty()) {
                    Glide.with(this@AddExpenseActivity)
                        .load(url)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(binding.ivPreview)
                } else {
                    binding.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        })
    }

    private fun loadExpenseData() {
        val expense = AppData.expenses.find { it.id == expenseId }
        expense?.let {
            binding.etTitle.setText(it.title)
            binding.etAmount.setText(it.amount.toString())
            binding.etDate.setText(it.date)
            binding.etNotes.setText(it.notes)
            binding.cbRecurring.isChecked = it.isRecurring
            binding.etImageUrl.setText(it.imageUrl)
            
            val intervalIndex = intervalOptions.indexOfFirst { opt -> opt.second == it.recurringIntervalDays }
            if (intervalIndex != -1) {
                binding.spinnerBillingCycle.setSelection(intervalIndex)
            }

            if (it.imageUrl.isNotEmpty()) {
                Glide.with(this).load(it.imageUrl).into(binding.ivPreview)
            }

            val catIndex = AppData.categories.indexOfFirst { c -> c.id == it.categoryId }
            if (catIndex != -1) binding.spinnerCategory.setSelection(catIndex)
        }
    }

    private fun saveExpense() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val notes = binding.etNotes.text.toString()
        val isRecurring = binding.cbRecurring.isChecked
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val recurringInterval = if (isRecurring) intervalOptions[binding.spinnerBillingCycle.selectedItemPosition].second else 0

        var isValid = true
        if (title.isEmpty()) {
            binding.etTitle.error = "Title is required"
            isValid = false
        }
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount is required"
            isValid = false
        }
        if (date.isEmpty()) {
            binding.etDate.error = "Date is required"
            isValid = false
        }

        if (!isValid) return

        val amount = amountStr.toDoubleOrNull() ?: 0.0

        // Budget Check
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val totalSpentThisMonth = AppData.expenses.filter {
            try {
                val d = sdf.parse(it.date)
                val cal = Calendar.getInstance().apply { time = d }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) { false }
        }.sumOf { it.amount }

        if (totalSpentThisMonth + amount > AppData.monthlyBudgetLimit) {
            AlertDialog.Builder(this)
                .setTitle("Budget Limit Reached")
                .setMessage("Adding this expense will exceed your monthly limit of Rs. ${AppData.monthlyBudgetLimit}. Please increase your limit in the dashboard or adjust the amount.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        if (expenseId == -1) {
            val newId = (AppData.expenses.maxByOrNull { it.id }?.id ?: 0) + 1
            val newExpense = Expense(newId, title, amount, selectedCategoryId, date, isRecurring, imageUrl, notes, recurringInterval)
            AppData.expenses.add(newExpense)
        } else {
            val index = AppData.expenses.indexOfFirst { it.id == expenseId }
            if (index != -1) {
                val updated = Expense(expenseId, title, amount, selectedCategoryId, date, isRecurring, imageUrl, notes, recurringInterval)
                AppData.expenses[index] = updated
            }
        }
        
        AppData.save(this)
        Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()
        sendBroadcast(Intent("com.smartbudget.UPDATE_DASHBOARD"))
        setResult(RESULT_OK)
        finish()
    }
}

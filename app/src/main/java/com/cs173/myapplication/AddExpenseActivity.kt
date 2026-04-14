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
        } else {
            // Default date for new expense
            binding.etDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }

        binding.cbRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.llRecurringOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (isChecked) {
                // If checking recurring for the first time, default to active
                binding.cbRecurringActive.isChecked = true
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
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupImageSearch() {
        binding.btnGoogleImages.setOnClickListener {
            val query = binding.etTitle.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "Enter a title first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Use a search query that encourages direct image links
            val url = "https://www.google.com/search?q=${Uri.encode(query + " logo png direct link")}&tbm=isch"
            
            AlertDialog.Builder(this)
                .setTitle("Getting Image URL")
                .setMessage("1. Find image\n2. Long press image\n3. Select 'Open image in new tab'\n4. Copy URL from browser address bar\n5. Paste it here")
                .setPositiveButton("Search") { _, _ ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                .show()
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
            binding.cbRecurringActive.isChecked = it.isRecurringActive
            binding.etImageUrl.setText(it.imageUrl)
            
            val intervalIndex = intervalOptions.indexOfFirst { opt -> opt.second == it.recurringIntervalDays }
            if (intervalIndex != -1) binding.spinnerBillingCycle.setSelection(intervalIndex)

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
        var date = binding.etDate.text.toString().trim()
        val notes = binding.etNotes.text.toString()
        val isRecurring = binding.cbRecurring.isChecked
        val isRecurringActive = binding.cbRecurringActive.isChecked
        val imageUrl = binding.etImageUrl.text.toString().trim()
        val recurringInterval = if (isRecurring) intervalOptions[binding.spinnerBillingCycle.selectedItemPosition].second else 0

        if (title.isEmpty() || amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0

        // "Real-world" logic: If making it active now, ensure the charge is recorded for today
        if (isRecurring && isRecurringActive && expenseId == -1) {
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        if (expenseId == -1) {
            val newId = (AppData.expenses.maxByOrNull { it.id }?.id ?: 0) + 1
            val newExpense = Expense(newId, title, amount, selectedCategoryId, date, isRecurring, imageUrl, notes, recurringInterval, isRecurringActive)
            AppData.expenses.add(newExpense)
        } else {
            val index = AppData.expenses.indexOfFirst { it.id == expenseId }
            if (index != -1) {
                val updated = Expense(expenseId, title, amount, selectedCategoryId, date, isRecurring, imageUrl, notes, recurringInterval, isRecurringActive)
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

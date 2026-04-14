package com.cs173.myapplication.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs173.myapplication.AddExpenseActivity
import com.cs173.myapplication.AppData
import com.cs173.myapplication.adapter.ExpenseAdapter
import com.cs173.myapplication.databinding.FragmentExpensesBinding
import com.cs173.myapplication.model.Expense
import java.text.SimpleDateFormat
import java.util.*

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter
    private var displayedExpenses = ArrayList<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        setupSearch()
        applyFilters() // Initial load
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(displayedExpenses) { expense ->
            showExpenseOptions(expense)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(context)
        binding.rvExpenses.adapter = adapter
    }

    private fun setupFilters() {
        val categories = mutableListOf("All Categories")
        categories.addAll(AppData.categories.map { it.name })
        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = catAdapter

        val dateFilters = arrayOf("All Time", "This Month", "This Week")
        val dateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateFilters)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDateFilter.adapter = dateAdapter

        val sortOptions = arrayOf("Date (Newest)", "Date (Oldest)", "Amount (High to Low)", "Amount (Low to High)")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort?.adapter = sortAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { applyFilters() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.spinnerCategoryFilter.onItemSelectedListener = listener
        binding.spinnerDateFilter.onItemSelectedListener = listener
        binding.spinnerSort?.onItemSelectedListener = listener

        binding.cbThisWeek.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.cbThisMonth.setOnCheckedChangeListener { _, _ -> applyFilters() }
        binding.cbRecurringOnly.setOnCheckedChangeListener { _, _ -> applyFilters() }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                applyFilters()
                return true
            }
        })
    }

    private fun applyFilters() {
        val query = binding.searchView.query.toString().lowercase().trim()
        val selectedCategory = binding.spinnerCategoryFilter.selectedItem?.toString() ?: "All Categories"
        
        var filtered = AppData.expenses.filter { it.title.lowercase().contains(query) }

        if (selectedCategory != "All Categories") {
            val categoryId = AppData.categories.find { it.name == selectedCategory }?.id
            filtered = filtered.filter { it.categoryId == categoryId }
        }

        if (binding.cbRecurringOnly.isChecked) {
            filtered = filtered.filter { it.isRecurring }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()

        if (binding.cbThisMonth.isChecked || binding.spinnerDateFilter.selectedItem == "This Month") {
            filtered = filtered.filter { 
                try {
                    val date = sdf.parse(it.date)
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                } catch (e: Exception) { false }
            }
        }

        if (binding.cbThisWeek.isChecked || binding.spinnerDateFilter.selectedItem == "This Week") {
            filtered = filtered.filter { 
                try {
                    val date = sdf.parse(it.date)
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                } catch (e: Exception) { false }
            }
        }

        // Sorting
        val sortPos = binding.spinnerSort?.selectedItemPosition ?: 0
        filtered = when (sortPos) {
            0 -> filtered.sortedByDescending { it.date }
            1 -> filtered.sortedBy { it.date }
            2 -> filtered.sortedByDescending { it.amount }
            3 -> filtered.sortedBy { it.amount }
            else -> filtered
        }

        displayedExpenses.clear()
        displayedExpenses.addAll(filtered)
        adapter.notifyDataSetChanged()

        if (displayedExpenses.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvEmptyState.text = if (query.isEmpty()) "No expenses yet." else "No results matching \"$query\""
        } else {
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun showExpenseOptions(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle(expense.title)
            .setItems(arrayOf("Edit", "Delete", "Toggle Recurring")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(context, AddExpenseActivity::class.java)
                        intent.putExtra("expenseId", expense.id)
                        startActivity(intent)
                    }
                    1 -> {
                        AppData.expenses.remove(expense)
                        AppData.save(requireContext())
                        applyFilters()
                        Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show()
                        requireContext().sendBroadcast(Intent("com.smartbudget.UPDATE_DASHBOARD"))
                    }
                    2 -> {
                        expense.isRecurring = !expense.isRecurring
                        if (!expense.isRecurring) expense.recurringIntervalDays = 0
                        AppData.save(requireContext())
                        applyFilters()
                        Toast.makeText(context, "Recurring updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        applyFilters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.cs173.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs173.myapplication.AddExpenseActivity
import com.cs173.myapplication.AppData
import com.cs173.myapplication.adapter.ExpenseAdapter
import com.cs173.myapplication.databinding.FragmentSubscriptionsBinding
import com.cs173.myapplication.model.Expense
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter
    private var recurringExpenses = ArrayList<Expense>()

    private val dayOptions = arrayOf(
        "All Recurring" to 999,
        "3 Days" to 3,
        "7 Days" to 7,
        "14 Days" to 14,
        "21 Days" to 21,
        "30 Days" to 30
    )

    private val statusOptions = arrayOf("All", "Active", "Inactive")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        updateList()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            recurringExpenses,
            isSubscriptionView = true,
            onStatusChanged = { updateList() },
            onLongClick = { expense ->
                val intent = Intent(context, AddExpenseActivity::class.java)
                intent.putExtra("expenseId", expense.id)
                startActivity(intent)
            }
        )
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(context)
        binding.rvSubscriptions.adapter = adapter
    }

    private fun setupFilters() {
        val displayNames = dayOptions.map { it.first }
        val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayNames)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubDays.adapter = dayAdapter

        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerActiveFilter.adapter = statusAdapter

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                updateList()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.spinnerSubDays.onItemSelectedListener = listener
        binding.spinnerActiveFilter.onItemSelectedListener = listener
    }

    private fun updateList() {
        val selectedDays = dayOptions[binding.spinnerSubDays.selectedItemPosition].second
        val statusFilter = statusOptions[binding.spinnerActiveFilter.selectedItemPosition]
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val limitCal = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, selectedDays) 
        }

        val filtered = AppData.expenses.filter { it.isRecurring }.filter { expense ->
            // Status Filter
            val matchesStatus = when (statusFilter) {
                "Active" -> expense.isRecurringActive
                "Inactive" -> !expense.isRecurringActive
                else -> true
            }
            if (!matchesStatus) return@filter false

            // Date Filter
            if (selectedDays == 999) return@filter true
            
            try {
                val date = sdf.parse(expense.date) ?: return@filter false
                val nextRenewal = Calendar.getInstance().apply { time = date }
                
                // If the interval is 0, it's not actually recurring in the future
                if (expense.recurringIntervalDays <= 0) return@filter false

                // Calculate the VERY NEXT renewal date that is after the last recorded 'date'
                // and also after or equal to 'now' (since past dates are already paid)
                while (nextRenewal.before(now) || nextRenewal.time == date) {
                    nextRenewal.add(Calendar.DAY_OF_YEAR, expense.recurringIntervalDays)
                }
                
                // Show if this renewal falls within our selected window (e.g., next 7 days)
                nextRenewal.before(limitCal) || nextRenewal.time == limitCal.time
            } catch (e: Exception) { false }
        }

        recurringExpenses.clear()
        recurringExpenses.addAll(filtered)
        adapter.notifyDataSetChanged()

        val totalToPay = recurringExpenses.filter { it.isRecurringActive }.sumOf { it.amount }
        val windowName = dayOptions[binding.spinnerSubDays.selectedItemPosition].first
        binding.tvSubSummary.text = if (selectedDays == 999) {
            "Total Recurring: Rs. $totalToPay"
        } else {
            "Total to pay in $windowName: Rs. $totalToPay"
        }

        binding.tvEmptySubs.visibility = if (recurringExpenses.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

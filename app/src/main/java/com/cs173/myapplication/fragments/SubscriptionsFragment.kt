package com.cs173.myapplication.fragments

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
        "3 Days" to 3,
        "7 Days" to 7,
        "14 Days" to 14,
        "30 Days" to 30,
        "All Recurring" to 999
    )

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
        setupDayFilter()
        updateList()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(recurringExpenses) { expense ->
            val intent = Intent(context, AddExpenseActivity::class.java)
            intent.putExtra("expenseId", expense.id)
            startActivity(intent)
        }
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(context)
        binding.rvSubscriptions.adapter = adapter
    }

    private fun setupDayFilter() {
        val displayNames = dayOptions.map { it.first }
        val dayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayNames)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubDays.adapter = dayAdapter

        binding.spinnerSubDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                updateList()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun updateList() {
        val selectedDays = dayOptions[binding.spinnerSubDays.selectedItemPosition].second
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        
        val limitCal = Calendar.getInstance()
        limitCal.add(Calendar.DAY_OF_YEAR, selectedDays)

        val filtered = AppData.expenses.filter { it.isRecurring }.filter { expense ->
            if (selectedDays == 999) return@filter true
            
            try {
                val date = sdf.parse(expense.date)
                if (date == null) return@filter false
                
                val expCal = Calendar.getInstance().apply { time = date }
                
                // If the expense date is in the past, calculate the "next" occurrence
                while (expCal.before(now) && expense.recurringIntervalDays > 0) {
                    expCal.add(Calendar.DAY_OF_YEAR, expense.recurringIntervalDays)
                }
                
                expCal.after(now) && expCal.before(limitCal)
            } catch (e: Exception) { false }
        }

        recurringExpenses.clear()
        recurringExpenses.addAll(filtered)
        adapter.notifyDataSetChanged()

        val total = recurringExpenses.sumOf { it.amount }
        binding.tvSubSummary.text = "Total to pay in next ${dayOptions[binding.spinnerSubDays.selectedItemPosition].first}: Rs. $total"

        if (recurringExpenses.isEmpty()) {
            binding.tvEmptySubs.visibility = View.VISIBLE
        } else {
            binding.tvEmptySubs.visibility = View.GONE
        }
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

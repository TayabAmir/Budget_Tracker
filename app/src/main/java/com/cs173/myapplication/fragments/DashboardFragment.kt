package com.cs173.myapplication.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs173.myapplication.AppData
import com.cs173.myapplication.adapter.ExpenseAdapter
import com.cs173.myapplication.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBudgetLimit()
        updateUI()

        requireContext().registerReceiver(
            refreshReceiver, 
            IntentFilter("com.smartbudget.UPDATE_DASHBOARD"),
            Context.RECEIVER_EXPORTED
        )
    }

    private fun setupBudgetLimit() {
        binding.etBudgetLimit.setText(AppData.monthlyBudgetLimit.toString())
        binding.etBudgetLimit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val limit = s.toString().toDoubleOrNull() ?: 0.0
                AppData.monthlyBudgetLimit = limit
                AppData.save(requireContext())
                updateUI()
            }
        })
    }

    private fun setupRecentExpenses() {
        val recent = AppData.expenses.takeLast(5).reversed()
        val adapter = ExpenseAdapter(recent)
        binding.rvRecentExpenses.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentExpenses.adapter = adapter
    }

    private fun updateUI() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val totalSpentThisMonth = AppData.expenses.filter {
            try {
                val d = sdf.parse(it.date)
                val cal = Calendar.getInstance().apply { time = d }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) { false }
        }.sumOf { it.amount }

        binding.tvTotalSpent.text = "Rs. $totalSpentThisMonth"

        if (totalSpentThisMonth > AppData.monthlyBudgetLimit) {
            binding.tvBudgetStatus.text = "OVER BUDGET! Limit: Rs. ${AppData.monthlyBudgetLimit}"
            binding.tvBudgetStatus.setTextColor(android.graphics.Color.RED)
        } else {
            val remaining = AppData.monthlyBudgetLimit - totalSpentThisMonth
            binding.tvBudgetStatus.text = "Within Budget (Rs. $remaining left)"
            binding.tvBudgetStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        binding.pieChart.invalidate()
        setupRecentExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireContext().unregisterReceiver(refreshReceiver)
        } catch (e: Exception) {}
        _binding = null
    }
}

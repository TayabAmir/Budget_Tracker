package com.cs173.myapplication.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs173.myapplication.AppData
import com.cs173.myapplication.adapter.ExpenseAdapter
import com.cs173.myapplication.databinding.FragmentDashboardBinding

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
        
        setupRecentExpenses()
        updateUI()

        requireContext().registerReceiver(
            refreshReceiver, 
            IntentFilter("com.smartbudget.UPDATE_DASHBOARD"),
            Context.RECEIVER_EXPORTED
        )
    }

    private fun setupRecentExpenses() {
        val recent = AppData.expenses.takeLast(5).reversed()
        val adapter = ExpenseAdapter(recent)
        binding.rvRecentExpenses.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecentExpenses.adapter = adapter
    }

    private fun updateUI() {
        val totalSpent = AppData.expenses.sumOf { it.amount }
        binding.tvTotalSpent.text = "Rs. $totalSpent"

        val totalLimit = AppData.categories.sumOf { it.limit }
        if (totalSpent > totalLimit) {
            binding.tvBudgetStatus.text = "Over Budget!"
            binding.tvBudgetStatus.setTextColor(android.graphics.Color.RED)
        } else {
            binding.tvBudgetStatus.text = "Within Budget"
            binding.tvBudgetStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        }

        binding.tvActiveSubs.text = AppData.subscriptions.count { it.isActive }.toString()
        binding.tvGoalsProgress.text = AppData.goals.count { it.savedAmount < it.targetAmount }.toString()
        binding.tvTotalCount.text = (AppData.expenses.size + AppData.subscriptions.size).toString()
        
        binding.pieChart.invalidate()
        setupRecentExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(refreshReceiver)
        _binding = null
    }
}

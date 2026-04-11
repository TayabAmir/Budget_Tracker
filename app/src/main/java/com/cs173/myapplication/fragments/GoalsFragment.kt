package com.cs173.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs173.myapplication.AddGoalActivity
import com.cs173.myapplication.AppData
import com.cs173.myapplication.adapter.GoalAdapter
import com.cs173.myapplication.databinding.FragmentGoalsBinding
import com.cs173.myapplication.model.Goal
import java.text.SimpleDateFormat
import java.util.*

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GoalAdapter
    private var displayedGoals = ArrayList<Goal>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        applyFilters()
    }

    private fun setupRecyclerView() {
        adapter = GoalAdapter(displayedGoals) { goal ->
            showAddFundsDialog(goal)
        }
        binding.rvGoals.layoutManager = LinearLayoutManager(context)
        binding.rvGoals.adapter = adapter
    }

    private fun setupFilters() {
        val filters = arrayOf("All Goals", "Completed", "In Progress", "Near Deadline")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filters)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGoalFilter.adapter = filterAdapter

        binding.spinnerGoalFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                applyFilters()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun applyFilters() {
        val filter = binding.spinnerGoalFilter.selectedItem.toString()
        var filtered = AppData.goals.toList()

        filtered = when (filter) {
            "Completed" -> filtered.filter { it.savedAmount >= it.targetAmount }
            "In Progress" -> filtered.filter { it.savedAmount < it.targetAmount }
            "Near Deadline" -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val limit = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time
                filtered.filter { 
                    try {
                        val date = sdf.parse(it.deadline)
                        date != null && date.before(limit) && it.savedAmount < it.targetAmount
                    } catch (e: Exception) { false }
                }
            }
            else -> filtered
        }

        displayedGoals.clear()
        displayedGoals.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun showAddFundsDialog(goal: Goal) {
        val editText = EditText(requireContext())
        editText.hint = "Amount"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        AlertDialog.Builder(requireContext())
            .setTitle("Add Funds to ${goal.name}")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val amount = editText.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    goal.savedAmount += amount
                    adapter.notifyDataSetChanged()
                    Toast.makeText(context, "Added Rs. $amount to ${goal.name}!", Toast.LENGTH_SHORT).show()
                    requireContext().sendBroadcast(Intent("com.smartbudget.UPDATE_DASHBOARD"))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

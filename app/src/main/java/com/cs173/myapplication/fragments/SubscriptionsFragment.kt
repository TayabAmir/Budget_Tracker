package com.cs173.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs173.myapplication.AddSubscriptionActivity
import com.cs173.myapplication.AppData
import com.cs173.myapplication.R
import com.cs173.myapplication.adapter.SubscriptionAdapter
import com.cs173.myapplication.databinding.FragmentSubscriptionsBinding
import com.cs173.myapplication.model.Subscription
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SubscriptionAdapter
    private var displayedSubs = ArrayList<Subscription>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupFilters()
        detectRecurringExpenses()
        applyFilters()
    }

    private fun setupRecyclerViews() {
        // Upcoming bills (next 7 days)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val next7Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }

        val upcoming = AppData.subscriptions.filter { sub ->
            try {
                val date = sdf.parse(sub.nextBillingDate)
                date != null && date.after(now.time) && date.before(next7Days.time)
            } catch (e: Exception) { false }
        }
        
        val upcomingAdapter = SubscriptionAdapter(upcoming) { _, _ -> }
        binding.rvUpcomingBills.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvUpcomingBills.adapter = upcomingAdapter

        // Main List
        adapter = SubscriptionAdapter(displayedSubs) { sub, isActive ->
            sub.isActive = isActive
            Toast.makeText(context, "${sub.name} is now ${if (isActive) "Active" else "Inactive"}", Toast.LENGTH_SHORT).show()
        }
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(context)
        binding.rvSubscriptions.adapter = adapter
    }

    private fun setupFilters() {
        val filters = arrayOf("All", "Active", "Inactive", "Monthly", "Yearly")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filters)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubFilter.adapter = filterAdapter

        binding.spinnerSubFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                applyFilters()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun applyFilters() {
        val filter = binding.spinnerSubFilter.selectedItem.toString()
        var filtered = AppData.subscriptions.toList()

        filtered = when (filter) {
            "Active" -> filtered.filter { it.isActive }
            "Inactive" -> filtered.filter { !it.isActive }
            "Monthly" -> filtered.filter { it.billingCycle == "Monthly" }
            "Yearly" -> filtered.filter { it.billingCycle == "Yearly" }
            else -> filtered
        }

        displayedSubs.clear()
        displayedSubs.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun detectRecurringExpenses() {
        binding.llDetectedSubs.removeAllViews()
        val recurringExpenses = AppData.expenses.filter { it.isRecurring }
        val grouped = recurringExpenses.groupBy { it.title }

        for ((title, list) in grouped) {
            if (AppData.subscriptions.none { it.name.contains(title, true) }) {
                val view = LayoutInflater.from(context).inflate(R.layout.item_detected_sub, binding.llDetectedSubs, false)
                view.findViewById<TextView>(R.id.tv_detected_title).text = "We noticed you pay for $title recurringly"
                view.findViewById<Button>(R.id.btn_add_detected).setOnClickListener {
                    val intent = Intent(context, AddSubscriptionActivity::class.java)
                    intent.putExtra("name", title)
                    intent.putExtra("amount", list.firstOrNull()?.amount ?: 0.0)
                    startActivity(intent)
                }
                binding.llDetectedSubs.addView(view)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.budget_tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budget_tracker.databinding.FragmentDashboardBinding
import com.example.budget_tracker.databinding.ItemCategoryBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val visibleCategories = mutableListOf<Category>()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryAdapter = CategoryAdapter(visibleCategories)
        binding.rvCategories.layoutManager = LinearLayoutManager(context)
        binding.rvCategories.adapter = categoryAdapter
        refreshDashboard()
    }

    override fun onResume() {
        super.onResume()
        if (::categoryAdapter.isInitialized) {
            refreshDashboard()
            categoryAdapter.notifyDataSetChanged()
        }
    }

    private fun refreshDashboard() {
        val totalSpent = DataManager.expenses.sumOf { it.amount }
        binding.tvTotalSpending.text = "$${String.format("%.2f", totalSpent)}"
        visibleCategories.clear()
        visibleCategories.addAll(DataManager.categories.filter { it.spent > 0.0 })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CategoryAdapter(private val categories: List<Category>) :
        RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.binding.tvCategoryName.text = category.name
            holder.binding.tvCategorySpent.text = "$${category.spent} / $${category.limit}"
            val progress = ((category.spent / category.limit) * 100).toInt()
            holder.binding.pbCategory.progress = progress
        }

        override fun getItemCount() = categories.size
    }
}

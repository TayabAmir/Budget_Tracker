package com.example.budget_tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budget_tracker.databinding.FragmentSavingsBinding
import com.example.budget_tracker.databinding.ItemSavingsBinding

class SavingsFragment : Fragment() {

    private var _binding: FragmentSavingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSavings.layoutManager = LinearLayoutManager(context)
        binding.rvSavings.adapter = SavingsAdapter(DataManager.savingsGoals)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SavingsAdapter(private val goals: List<SavingsGoal>) :
        RecyclerView.Adapter<SavingsAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemSavingsBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSavingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val goal = goals[position]
            holder.binding.tvGoalTitle.text = goal.title
            holder.binding.tvSavingsProgress.text = "$${goal.currentAmount} / $${goal.targetAmount}"
            val progress = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
            holder.binding.pbSavings.progress = progress
        }

        override fun getItemCount() = goals.size
    }
}

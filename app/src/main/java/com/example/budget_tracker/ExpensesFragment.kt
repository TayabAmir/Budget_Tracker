package com.example.budget_tracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budget_tracker.databinding.FragmentExpensesBinding
import com.example.budget_tracker.databinding.ItemExpenseBinding

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ExpenseAdapter(DataManager.expenses)
        binding.rvExpenses.layoutManager = LinearLayoutManager(context)
        binding.rvExpenses.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filtered = DataManager.getFilteredExpenses(s.toString())
                adapter.updateData(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            val query = binding.etSearch.text?.toString().orEmpty()
            val filtered = DataManager.getFilteredExpenses(query)
            adapter.updateData(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ExpenseAdapter(private var expenses: List<Expense>) :
        RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

        fun updateData(newExpenses: List<Expense>) {
            this.expenses = newExpenses
            notifyDataSetChanged()
        }

        class ViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val expense = expenses[position]
            holder.binding.tvExpenseTitle.text = expense.title
            holder.binding.tvExpenseCategory.text = expense.category
            holder.binding.tvExpenseAmount.text = "$${expense.amount}"

            val imageUrl = ImageUrlUtils.normalizeGoogleImageUrl(expense.imageUrl)
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.binding.ivExpenseIcon)
            } else {
                holder.binding.ivExpenseIcon.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        }

        override fun getItemCount() = expenses.size
    }
}

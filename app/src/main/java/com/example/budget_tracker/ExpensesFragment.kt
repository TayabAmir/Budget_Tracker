package com.example.budget_tracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private var currentSort = "Date (Newest)"

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

        setupRecyclerView()
        setupSearch()
        setupSortSpinner()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onEdit = { expense ->
                val intent = Intent(context, AddExpenseActivity::class.java)
                intent.putExtra("expense", expense)
                startActivity(intent)
            },
            onDelete = { expense ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense? If it's a subscription, it will also be removed.")
                    .setPositiveButton("Delete") { _, _ ->
                        DataManager.deleteExpense(expense.id)
                        refreshList()
                        Toast.makeText(context, "Expense deleted and balance restored", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvExpenses.layoutManager = LinearLayoutManager(context)
        binding.rvExpenses.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refreshList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Date (Newest)", "Date (Oldest)", "Price (Low to High)", "Price (High to Low)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = adapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = sortOptions[position]
                refreshList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun refreshList() {
        val query = binding.etSearch.text?.toString().orEmpty()
        val filtered = DataManager.getFilteredExpenses(query, currentSort)
        adapter.updateData(filtered)
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ExpenseAdapter(
        private val onEdit: (Expense) -> Unit,
        private val onDelete: (Expense) -> Unit
    ) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

        private var expenses: List<Expense> = emptyList()

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
            holder.binding.tvExpenseAmount.text = "$${String.format("%.2f", expense.amount)}"

            holder.binding.btnEdit.setOnClickListener { onEdit(expense) }
            holder.binding.btnDelete.setOnClickListener { onDelete(expense) }

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

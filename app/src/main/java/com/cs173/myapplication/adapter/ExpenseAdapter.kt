package com.cs173.myapplication.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cs173.myapplication.AppData
import com.cs173.myapplication.R
import com.cs173.myapplication.model.Expense
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val isSubscriptionView: Boolean = false,
    private val onStatusChanged: () -> Unit = {},
    private val onLongClick: (Expense) -> Unit = {}
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.iv_expense_thumbnail)
        val tvTitle: TextView = view.findViewById(R.id.tv_expense_title)
        val tvDate: TextView = view.findViewById(R.id.tv_expense_date)
        val tvAmount: TextView = view.findViewById(R.id.tv_expense_amount)
        val tvCategoryBadge: TextView = view.findViewById(R.id.tv_category_badge)
        val cbActiveStatus: CheckBox = view.findViewById(R.id.cb_active_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvTitle.text = expense.title
        holder.tvAmount.text = "Rs. ${expense.amount}"
        holder.tvDate.text = expense.date

        val category = AppData.categories.find { it.id == expense.categoryId }
        category?.let {
            holder.tvCategoryBadge.text = it.name
            holder.tvCategoryBadge.setBackgroundColor(Color.parseColor(it.colorHex))
        }

        if (expense.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(expense.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        if (isSubscriptionView && expense.isRecurring) {
            holder.cbActiveStatus.visibility = View.VISIBLE
            holder.cbActiveStatus.setOnCheckedChangeListener(null)
            holder.cbActiveStatus.isChecked = expense.isRecurringActive
            
            holder.cbActiveStatus.setOnClickListener {
                val isChecked = holder.cbActiveStatus.isChecked
                if (isChecked) {
                    // Activate: Immediately "cut the money" by setting date to today
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    expense.date = sdf.format(Date())
                    expense.isRecurringActive = true
                    Toast.makeText(holder.itemView.context, "Subscription reactivated and charged for today", Toast.LENGTH_SHORT).show()
                } else {
                    // Deactivate: Stop future recurring, but keep current month record
                    expense.isRecurringActive = false
                    Toast.makeText(holder.itemView.context, "Cancelled. Won't recur next cycle.", Toast.LENGTH_SHORT).show()
                }
                AppData.save(holder.itemView.context)
                onStatusChanged()
                notifyItemChanged(position)
            }
        } else {
            holder.cbActiveStatus.visibility = View.GONE
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(expense)
            true
        }
    }

    override fun getItemCount() = expenses.size

    fun updateList(newList: List<Expense>) {
        expenses = newList
        notifyDataSetChanged()
    }
}

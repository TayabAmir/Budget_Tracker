package com.cs173.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cs173.myapplication.R
import com.cs173.myapplication.model.Subscription

class SubscriptionAdapter(
    private var subscriptions: List<Subscription>,
    private val onToggleActive: (Subscription, Boolean) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    class SubscriptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_sub_icon)
        val tvName: TextView = view.findViewById(R.id.tv_sub_name)
        val tvCycle: TextView = view.findViewById(R.id.tv_billing_cycle)
        val tvNextBilling: TextView = view.findViewById(R.id.tv_next_billing)
        val tvAmount: TextView = view.findViewById(R.id.tv_sub_amount)
        val cbActive: CheckBox = view.findViewById(R.id.cb_is_active)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val sub = subscriptions[position]
        holder.tvName.text = sub.name
        holder.tvAmount.text = "Rs. ${sub.amount}"
        holder.tvCycle.text = sub.billingCycle
        holder.tvNextBilling.text = "Next: ${sub.nextBillingDate}"
        
        holder.cbActive.setOnCheckedChangeListener(null)
        holder.cbActive.isChecked = sub.isActive
        
        if (sub.iconUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(sub.iconUrl).into(holder.ivIcon)
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_month)
        }

        holder.cbActive.setOnCheckedChangeListener { _, isChecked ->
            onToggleActive(sub, isChecked)
        }
    }

    override fun getItemCount() = subscriptions.size

    fun updateList(newList: List<Subscription>) {
        subscriptions = newList
        notifyDataSetChanged()
    }
}

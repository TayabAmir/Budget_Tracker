package com.example.budget_tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budget_tracker.databinding.FragmentSubscriptionsBinding
import com.example.budget_tracker.databinding.ItemSubscriptionBinding

class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSubscriptions.layoutManager = LinearLayoutManager(context)
        binding.rvSubscriptions.adapter = SubscriptionAdapter(DataManager.subscriptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SubscriptionAdapter(private val subscriptions: List<Subscription>) :
        RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val subscription = subscriptions[position]
            holder.binding.tvSubName.text = subscription.name
            holder.binding.tvSubBilling.text = "Next: ${subscription.nextBillingDate} (${subscription.billingCycle})"
            holder.binding.tvSubPrice.text = "$${subscription.price}"
        }

        override fun getItemCount() = subscriptions.size
    }
}

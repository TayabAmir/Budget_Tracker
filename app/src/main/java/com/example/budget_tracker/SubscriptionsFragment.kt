package com.example.budget_tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budget_tracker.databinding.FragmentSubscriptionsBinding
import com.example.budget_tracker.databinding.ItemSubscriptionBinding

class SubscriptionsFragment : Fragment() {

    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SubscriptionAdapter

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

        setupRecyclerView()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = SubscriptionAdapter { subId ->
            DataManager.toggleSubscriptionActive(subId)
            refreshList()
        }
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(context)
        binding.rvSubscriptions.adapter = adapter
    }

    private fun setupFilters() {
        val dayOptions = arrayOf("All", "1 Day", "3 Days", "7 Days", "15 Days", "30 Days")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dayOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDays.adapter = spinnerAdapter

        binding.cbActiveOnly.setOnCheckedChangeListener { _, _ -> refreshList() }
        binding.spinnerDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun refreshList() {
        val activeOnly = if (binding.cbActiveOnly.isChecked) true else null
        val daysText = binding.spinnerDays.selectedItem.toString()
        val daysLimit = if (daysText == "All") null else daysText.split(" ")[0].toInt()
        
        val filtered = DataManager.getSubscriptionsFiltered(activeOnly, daysLimit)
        adapter.updateData(filtered)
    }

    override fun onResume() {
        super.onResume()
        DataManager.checkAndProcessSubscriptions() // Check for any due subscriptions
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SubscriptionAdapter(private val onToggle: (Int) -> Unit) :
        RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {

        private var subscriptions: List<Subscription> = emptyList()

        fun updateData(newSubs: List<Subscription>) {
            this.subscriptions = newSubs
            notifyDataSetChanged()
        }

        class ViewHolder(val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val subscription = subscriptions[position]
            holder.binding.tvSubName.text = subscription.name
            holder.binding.tvSubBilling.text = "Next: ${subscription.nextBillingDate} (${subscription.billingCycle})"
            holder.binding.tvSubPrice.text = "$${String.format("%.2f", subscription.price)}"
            
            holder.binding.switchActive.setOnCheckedChangeListener(null)
            holder.binding.switchActive.isChecked = subscription.isActive
            holder.binding.switchActive.setOnCheckedChangeListener { _, _ ->
                onToggle(subscription.id)
            }

            val imageUrl = ImageUrlUtils.normalizeGoogleImageUrl(subscription.imageUrl)
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_slideshow)
                    .error(android.R.drawable.ic_menu_slideshow)
                    .into(holder.binding.ivSubIcon)
            } else {
                holder.binding.ivSubIcon.setImageResource(android.R.drawable.ic_menu_slideshow)
            }
        }

        override fun getItemCount() = subscriptions.size
    }
}

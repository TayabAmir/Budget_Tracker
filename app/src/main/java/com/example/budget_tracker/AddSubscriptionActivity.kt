package com.example.budget_tracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budget_tracker.databinding.ActivityAddSubscriptionBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddSubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cycles = listOf("Monthly", "Yearly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cycles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBillingCycle.adapter = adapter

        binding.btnSaveSubscription.setOnClickListener {
            saveSubscription()
        }
    }

    private fun saveSubscription() {
        val name = binding.etSubscriptionName.text?.toString()?.trim().orEmpty()
        val priceValue = binding.etSubscriptionPrice.text?.toString()?.trim().orEmpty()
        val nextBillingDate = binding.etNextBillingDate.text?.toString()?.trim().orEmpty()
        val billingCycle = binding.spinnerBillingCycle.selectedItem?.toString().orEmpty()
        val imageUrl = binding.etImageUrl.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            Toast.makeText(this, getString(R.string.subscription_name_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (priceValue.isBlank()) {
            Toast.makeText(this, getString(R.string.subscription_price_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (nextBillingDate.isBlank()) {
            Toast.makeText(this, getString(R.string.subscription_date_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceValue.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            Toast.makeText(this, getString(R.string.subscription_price_invalid_error), Toast.LENGTH_SHORT).show()
            return
        }

        val parsedDate = parseStrictDate(nextBillingDate)
        if (parsedDate == null) {
            Toast.makeText(this, getString(R.string.invalid_date_format), Toast.LENGTH_SHORT).show()
            return
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (!parsedDate.after(today)) {
            Toast.makeText(this, getString(R.string.subscription_future_date_error), Toast.LENGTH_SHORT).show()
            return
        }

        val normalizedImageUrl = if (imageUrl.isBlank()) {
            null
        } else {
            ImageUrlUtils.normalizeGoogleImageUrl(imageUrl)
        }

        if (imageUrl.isNotBlank() && normalizedImageUrl == null) {
            Toast.makeText(this, getString(R.string.invalid_google_image_url), Toast.LENGTH_SHORT).show()
            return
        }

        val newSubscription = Subscription(
            id = DataManager.subscriptions.size + 1,
            name = name,
            price = price,
            billingCycle = billingCycle,
            nextBillingDate = nextBillingDate,
            nextBillingTimestamp = parsedDate.time,
            imageUrl = normalizedImageUrl
        )

        DataManager.addSubscription(newSubscription)
        Toast.makeText(this, getString(R.string.subscription_saved), Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    private fun parseStrictDate(value: String) = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }.parse(value)
    }.getOrNull()
}

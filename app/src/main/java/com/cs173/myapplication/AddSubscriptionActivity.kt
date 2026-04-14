package com.cs173.myapplication

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs173.myapplication.databinding.ActivityAddSubscriptionBinding
import com.cs173.myapplication.model.Expense
import com.cs173.myapplication.model.Subscription
import com.cs173.myapplication.receiver.BillReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

class AddSubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupDatePicker()

        // Pre-fill if coming from detection
        intent.getStringExtra("name")?.let { binding.etSubName.setText(it) }
        intent.getDoubleExtra("amount", 0.0).takeIf { it > 0 }?.let { binding.etSubAmount.setText(it.toString()) }

        binding.btnSaveSub.setOnClickListener {
            saveSubscription()
        }
    }

    private fun setupSpinners() {
        val cycles = arrayOf("Weekly", "Monthly", "Yearly")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cycles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBillingCycle.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etNextBilling.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                binding.etNextBilling.setText(String.format("%d-%02d-%02d", year, month + 1, day))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun saveSubscription() {
        val name = binding.etSubName.text.toString()
        val amountStr = binding.etSubAmount.text.toString()
        val cycle = binding.spinnerBillingCycle.selectedItem.toString()
        val nextDate = binding.etNextBilling.text.toString()
        val iconUrl = binding.etSubIconUrl.text.toString()
        val isActive = binding.cbSubActive.isChecked

        if (name.isEmpty() || amountStr.isEmpty() || nextDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val newId = (AppData.subscriptions.maxByOrNull { it.id }?.id ?: 0) + 1
        val sub = Subscription(newId, name, amount, cycle, nextDate, iconUrl, isActive)
        
        AppData.subscriptions.add(sub)
        
        // Also add as a recurring expense automatically
        val expId = (AppData.expenses.maxByOrNull { it.id }?.id ?: 0) + 1
        val recurringExpense = Expense(expId, name, amount, 10, nextDate, true, iconUrl, "Subscription: $cycle")
        AppData.expenses.add(recurringExpense)
        
        scheduleReminder(sub)
        
        // Save to persistent storage
        AppData.save(this)

        Toast.makeText(this, "Subscription saved and added to expenses!", Toast.LENGTH_SHORT).show()
        sendBroadcast(Intent("com.smartbudget.UPDATE_DASHBOARD"))
        finish()
    }

    private fun scheduleReminder(sub: Subscription) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = sdf.parse(sub.nextBillingDate)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.DAY_OF_YEAR, -1) // 1 day before
                calendar.set(Calendar.HOUR_OF_DAY, 9) // 9 AM
                
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, BillReminderReceiver::class.java).apply {
                        action = "com.smartbudget.BILL_REMINDER"
                        putExtra("subName", sub.name)
                        putExtra("amount", sub.amount)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        this, sub.id, intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}

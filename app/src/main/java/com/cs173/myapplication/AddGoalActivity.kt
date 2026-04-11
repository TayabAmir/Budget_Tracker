package com.cs173.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs173.myapplication.databinding.ActivityAddGoalBinding
import com.cs173.myapplication.model.Goal
import java.util.*

class AddGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddGoalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }
    }

    private fun setupDatePicker() {
        binding.et_goal_deadline.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                binding.et_goal_deadline.setText(String.format("%d-%02d-%02d", year, month + 1, day))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun saveGoal() {
        val name = binding.et_goal_name.text.toString()
        val targetStr = binding.et_target_amount.text.toString()
        val savedStr = binding.et_saved_amount.text.toString()
        val deadline = binding.et_goal_deadline.text.toString()
        val imageUrl = binding.et_goal_image_url.text.toString()

        if (name.isEmpty() || targetStr.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val target = targetStr.toDoubleOrNull() ?: 0.0
        val saved = savedStr.toDoubleOrNull() ?: 0.0
        val newId = (AppData.goals.maxByOrNull { it.id }?.id ?: 0) + 1
        
        AppData.goals.add(Goal(newId, name, target, saved, deadline, imageUrl))

        Toast.makeText(this, "Goal created! Keep saving!", Toast.LENGTH_SHORT).show()
        sendBroadcast(Intent("com.smartbudget.UPDATE_DASHBOARD"))
        finish()
    }
}

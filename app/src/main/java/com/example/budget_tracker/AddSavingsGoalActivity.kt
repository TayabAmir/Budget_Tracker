package com.example.budget_tracker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budget_tracker.databinding.ActivityAddSavingsGoalBinding

class AddSavingsGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSavingsGoalBinding
    private var editingGoalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSavingsGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingGoalId = intent.getIntExtra("GOAL_ID", -1)
        if (editingGoalId != -1) {
            setupEditMode()
        }

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        binding.btnDeleteGoal.setOnClickListener {
            deleteGoal()
        }
    }

    private fun setupEditMode() {
        val goal = DataManager.savingsGoals.find { it.id == editingGoalId }
        if (goal != null) {
            binding.etGoalTitle.setText(goal.title)
            binding.etTargetAmount.setText(goal.targetAmount.toString())
            binding.etCurrentAmount.setText(goal.currentAmount.toString())
            binding.etImageUrl.setText(goal.imageUrl ?: "")
            binding.btnDeleteGoal.visibility = View.VISIBLE
            supportActionBar?.title = "Edit Savings Goal"
        }
    }

    private fun saveGoal() {
        val title = binding.etGoalTitle.text?.toString()?.trim().orEmpty()
        val targetAmountValue = binding.etTargetAmount.text?.toString()?.trim().orEmpty()
        val currentAmountValue = binding.etCurrentAmount.text?.toString()?.trim().orEmpty()
        val imageUrl = binding.etImageUrl.text?.toString()?.trim().orEmpty()

        if (title.isBlank()) {
            Toast.makeText(this, getString(R.string.goal_title_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (targetAmountValue.isBlank()) {
            Toast.makeText(this, getString(R.string.goal_target_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentAmountValue.isBlank()) {
            Toast.makeText(this, getString(R.string.goal_current_required_error), Toast.LENGTH_SHORT).show()
            return
        }

        val targetAmount = targetAmountValue.toDoubleOrNull()
        val currentAmount = currentAmountValue.toDoubleOrNull()

        if (targetAmount == null || targetAmount <= 0.0) {
            Toast.makeText(this, getString(R.string.goal_target_invalid_error), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentAmount == null || currentAmount < 0.0) {
            Toast.makeText(this, getString(R.string.goal_current_invalid_error), Toast.LENGTH_SHORT).show()
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

        if (editingGoalId != -1) {
            val updatedGoal = SavingsGoal(
                id = editingGoalId,
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                imageUrl = normalizedImageUrl
            )
            DataManager.updateSavingsGoal(updatedGoal)
            Toast.makeText(this, "Savings goal updated", Toast.LENGTH_SHORT).show()
        } else {
            val newGoal = SavingsGoal(
                id = (DataManager.savingsGoals.maxOfOrNull { it.id } ?: 0) + 1,
                title = title,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                imageUrl = normalizedImageUrl
            )
            DataManager.addSavingsGoal(newGoal)
            Toast.makeText(this, getString(R.string.savings_goal_saved), Toast.LENGTH_SHORT).show()
        }
        
        setResult(RESULT_OK)
        finish()
    }

    private fun deleteGoal() {
        if (editingGoalId != -1) {
            DataManager.deleteSavingsGoal(editingGoalId)
            Toast.makeText(this, "Savings goal deleted", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }
}

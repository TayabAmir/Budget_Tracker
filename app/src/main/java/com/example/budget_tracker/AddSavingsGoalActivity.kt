package com.example.budget_tracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budget_tracker.databinding.ActivityAddSavingsGoalBinding

class AddSavingsGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSavingsGoalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSavingsGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
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

        if (currentAmount > targetAmount) {
            Toast.makeText(this, getString(R.string.goal_current_exceeds_target_error), Toast.LENGTH_SHORT).show()
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

        val newGoal = SavingsGoal(
            id = DataManager.savingsGoals.size + 1,
            title = title,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            imageUrl = normalizedImageUrl
        )

        DataManager.addSavingsGoal(newGoal)
        Toast.makeText(this, getString(R.string.savings_goal_saved), Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}

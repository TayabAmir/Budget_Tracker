package com.example.budget_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budget_tracker.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            attemptSignUp()
        }

        binding.tvGoToSignIn.setOnClickListener {
            finish()
        }
    }

    private fun attemptSignUp() {
        val username = binding.etFullName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        var hasError = false

        if (username.isBlank()) {
            binding.tilFullName.error = getString(R.string.username_required_error)
            hasError = true
        } else if (username.any { it.isWhitespace() }) {
            binding.tilFullName.error = getString(R.string.username_no_spaces_error)
            hasError = true
        }

        if (email.isBlank()) {
            binding.tilEmail.error = getString(R.string.email_required_error)
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            hasError = true
        }

        if (password.isBlank()) {
            binding.tilPassword.error = getString(R.string.password_required_error)
            hasError = true
        } else if (password.length <= 6) {
            binding.tilPassword.error = getString(R.string.password_length_error)
            hasError = true
        }

        if (confirmPassword.isBlank()) {
            binding.tilConfirmPassword.error = getString(R.string.confirm_password_required_error)
            hasError = true
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.password_mismatch)
            hasError = true
        }

        if (hasError) {
            return
        }

        val signUpError = DataManager.signUp(username, email, password)
        if (signUpError != null) {
            Toast.makeText(this, signUpError, Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, getString(R.string.account_created_success), Toast.LENGTH_SHORT).show()
        openMainScreen()
    }

    private fun openMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

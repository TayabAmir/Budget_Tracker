package com.example.budget_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budget_tracker.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DataManager.isSignedIn()) {
            openMainScreen()
            return
        }

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            attemptSignIn()
        }

        binding.tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun attemptSignIn() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        var hasError = false

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

        if (hasError) {
            return
        }

        val signInError = DataManager.signIn(email, password)
        if (signInError != null) {
            Toast.makeText(this, getString(R.string.invalid_email_or_password), Toast.LENGTH_SHORT).show()
            return
        }

        openMainScreen()
    }

    private fun openMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

package com.example.budget_tracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Splash screen delay
        Handler(Looper.getMainLooper()).postDelayed({
            val destination = if (DataManager.isSignedIn()) {
                MainActivity::class.java
            } else {
                SignInActivity::class.java
            }
            val intent = Intent(this, destination)
            startActivity(intent)
            finish()
        }, 2000)
    }
}

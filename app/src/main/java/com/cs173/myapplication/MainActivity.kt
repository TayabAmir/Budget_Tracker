package com.cs173.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.cs173.myapplication.databinding.ActivityMainBinding
import com.cs173.myapplication.fragments.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AppData with persistent storage
        AppData.init(this)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        
        // Update Sidebar Header
        val headerView = binding.navView.getHeaderView(0)
        val tvBalance = headerView.findViewById<TextView>(R.id.total_balance)
        updateSidebarBalance(tvBalance)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> replaceFragment(DashboardFragment())
                R.id.nav_expenses -> replaceFragment(ExpensesFragment())
                R.id.nav_subscriptions -> replaceFragment(SubscriptionsFragment())
                R.id.nav_goals -> replaceFragment(GoalsFragment())
                R.id.nav_reports -> replaceFragment(ReportsFragment())
            }
            true
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        // Default Fragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun updateSidebarBalance(tvBalance: TextView) {
        val totalExp = AppData.expenses.sumOf { it.amount }
        tvBalance.text = "Total Spent: Rs. $totalExp"
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings updated", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_notifications -> {
                Toast.makeText(this, "Notification preferences saved", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_export -> {
                // Navigate to Reports where export buttons are located
                binding.bottomNavigation.selectedItemId = R.id.nav_reports
                replaceFragment(ReportsFragment())
                Toast.makeText(this, "Use buttons below to export", Toast.LENGTH_LONG).show()
            }
            R.id.nav_about -> {
                android.app.AlertDialog.Builder(this)
                    .setTitle("About SmartBudget")
                    .setMessage("SmartBudget v1.0\nDeveloped for Budget & Subscription Tracking.\n\nAuthor: 2023-CS-173")
                    .setPositiveButton("OK", null)
                    .show()
            }
            R.id.nav_logout -> {
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        // Refresh balance if it changed
        val headerView = binding.navView.getHeaderView(0)
        val tvBalance = headerView.findViewById<TextView>(R.id.total_balance)
        updateSidebarBalance(tvBalance)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

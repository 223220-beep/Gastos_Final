package com.gastosapp.features.expenses.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastosapp.databinding.ActivityDashboardBinding
import com.gastosapp.features.auth.data.UserEntity
import com.gastosapp.features.auth.presentation.WelcomeActivity
import com.gastosapp.features.profile.presentation.ProfileActivity
import com.gastosapp.features.analytics.presentation.AnalyticsActivity
import com.gastosapp.features.reminders.presentation.RemindersActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(), ExpenseAdapter.OnExpenseClickListener {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter
    private var currentUser: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getSerializableExtra("user") as? UserEntity
        if (currentUser == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupObservers()
    }

    private fun initViews() {
        binding.tvUserName.text = currentUser?.name ?: "Usuario"
        
        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user", currentUser)
            startActivity(intent)
        }

        binding.btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.btnViewAll.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
        }

        binding.btnAnalytics.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }

        binding.btnReminders.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }

        binding.btnBudgets.setOnClickListener {
            startActivity(Intent(this, com.gastosapp.features.analytics.presentation.BudgetActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(this)
        binding.rvRecentExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvRecentExpenses.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                if (!state.isLoading) {
                    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
                    binding.tvTotalMonth.text = format.format(state.totalMonthly)
                    
                    if (state.recentExpenses.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.rvRecentExpenses.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.rvRecentExpenses.visibility = View.VISIBLE
                        adapter.setExpenses(state.recentExpenses)
                    }
                }
            }
        }
    }

    override fun onExpenseClick(expense: com.gastosapp.features.expenses.data.ExpenseEntity) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra("expense_id", expense.id.toString())
        startActivity(intent)
    }
}

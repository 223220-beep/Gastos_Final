package com.gastosapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastosapp.adapter.ExpenseAdapter
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.data.local.UserEntity
import com.gastosapp.databinding.ActivityDashboardBinding
import com.gastosapp.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
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
        observeViewModel()
    }

    private fun initViews() {
        binding.tvUserName.text = currentUser?.name
        
        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user", currentUser)
            startActivity(intent)
        }

        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            intent.putExtra("user_id", currentUser?.id.toString())
            startActivity(intent)
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
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(this)
        binding.rvRecentExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvRecentExpenses.adapter = adapter
    }

    private fun observeViewModel() {
        binding.progressBar.visibility = View.VISIBLE

        viewModel.getRecentExpenses().observe(this) { expenses ->
            binding.progressBar.visibility = View.GONE
            if (expenses != null && expenses.isNotEmpty()) {
                adapter.setExpenses(expenses)
                binding.emptyView.visibility = View.GONE
                binding.rvRecentExpenses.visibility = View.VISIBLE
            } else {
                binding.emptyView.visibility = View.VISIBLE
                binding.rvRecentExpenses.visibility = View.GONE
            }
        }

        viewModel.getTotalMonthlyExpenses().observe(this) { total ->
            binding.tvTotalMonth.text = formatCurrency(total ?: 0.0)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return format.format(amount)
    }

    override fun onExpenseClick(expense: ExpenseEntity) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra("expense_id", expense.id)
        startActivity(intent)
    }
}

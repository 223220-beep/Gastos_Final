package com.gastosapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastosapp.adapter.ExpenseAdapter
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.databinding.ActivityExpensesListBinding
import com.gastosapp.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class ExpensesListActivity : AppCompatActivity(), ExpenseAdapter.OnExpenseClickListener {

    private lateinit var binding: ActivityExpensesListBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter
    private var allExpenses: List<ExpenseEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExpenses(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(this)
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun observeViewModel() {
        binding.progressBar.visibility = View.VISIBLE

        viewModel.getAllExpenses().observe(this) { expenses ->
            binding.progressBar.visibility = View.GONE
            if (expenses != null) {
                allExpenses = expenses
                adapter.setExpenses(expenses)
                updateSummary(expenses)
                updateEmptyState(expenses.isEmpty())
            }
        }
    }

    private fun filterExpenses(query: String) {
        val filtered = if (query.trim().isEmpty()) {
            allExpenses
        } else {
            val lowerQuery = query.lowercase().trim()
            allExpenses.filter {
                it.description.lowercase().contains(lowerQuery) ||
                        it.category.lowercase().contains(lowerQuery)
            }
        }
        adapter.setExpenses(filtered)
        updateSummary(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun updateSummary(expenses: List<ExpenseEntity>) {
        val total = expenses.sumOf { it.amount }
        binding.tvTotal.text = formatCurrency(total)

        val count = expenses.size
        binding.tvCount.text = "$count ${if (count == 1) "gasto" else "gastos"}"
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyView.visibility = View.VISIBLE
            binding.rvExpenses.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.rvExpenses.visibility = View.VISIBLE
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

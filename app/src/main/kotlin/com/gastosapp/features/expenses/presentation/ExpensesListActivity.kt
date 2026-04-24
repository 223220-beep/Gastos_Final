package com.gastosapp.features.expenses.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastosapp.databinding.ActivityExpensesListBinding
import com.gastosapp.features.expenses.data.ExpenseEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class ExpensesListActivity : AppCompatActivity(), ExpenseAdapter.OnExpenseClickListener {

    private lateinit var binding: ActivityExpensesListBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupRecyclerView()
        setupObservers()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.etSearch.addTextChangedListener { text ->
            filterExpenses(text.toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(this)
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                if (!state.isLoading) {
                    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
                    binding.tvTotal.text = format.format(state.allExpenses.sumOf { it.amount })
                    binding.tvCount.text = "${state.allExpenses.size} gastos"
                    
                    if (state.allExpenses.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.rvExpenses.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.rvExpenses.visibility = View.VISIBLE
                        adapter.setExpenses(state.allExpenses)
                    }
                }
            }
        }
    }

    private fun filterExpenses(query: String) {
        val filtered = viewModel.uiState.value.allExpenses.filter {
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
        adapter.setExpenses(filtered)
        
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        binding.tvTotal.text = format.format(filtered.sumOf { it.amount })
        binding.tvCount.text = "${filtered.size} gastos"
        
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvExpenses.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onExpenseClick(expense: ExpenseEntity) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra("expense_id", expense.id.toString())
        startActivity(intent)
    }
}

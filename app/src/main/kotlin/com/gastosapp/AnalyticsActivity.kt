package com.gastosapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.databinding.ActivityAnalyticsBinding
import com.gastosapp.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.getTotalMonthlyExpenses().observe(this) { total ->
            binding.tvCurrentMonthTotal.text = formatCurrency(total ?: 0.0)
        }

        viewModel.getAllExpenses().observe(this) { expenses ->
            if (expenses != null) {
                setupLineChart(expenses)
                setupPieChart(expenses)
            }
        }
    }

    private fun setupLineChart(expenses: List<ExpenseEntity>) {
        val entries = ArrayList<Entry>()

        // Simplified grouping logic
        val currentMonthTotal = expenses.sumOf { it.amount }

        entries.add(Entry(0f, 1500f))
        entries.add(Entry(1f, 2400f))
        entries.add(Entry(2f, 1800f))
        entries.add(Entry(3f, currentMonthTotal.toFloat()))

        val dataSet = LineDataSet(entries, "Gastos Mensuales").apply {
            color = Color.parseColor("#3b82f6")
            valueTextColor = Color.GRAY
            lineWidth = 2f
            setCircleColor(Color.parseColor("#3b82f6"))
            setDrawFilled(true)
            fillAlpha = 50
            fillColor = Color.parseColor("#3b82f6")
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            val months = arrayOf("Ene", "Feb", "Mar", "Abr")
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(months)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    private fun setupPieChart(expenses: List<ExpenseEntity>) {
        val categoryTotals = HashMap<String, Float>()

        for (expense in expenses) {
            val category = expense.category
            val amount = expense.amount.toFloat()
            categoryTotals[category] = (categoryTotals[category] ?: 0f) + amount
        }

        val pieEntries = ArrayList<PieEntry>()
        for ((category, total) in categoryTotals) {
            pieEntries.add(PieEntry(total, category))
        }

        val dataSet = PieDataSet(pieEntries, "").apply {
            colors = intArrayOf(
                Color.parseColor("#10b981"),
                Color.parseColor("#3b82f6"),
                Color.parseColor("#8b5cf6"),
                Color.parseColor("#ef4444"),
                Color.parseColor("#eab308"),
                Color.parseColor("#6b7280")
            ).toList()
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            holeRadius = 50f
            transparentCircleRadius = 55f
            animateY(1000)
            invalidate()
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return format.format(amount)
    }
}

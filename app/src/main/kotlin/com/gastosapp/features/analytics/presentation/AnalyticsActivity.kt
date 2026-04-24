package com.gastosapp.features.analytics.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.gastosapp.core.presentation.theme.GastosAppTheme
import com.gastosapp.features.expenses.data.ExpenseEntity
import com.gastosapp.features.expenses.presentation.ExpenseViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class AnalyticsActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GastosAppTheme {
                AnalyticsScreen(viewModel = viewModel, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: ExpenseViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análisis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total del Mes", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatCurrency(uiState.totalMonthly),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Historial", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LineChartCompose(expenses = uiState.allExpenses)

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Categorías", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            PieChartCompose(expenses = uiState.allExpenses)
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun LineChartCompose(expenses: List<ExpenseEntity>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = true
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
            }
        },
        update = { chart ->
            val entries = ArrayList<Entry>()
            // Lógica simplificada
            entries.add(Entry(0f, 1500f))
            entries.add(Entry(1f, 2400f))
            entries.add(Entry(2f, 1800f))
            entries.add(Entry(3f, expenses.sumOf { it.amount }.toFloat()))

            val dataSet = LineDataSet(entries, "Historial").apply {
                color = android.graphics.Color.BLUE
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = android.graphics.Color.BLUE
                fillAlpha = 30
            }

            chart.data = LineData(dataSet)
            val months = arrayOf("Ene", "Feb", "Mar", "Hoy")
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
            chart.animateX(1000)
            chart.invalidate()
        }
    )
}

@Composable
fun PieChartCompose(expenses: List<ExpenseEntity>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                holeRadius = 40f
            }
        },
        update = { chart ->
            val categoryTotals = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { exp -> exp.amount }.toFloat() }

            val entries = categoryTotals.map { PieEntry(it.value, it.key) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(
                    android.graphics.Color.GREEN,
                    android.graphics.Color.BLUE,
                    android.graphics.Color.RED,
                    android.graphics.Color.MAGENTA,
                    android.graphics.Color.YELLOW
                )
                valueTextSize = 12f
            }

            chart.data = PieData(dataSet)
            chart.animateY(1000)
            chart.invalidate()
        }
    )
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}

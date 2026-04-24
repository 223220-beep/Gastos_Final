package com.gastosapp.features.expenses.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gastosapp.features.expenses.data.ExpenseEntity
import com.gastosapp.core.presentation.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    userName: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpensesList: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    viewModel: ExpenseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Blue600, Blue800)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                // Top Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bienvenido de nuevo",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = userName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Total Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total del mes",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.isLoading) "..." else formatCurrency(uiState.totalMonthly),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // Quick Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Agregar",
                            icon = Icons.Default.Add,
                            color = Blue600,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAddExpense
                        )
                        ActionCard(
                            title = "Gastos",
                            icon = Icons.Default.List,
                            color = Gray800,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToExpensesList
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Análisis",
                            icon = Icons.Default.BarChart,
                            color = Gray800,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAnalytics
                        )
                        ActionCard(
                            title = "Alarmas",
                            icon = Icons.Default.Notifications,
                            color = Gray800,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReminders
                        )
                    }
                    ActionCard(
                        title = "Metas de Presupuesto",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = Gray800,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToBudgets
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Recent Expenses Title
            item {
                Text(
                    text = "Gastos recientes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expenses List
            if (uiState.isLoading) {
                items(3) {
                    LoadingExpenseCard()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else if (uiState.recentExpenses.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(uiState.recentExpenses) { expense ->
                    ExpenseCard(expense = expense, onClick = {})
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ExpenseCard(expense: ExpenseEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(category = expense.category)
                    Text(text = "•", color = Gray500)
                    Text(text = formatDate(expense.timestamp), fontSize = 12.sp, color = Gray500)
                }
            }

            Text(
                text = formatCurrency(expense.amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    val (bgColor, textColor) = getCategoryColors(category)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = category, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
fun LoadingExpenseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .background(Gray300.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(12.dp)
                    .background(Gray300.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Gray300
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "No hay gastos registrados", color = Gray600)
            Text(
                text = "Agrega tu primer gasto para comenzar",
                fontSize = 14.sp,
                color = Gray500
            )
        }
    }
}

fun getCategoryColors(category: String): Pair<Color, Color> {
    // Definir colores de categorías localmente o importarlos
    val Blue100 = Color(0xFFDBEAFE)
    val Blue700 = Color(0xFF1D4ED8)
    val Green100 = Color(0xFFDCFCE7)
    val Green700 = Color(0xFF15803D)
    val Purple100 = Color(0xFFF3E8FF)
    val Purple700 = Color(0xFF7C3AED)
    val Red100 = Color(0xFFFEE2E2)
    val Red700 = Color(0xFFB91C1C)
    val Yellow100 = Color(0xFFFEF9C3)
    val Yellow700 = Color(0xFFA16207)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray700 = Color(0xFF374151)

    return when (category) {
        "Alimentos" -> Green100 to Green700
        "Transporte" -> Blue100 to Blue700
        "Entretenimiento" -> Purple100 to Purple700
        "Salud" -> Red100 to Red700
        "Servicios" -> Yellow100 to Yellow700
        else -> Gray100 to Gray700
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    return format.format(amount)
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("d MMM", Locale("es", "MX"))
    return sdf.format(date)
}

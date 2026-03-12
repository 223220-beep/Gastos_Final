package com.gastosapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private val listener: OnExpenseClickListener) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses: List<ExpenseEntity> = emptyList()

    interface OnExpenseClickListener {
        fun onExpenseClick(expense: ExpenseEntity)
    }

    fun setExpenses(expenses: List<ExpenseEntity>) {
        this.expenses = expenses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: ExpenseEntity) {
            binding.tvDescription.text = expense.description
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(expense.timestamp))
            binding.tvCategoryDate.text = "${expense.category} • $dateStr"
            binding.tvAmount.text = formatCurrency(expense.amount)

            binding.root.setOnClickListener {
                listener.onExpenseClick(expense)
            }
        }

        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            return format.format(amount)
        }
    }
}

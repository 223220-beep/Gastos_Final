package com.gastosapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gastosapp.data.local.ReminderEntity
import com.gastosapp.databinding.ItemReminderBinding
import java.text.NumberFormat
import java.util.*

class ReminderAdapter(private val listener: OnReminderClickListener) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private var reminders: List<ReminderEntity> = emptyList()

    interface OnReminderClickListener {
        fun onReminderClick(reminder: ReminderEntity)
        fun onReminderToggle(reminder: ReminderEntity)
    }

    fun setReminders(reminders: List<ReminderEntity>) {
        this.reminders = reminders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position], listener)
    }

    override fun getItemCount(): Int = reminders.size

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ReminderEntity, listener: OnReminderClickListener) {
            binding.tvDescription.text = reminder.description
            binding.tvDate.text = "${reminder.date} • ${reminder.category}"
            binding.tvAmount.text = formatCurrency(reminder.amount)

            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = reminder.isCompleted

            if (reminder.isCompleted) {
                binding.tvDescription.alpha = 0.5f
                binding.tvAmount.alpha = 0.5f
            } else {
                binding.tvDescription.alpha = 1.0f
                binding.tvAmount.alpha = 1.0f
            }

            binding.cbCompleted.setOnCheckedChangeListener { _, _ ->
                listener.onReminderToggle(reminder)
            }

            binding.root.setOnClickListener { listener.onReminderClick(reminder) }
        }

        private fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
            return format.format(amount)
        }
    }
}

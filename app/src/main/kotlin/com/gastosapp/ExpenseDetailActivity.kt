package com.gastosapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.databinding.ActivityExpenseDetailBinding
import com.gastosapp.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseDetailBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private var expenseId: Int = -1
    private var expense: ExpenseEntity? = null
    private var isEditing = false
    private val selectedDate = Calendar.getInstance()

    companion object {
        private val CATEGORIES = arrayOf(
            "Alimentos",
            "Transporte",
            "Entretenimiento",
            "Salud",
            "Servicios",
            "Otros"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseId = intent.getIntExtra("expense_id", -1)
        if (expenseId == -1) {
            finish()
            return
        }

        initViews()
        setupCategorySpinner()
        setupDatePicker()
        observeViewModel()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener { switchToEditMode() }
        binding.btnDelete.setOnClickListener { showDeleteConfirmation() }
        binding.btnSave.setOnClickListener { handleUpdate() }
        binding.btnCancel.setOnClickListener { switchToViewMode() }
    }

    private fun observeViewModel() {
        viewModel.getExpenseById(expenseId).observe(this) { expenseEntity ->
            if (expenseEntity != null) {
                expense = expenseEntity
                if (!isEditing) {
                    displayExpense()
                }
            } else if (!isEditing) {
                finish()
            }
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, CATEGORIES)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.etDate.setText(sdf.format(selectedDate.time))
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun displayExpense() {
        expense?.let {
            binding.tvDescription.text = it.description
            binding.tvAmount.text = formatCurrency(it.amount)
            binding.tvCategory.text = it.category
            binding.tvDate.text = formatDateLong(it.timestamp)
        }
    }

    private fun switchToEditMode() {
        isEditing = true
        binding.viewModeLayout.visibility = View.GONE
        binding.editModeLayout.visibility = View.VISIBLE

        expense?.let {
            binding.etDescription.setText(it.description)
            binding.etAmount.setText(it.amount.toString())

            val categoryIndex = CATEGORIES.indexOf(it.category)
            if (categoryIndex >= 0) {
                binding.spinnerCategory.setSelection(categoryIndex)
            }

            selectedDate.timeInMillis = it.timestamp
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.etDate.setText(sdf.format(selectedDate.time))
        }
    }

    private fun switchToViewMode() {
        isEditing = false
        binding.editModeLayout.visibility = View.GONE
        binding.viewModeLayout.visibility = View.VISIBLE
    }

    private fun handleUpdate() {
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val category = CATEGORIES[binding.spinnerCategory.selectedItemPosition]

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        expense?.let {
            val updatedExpense = it.copy(
                description = description,
                amount = amount,
                category = category,
                timestamp = selectedDate.timeInMillis
            )
            viewModel.updateExpense(updatedExpense)
            Toast.makeText(this, "¡Gasto actualizado exitosamente!", Toast.LENGTH_SHORT).show()
            switchToViewMode()
        }
        setLoading(false)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar gasto?")
            .setMessage("Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> handleDelete() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleDelete() {
        setLoading(true)
        expense?.let {
            viewModel.deleteExpense(it)
            Toast.makeText(this, "¡Gasto eliminado exitosamente!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSave.isEnabled = !loading
        binding.btnSave.text = if (loading) "Guardando..." else "Guardar"
        binding.btnDelete.isEnabled = !loading
        binding.btnEdit.isEnabled = !loading
        binding.btnCancel.isEnabled = !loading
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return format.format(amount)
    }

    private fun formatDateLong(timestamp: Long): String {
        val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX"))
        return outputFormat.format(Date(timestamp))
    }
}

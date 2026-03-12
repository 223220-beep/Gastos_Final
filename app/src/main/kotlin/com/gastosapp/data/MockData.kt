package com.gastosapp.data

import com.gastosapp.model.Expense
import com.gastosapp.model.Reminder
import com.gastosapp.model.User
import java.util.*

/**
 * Datos mock locales para pruebas.
 * TODO: Reemplazar con llamadas a API real cuando esté disponible.
 */
object MockData {
    private var currentUser: User? = null
    private val expenses: MutableList<Expense> = mutableListOf()
    private val reminders: MutableList<Reminder> = mutableListOf()

    init {
        initMockExpenses()
        initMockReminders()
    }

    private fun initMockExpenses() {
        expenses.add(
            Expense(
                UUID.randomUUID().toString(),
                "Comida en restaurante",
                250.00,
                "Alimentos",
                "2026-03-06",
                "user1"
            )
        )
        expenses.add(
            Expense(
                UUID.randomUUID().toString(),
                "Uber al trabajo",
                85.50,
                "Transporte",
                "2026-03-05",
                "user1"
            )
        )
        expenses.add(
            Expense(
                UUID.randomUUID().toString(),
                "Netflix mensual",
                199.00,
                "Entretenimiento",
                "2026-03-04",
                "user1"
            )
        )
        expenses.add(
            Expense(
                UUID.randomUUID().toString(),
                "Consulta médica",
                500.00,
                "Salud",
                "2026-03-03",
                "user1"
            )
        )
        expenses.add(
            Expense(
                UUID.randomUUID().toString(),
                "Luz del mes",
                450.00,
                "Servicios",
                "2026-03-02",
                "user1"
            )
        )
        expenses.add(
            Expense(
                UUID.randomUUID().toString(),
                "Compras supermercado",
                1200.00,
                "Alimentos",
                "2026-03-01",
                "user1"
            )
        )
    }

    private fun initMockReminders() {
        reminders.add(
            Reminder(
                UUID.randomUUID().toString(),
                "Pago de luz",
                850.00,
                "Servicios",
                "2026-03-15",
                false
            )
        )
        reminders.add(
            Reminder(
                UUID.randomUUID().toString(),
                "Internet mensual",
                549.00,
                "Servicios",
                "2026-03-20",
                false
            )
        )
    }

    fun getCurrentUser(): User? = currentUser

    fun setCurrentUser(user: User?) {
        this.currentUser = user
    }

    fun getExpenses(): List<Expense> = expenses.toList()

    fun getRecentExpenses(limit: Int): List<Expense> {
        val size = limit.coerceAtMost(expenses.size)
        return expenses.subList(0, size).toList()
    }

    fun getTotalMonth(): Double = expenses.sumOf { it.amount }

    fun addExpense(expense: Expense) {
        expense.id = UUID.randomUUID().toString()
        expenses.add(0, expense)
    }

    fun updateExpense(updatedExpense: Expense) {
        val index = expenses.indexOfFirst { it.id == updatedExpense.id }
        if (index != -1) {
            expenses[index] = updatedExpense
        }
    }

    fun deleteExpense(expenseId: String) {
        expenses.removeAll { it.id == expenseId }
    }

    fun searchExpenses(query: String): List<Expense> {
        val lowerQuery = query.lowercase()
        return expenses.filter {
            it.description.lowercase().contains(lowerQuery) ||
                    it.category.lowercase().contains(lowerQuery)
        }
    }

    // Reminder methods
    fun getReminders(): List<Reminder> = reminders.toList()

    fun addReminder(reminder: Reminder) {
        reminder.id = UUID.randomUUID().toString()
        reminders.add(0, reminder)
    }

    fun updateReminder(updatedReminder: Reminder) {
        val index = reminders.indexOfFirst { it.id == updatedReminder.id }
        if (index != -1) {
            reminders[index] = updatedReminder
        }
    }

    fun deleteReminder(reminderId: String) {
        reminders.removeAll { it.id == reminderId }
    }

    /**
     * Placeholder para login con API.
     * TODO: Implementar llamada real a API de autenticación.
     */
    fun mockLogin(email: String?, password: String?): User? {
        // Simulación de login exitoso
        if (!email.isNullOrEmpty() && !password.isNullOrEmpty() && password.length >= 6) {
            val user = User(
                UUID.randomUUID().toString(),
                email,
                email.split("@")[0]
            )
            setCurrentUser(user)
            return user
        }
        return null
    }

    /**
     * Placeholder para registro con API.
     * TODO: Implementar llamada real a API de registro.
     */
    fun mockRegister(name: String?, email: String?, password: String?): Boolean {
        // Simulación de registro exitoso
        return !name.isNullOrEmpty() &&
                !email.isNullOrEmpty() &&
                !password.isNullOrEmpty() && password.length >= 6
    }

    fun logout() {
        currentUser = null
    }
}

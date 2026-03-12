package com.gastosapp.data;

import com.gastosapp.model.Expense;
import com.gastosapp.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Datos mock locales para pruebas.
 * TODO: Reemplazar con llamadas a API real cuando esté disponible.
 */
public class MockData {
    private static MockData instance;
    private User currentUser;
    private List<Expense> expenses;
    private List<com.gastosapp.model.Reminder> reminders;

    private MockData() {
        expenses = new ArrayList<>();
        reminders = new ArrayList<>();
        initMockExpenses();
        initMockReminders();
    }

    public static synchronized MockData getInstance() {
        if (instance == null) {
            instance = new MockData();
        }
        return instance;
    }

    private void initMockExpenses() {
        expenses.add(new Expense(
                UUID.randomUUID().toString(),
                "Comida en restaurante",
                250.00,
                "Alimentos",
                "2026-03-06",
                "user1"));
        expenses.add(new Expense(
                UUID.randomUUID().toString(),
                "Uber al trabajo",
                85.50,
                "Transporte",
                "2026-03-05",
                "user1"));
        expenses.add(new Expense(
                UUID.randomUUID().toString(),
                "Netflix mensual",
                199.00,
                "Entretenimiento",
                "2026-03-04",
                "user1"));
        expenses.add(new Expense(
                UUID.randomUUID().toString(),
                "Consulta médica",
                500.00,
                "Salud",
                "2026-03-03",
                "user1"));
        expenses.add(new Expense(
                UUID.randomUUID().toString(),
                "Luz del mes",
                450.00,
                "Servicios",
                "2026-03-02",
                "user1"));
        expenses.add(new Expense(
                UUID.randomUUID().toString(),
                "Compras supermercado",
                1200.00,
                "Alimentos",
                "2026-03-01",
                "user1"));
    }

    private void initMockReminders() {
        reminders.add(new com.gastosapp.model.Reminder(
                UUID.randomUUID().toString(),
                "Pago de luz",
                850.00,
                "Servicios",
                "2026-03-15",
                false));
        reminders.add(new com.gastosapp.model.Reminder(
                UUID.randomUUID().toString(),
                "Internet mensual",
                549.00,
                "Servicios",
                "2026-03-20",
                false));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }

    public List<Expense> getRecentExpenses(int limit) {
        int size = Math.min(limit, expenses.size());
        return new ArrayList<>(expenses.subList(0, size));
    }

    public double getTotalMonth() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    public void addExpense(Expense expense) {
        expense.setId(UUID.randomUUID().toString());
        expenses.add(0, expense);
    }

    public void updateExpense(Expense updatedExpense) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(updatedExpense.getId())) {
                expenses.set(i, updatedExpense);
                break;
            }
        }
    }

    public void deleteExpense(String expenseId) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(expenseId)) {
                expenses.remove(i);
                break;
            }
        }
    }

    public List<Expense> searchExpenses(String query) {
        List<Expense> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Expense expense : expenses) {
            if (expense.getDescription().toLowerCase().contains(lowerQuery) ||
                    expense.getCategory().toLowerCase().contains(lowerQuery)) {
                filtered.add(expense);
            }
        }
        return filtered;
    }

    // Reminder methods
    public List<com.gastosapp.model.Reminder> getReminders() {
        return new ArrayList<>(reminders);
    }

    public void addReminder(com.gastosapp.model.Reminder reminder) {
        reminder.setId(UUID.randomUUID().toString());
        reminders.add(0, reminder);
    }

    public void updateReminder(com.gastosapp.model.Reminder updatedReminder) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(updatedReminder.getId())) {
                reminders.set(i, updatedReminder);
                break;
            }
        }
    }

    public void deleteReminder(String reminderId) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(reminderId)) {
                reminders.remove(i);
                break;
            }
        }
    }

    /**
     * Placeholder para login con API.
     * TODO: Implementar llamada real a API de autenticación.
     */
    public User mockLogin(String email, String password) {
        // Simulación de login exitoso
        if (email != null && !email.isEmpty() && password != null && password.length() >= 6) {
            User user = new User(
                    UUID.randomUUID().toString(),
                    email,
                    email.split("@")[0]);
            setCurrentUser(user);
            return user;
        }
        return null;
    }

    /**
     * Placeholder para registro con API.
     * TODO: Implementar llamada real a API de registro.
     */
    public boolean mockRegister(String name, String email, String password) {
        // Simulación de registro exitoso
        return name != null && !name.isEmpty() &&
                email != null && !email.isEmpty() &&
                password != null && password.length() >= 6;
    }

    public void logout() {
        currentUser = null;
    }
}

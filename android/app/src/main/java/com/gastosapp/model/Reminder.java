package com.gastosapp.model;

import java.io.Serializable;

public class Reminder implements Serializable {
    private String id;
    private String description;
    private double amount;
    private String category;
    private String reminderDate;
    private boolean completed;

    public Reminder() {
    }

    public Reminder(String id, String description, double amount, String category, String reminderDate,
            boolean completed) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.reminderDate = reminderDate;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

package com.gastosapp.model

import java.io.Serializable

data class Reminder(
    var id: String? = null,
    var description: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var reminderDate: String = "",
    var isCompleted: Boolean = false
) : Serializable

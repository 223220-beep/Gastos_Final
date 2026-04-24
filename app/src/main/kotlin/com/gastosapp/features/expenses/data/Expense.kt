package com.gastosapp.features.expenses.data

import java.io.Serializable

data class Expense(
    var id: String? = null,
    var description: String = "",
    var amount: Double = 0.0,
    var category: String = "",
    var date: String = "",
    var userId: String = ""
) : Serializable

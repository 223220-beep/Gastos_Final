package com.gastosapp.features.auth.data

import java.io.Serializable

data class User(
    var id: String? = null,
    var email: String = "",
    var name: String = ""
) : Serializable

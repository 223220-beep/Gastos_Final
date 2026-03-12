package com.gastosapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastosapp.data.local.UserEntity
import com.gastosapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableLiveData<UserEntity?>()
    val currentUser: LiveData<UserEntity?> = _currentUser

    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String> = _authError

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.login(email, password)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    _authError.value = "Credenciales inválidas"
                }
            } catch (e: Exception) {
                _authError.value = "Error en el servidor: ${e.message}"
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val newUser = UserEntity(name = name, email = email, password = password)
                val success = repository.register(newUser)
                if (success) {
                    _registerSuccess.value = true
                } else {
                    _authError.value = "El correo ya está registrado"
                }
            } catch (e: Exception) {
                _authError.value = "Error al registrar: ${e.message}"
            }
        }
    }
}

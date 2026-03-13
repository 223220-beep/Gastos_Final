package com.gastosapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.gastosapp.databinding.ActivityLoginBinding
import com.gastosapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        setupObservers()
        setupListeners()
        setupBiometric()
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(this) { user ->
            if (user != null) {
                // GUARDAR EN SHAREDPREFERENCES AL TENER ÉXITO
                saveUserCredentials(user.email, user.password)
                
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
                finish()
            }
        }

        viewModel.authError.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserCredentials(email: String, pass: String) {
        val users = sharedPreferences.getStringSet("saved_emails", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        users.add(email)
        sharedPreferences.edit().apply {
            putStringSet("saved_emails", users)
            putString("pass_$email", pass) // Guardamos la pass asociada al email
            apply()
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnFingerprint.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showSavedUsersMenu()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Rápido")
            .setSubtitle("Usa tu huella para elegir una cuenta")
            .setNegativeButtonText("Cancelar")
            .build()
            
        binding.btnFingerprint.visibility = View.VISIBLE
    }

    private fun showSavedUsersMenu() {
        val savedEmails = sharedPreferences.getStringSet("saved_emails", emptySet())?.toList() ?: emptyList()

        if (savedEmails.isEmpty()) {
            Toast.makeText(this, "No hay cuentas guardadas. Inicia sesión manualmente una vez.", Toast.LENGTH_LONG).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona tu cuenta")
        
        builder.setItems(savedEmails.toTypedArray()) { _, which ->
            val selectedEmail = savedEmails[which]
            val savedPass = sharedPreferences.getString("pass_$selectedEmail", "")
            
            if (!savedPass.isNullOrEmpty()) {
                binding.etEmail.setText(selectedEmail)
                binding.etPassword.setText(savedPass)
                viewModel.login(selectedEmail, savedPass)
            }
        }
        
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}

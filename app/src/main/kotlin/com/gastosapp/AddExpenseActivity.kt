package com.gastosapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gastosapp.data.local.ExpenseEntity
import com.gastosapp.databinding.ActivityAddExpenseBinding
import com.gastosapp.viewmodel.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val selectedDate = Calendar.getInstance()
    private val voiceTextBuilder = StringBuilder()

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val CATEGORIES =
                arrayOf(
                        "Selecciona una categoría",
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
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupCategorySpinner()
        setupDatePicker()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { handleSave() }
        binding.btnMic.setOnClickListener { toggleVoiceRecognition() }

        initSpeechRecognizer()
        updateDateDisplay()
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
                                updateDateDisplay()
                            },
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                    )
                    .show()
        }
        binding.etDate.isFocusable = false
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.etDate.setText(sdf.format(selectedDate.time))
    }

    private fun handleSave() {
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val userId = intent.getStringExtra("user_id") ?: "default_user"

        if (description.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una descripción", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un monto", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoryPosition == 0) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        val amount =
                amountStr.toDoubleOrNull()
                        ?: run {
                            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show()
                            return
                        }

        if (amount <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        val expense =
                ExpenseEntity(
                        description = description,
                        amount = amount,
                        category = CATEGORIES[categoryPosition],
                        timestamp = selectedDate.timeInMillis,
                        userId = userId
                )

        viewModel.addExpense(expense)
        Toast.makeText(this, "¡Gasto creado exitosamente!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer =
                    SpeechRecognizer.createSpeechRecognizer(this).apply {
                        setRecognitionListener(
                                object : RecognitionListener {
                                    override fun onReadyForSpeech(params: Bundle?) {
                                        isListening = true
                                        binding.btnMic.setColorFilter(
                                                ContextCompat.getColor(
                                                        this@AddExpenseActivity,
                                                        R.color.red_600
                                                )
                                        )
                                        Toast.makeText(
                                                        this@AddExpenseActivity,
                                                        "Escuchando...",
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                    }

                                    override fun onBeginningOfSpeech() {}
                                    override fun onRmsChanged(rmsdB: Float) {}
                                    override fun onBufferReceived(buffer: ByteArray?) {}
                                    override fun onEndOfSpeech() {
                                        if (isListening) startListening()
                                    }

                                    override fun onError(error: Int) {
                                        if (isListening) startListening()
                                        else {
                                            isListening = false
                                            binding.btnMic.setColorFilter(
                                                    ContextCompat.getColor(
                                                            this@AddExpenseActivity,
                                                            R.color.gray_400
                                                    )
                                            )
                                        }
                                    }

                                    override fun onResults(results: Bundle?) {
                                        val matches =
                                                results?.getStringArrayList(
                                                        SpeechRecognizer.RESULTS_RECOGNITION
                                                )
                                        if (!matches.isNullOrEmpty()) {
                                            val result = matches[0]
                                            if (voiceTextBuilder.isNotEmpty())
                                                    voiceTextBuilder.append(" ")
                                            voiceTextBuilder.append(result)
                                            binding.etDescription.setText(
                                                    voiceTextBuilder.toString()
                                            )
                                        }
                                        if (isListening) startListening()
                                    }

                                    override fun onPartialResults(partialResults: Bundle?) {}
                                    override fun onEvent(eventType: Int, params: Bundle?) {}
                                }
                        )
                    }
        }
    }

    private fun toggleVoiceRecognition() {
        if (isListening) {
            isListening = false
            speechRecognizer?.stopListening()
            binding.btnMic.setColorFilter(ContextCompat.getColor(this, R.color.gray_400))
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                            PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        REQUEST_RECORD_AUDIO_PERMISSION
                )
            } else {
                voiceTextBuilder.setLength(0)
                isListening = true
                startListening()
            }
        }
    }

    private fun startListening() {
        val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                }
        speechRecognizer?.startListening(intent)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSave.isEnabled = !loading
        binding.btnSave.text = if (loading) "Guardando..." else "Guardar"
        binding.etDescription.isEnabled = !loading
        binding.etAmount.isEnabled = !loading
        binding.spinnerCategory.isEnabled = !loading
        binding.etDate.isEnabled = !loading
    }
}

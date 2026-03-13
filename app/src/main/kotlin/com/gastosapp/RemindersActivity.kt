package com.gastosapp

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastosapp.adapter.ReminderAdapter
import com.gastosapp.data.local.ReminderEntity
import com.gastosapp.databinding.ActivityRemindersBinding
import com.gastosapp.viewmodel.ReminderViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RemindersActivity : AppCompatActivity(), ReminderAdapter.OnReminderClickListener {

    private lateinit var binding: ActivityRemindersBinding
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val selectedDate = Calendar.getInstance()
    private val voiceTextBuilder = StringBuilder()

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 201
        private val CATEGORIES =
                arrayOf("Alimentos", "Transporte", "Entretenimiento", "Salud", "Servicios", "Otros")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        initViews()
        setupRecyclerView()
        setupCategorySpinner()
        setupDatePicker()
        setupTimePicker()
        initSpeechRecognizer()
        observeViewModel()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        // Permiso de Audio
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        
        // Permiso de Notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        }

        // Permiso de Alarma Exacta (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddReminder.setOnClickListener { binding.formLayout.visibility = View.VISIBLE }
        binding.btnCancel.setOnClickListener { binding.formLayout.visibility = View.GONE }
        binding.btnSave.setOnClickListener { handleSave() }
        binding.btnMic.setOnClickListener { toggleVoiceRecognition() }

        updateDateDisplay()
        updateTimeDisplay()
    }

    private fun setupRecyclerView() {
        adapter = ReminderAdapter(this)
        binding.rvReminders.layoutManager = LinearLayoutManager(this)
        binding.rvReminders.adapter = adapter
    }

    private fun setupCategorySpinner() {
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, CATEGORIES)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = catAdapter
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                            this,
                            { _, year, month, dayOfMonth ->
                                selectedDate.set(Calendar.YEAR, year)
                                selectedDate.set(Calendar.MONTH, month)
                                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                updateDateDisplay()
                            },
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                    )
                    .show()
        }
    }

    private fun setupTimePicker() {
        binding.etTime.setOnClickListener {
            TimePickerDialog(
                            this,
                            { _, hourOfDay, minute ->
                                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                selectedDate.set(Calendar.MINUTE, minute)
                                selectedDate.set(Calendar.SECOND, 0)
                                updateTimeDisplay()
                            },
                            selectedDate.get(Calendar.HOUR_OF_DAY),
                            selectedDate.get(Calendar.MINUTE),
                            true
                    )
                    .show()
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.etDate.setText(sdf.format(selectedDate.time))
    }

    private fun updateTimeDisplay() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.etTime.setText(sdf.format(selectedDate.time))
    }

    private fun observeViewModel() {
        viewModel.allReminders.observe(this) { reminders ->
            if (reminders != null) {
                adapter.setReminders(reminders)
                if (reminders.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.rvReminders.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.rvReminders.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun handleSave() {
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val date = binding.etDate.text.toString()
        val time = binding.etTime.text.toString()

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "La fecha debe ser futura", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val reminder =
                ReminderEntity(
                        description = description,
                        amount = amount,
                        category = category,
                        date = "$date $time",
                        isCompleted = false
                )
        viewModel.addReminder(reminder)

        scheduleAlarm(reminder)

        binding.formLayout.visibility = View.GONE
        clearForm()
        Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleAlarm(reminder: ReminderEntity) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, com.gastosapp.receiver.ReminderReceiver::class.java).apply {
            putExtra("TITLE", "🔔 Recordatorio: ${reminder.category}")
            putExtra("MESSAGE", "${reminder.description} - $${reminder.amount}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                selectedDate.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: No se pudo programar la alarma exacta", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearForm() {
        binding.etDescription.setText("")
        binding.etAmount.setText("")
        selectedDate.time = Date()
        updateDateDisplay()
        updateTimeDisplay()
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
                                                        this@RemindersActivity,
                                                        R.color.red_600
                                                )
                                        )
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
                                                            this@RemindersActivity,
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
                        REQUEST_CODE_PERMISSIONS
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

    override fun onReminderClick(reminder: ReminderEntity) {}

    override fun onReminderToggle(reminder: ReminderEntity) {
        val updatedReminder = reminder.copy(isCompleted = !reminder.isCompleted)
        viewModel.updateReminder(updatedReminder)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}

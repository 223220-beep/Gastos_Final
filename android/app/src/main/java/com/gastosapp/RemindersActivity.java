package com.gastosapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gastosapp.adapter.ReminderAdapter;
import com.gastosapp.data.local.ReminderEntity;
import com.gastosapp.viewmodel.ReminderViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RemindersActivity extends AppCompatActivity implements ReminderAdapter.OnReminderClickListener {

    private RecyclerView rvReminders;
    private View emptyView;
    private LinearLayout formLayout;
    private EditText etDescription;
    private EditText etAmount;
    private EditText etDate;
    private Spinner spinnerCategory;
    private ImageButton btnMic;
    private ReminderAdapter adapter;
    private ReminderViewModel viewModel;

    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private Calendar selectedDate;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private static final String[] CATEGORIES = {
            "Alimentos", "Transporte", "Entretenimiento", "Salud", "Servicios", "Otros"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        viewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        initViews();
        setupRecyclerView();
        setupCategorySpinner();
        setupDatePicker();
        initSpeechRecognizer();
        observeViewModel();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddReminder = findViewById(R.id.btnAddReminder);
        rvReminders = findViewById(R.id.rvReminders);
        emptyView = findViewById(R.id.emptyView);
        formLayout = findViewById(R.id.formLayout);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnMic = findViewById(R.id.btnMic);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnBack.setOnClickListener(v -> finish());
        btnAddReminder.setOnClickListener(v -> formLayout.setVisibility(View.VISIBLE));
        btnCancel.setOnClickListener(v -> formLayout.setVisibility(View.GONE));
        btnSave.setOnClickListener(v -> handleSave());
        btnMic.setOnClickListener(v -> toggleVoiceRecognition());

        selectedDate = Calendar.getInstance();
        updateDateDisplay();
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(this);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        rvReminders.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateDateDisplay();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void observeViewModel() {
        viewModel.getAllReminders().observe(this, reminders -> {
            if (reminders != null) {
                adapter.setReminders(reminders);
                if (reminders.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    rvReminders.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    rvReminders.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void handleSave() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = etDate.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        ReminderEntity reminder = new ReminderEntity(description, amount, category, date, false);
        viewModel.addReminder(reminder);

        formLayout.setVisibility(View.GONE);
        clearForm();
        Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show();
    }

    private void clearForm() {
        etDescription.setText("");
        etAmount.setText("");
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
    }

    // Voice recognition logic
    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    btnMic.setColorFilter(ContextCompat.getColor(RemindersActivity.this, R.color.red_600));
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    isListening = false;
                    btnMic.setColorFilter(ContextCompat.getColor(RemindersActivity.this, R.color.gray_400));
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    btnMic.setColorFilter(ContextCompat.getColor(RemindersActivity.this, R.color.gray_400));
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        etDescription.setText(matches.get(0));
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }
    }

    private void toggleVoiceRecognition() {
        if (isListening) {
            speechRecognizer.stopListening();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                        REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startListening();
            }
        }
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onReminderClick(ReminderEntity reminder) {
        // Option to edit or delete
    }

    @Override
    public void onReminderToggle(ReminderEntity reminder) {
        reminder.setCompleted(!reminder.isCompleted());
        viewModel.updateReminder(reminder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null)
            speechRecognizer.destroy();
    }
}

package com.gastosapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.gastosapp.data.local.ExpenseEntity;
import com.gastosapp.viewmodel.ExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddExpenseActivity extends AppCompatActivity {

    private EditText etDescription;
    private EditText etAmount;
    private Spinner spinnerCategory;
    private EditText etDate;
    private Button btnSave;
    private ImageButton btnMic;
    private SpeechRecognizer speechRecognizer;
    private boolean isLoading = false;
    private boolean isListening = false;
    private Calendar selectedDate;
    private ExpenseViewModel viewModel;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private static final String[] CATEGORIES = {
            "Selecciona una categoría",
            "Alimentos",
            "Transporte",
            "Entretenimiento",
            "Salud",
            "Servicios",
            "Otros"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        initViews();
        setupCategorySpinner();
        setupDatePicker();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        btnMic = findViewById(R.id.btnMic);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSave());
        btnMic.setOnClickListener(v -> toggleVoiceRecognition());

        initSpeechRecognizer();

        // Set default date to today
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        etDate.setFocusable(false);
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void handleSave() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        int categoryPosition = spinnerCategory.getSelectedItemPosition();

        if (description.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryPosition == 0) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        ExpenseEntity expense = new ExpenseEntity(
                description,
                amount,
                CATEGORIES[categoryPosition],
                selectedDate.getTimeInMillis(),
                "user1" // Temporalmente fijo hasta que implementes Auth con Room
        );

        viewModel.addExpense(expense);

        Toast.makeText(this, "¡Gasto creado exitosamente!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    btnMic.setColorFilter(ContextCompat.getColor(AddExpenseActivity.this, R.color.red_600));
                    Toast.makeText(AddExpenseActivity.this, "Escuchando...", Toast.LENGTH_SHORT).show();
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
                    btnMic.setColorFilter(ContextCompat.getColor(AddExpenseActivity.this, R.color.gray_400));
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    btnMic.setColorFilter(ContextCompat.getColor(AddExpenseActivity.this, R.color.gray_400));
                    Toast.makeText(AddExpenseActivity.this, "Error en reconocimiento", Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnSave.setEnabled(!loading);
        btnSave.setText(loading ? "Guardando..." : "Guardar");
        etDescription.setEnabled(!loading);
        etAmount.setEnabled(!loading);
        spinnerCategory.setEnabled(!loading);
        etDate.setEnabled(!loading);
    }
}

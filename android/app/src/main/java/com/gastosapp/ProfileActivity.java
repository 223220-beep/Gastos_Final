package com.gastosapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gastosapp.data.local.UserEntity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    private UserEntity user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = (UserEntity) getIntent().getSerializableExtra("user");
        if (user == null) {
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvId = findViewById(R.id.tvId);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());
        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());
        tvId.setText(String.valueOf(user.getId()));

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        // Clear session if needed
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

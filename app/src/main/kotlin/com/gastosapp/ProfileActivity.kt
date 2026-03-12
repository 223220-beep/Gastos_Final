package com.gastosapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gastosapp.data.local.UserEntity
import com.gastosapp.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var user: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getSerializableExtra("user") as? UserEntity
        if (user == null) {
            finish()
            return
        }

        initViews()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }

        user?.let {
            binding.tvUserName.text = it.name
            binding.tvUserEmail.text = it.email
            binding.tvName.text = it.name
            binding.tvEmail.text = it.email
            binding.tvId.text = it.id.toString()
        }

        binding.btnLogout.setOnClickListener { handleLogout() }
    }

    private fun handleLogout() {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}

package com.gastosapp.features.analytics.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.gastosapp.core.presentation.theme.GastosAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetActivity : ComponentActivity() {
    
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GastosAppTheme {
                BudgetScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

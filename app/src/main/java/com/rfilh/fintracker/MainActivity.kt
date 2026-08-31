package com.rfilh.fintracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.rfilh.fintracker.ui.nav.FinTrackerNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as FinTrackerApp).container
        setContent {
            MaterialTheme {
                Surface { FinTrackerNavHost(container) }
            }
        }
    }
}

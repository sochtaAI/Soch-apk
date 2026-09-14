package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.repository.SochRepository
import com.example.ui.navigation.SochNavigation
import com.example.ui.theme.SochTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = SochRepository(applicationContext)
        setContent {
            SochTheme {
                SochNavigation(repository = repository)
            }
        }
    }
}


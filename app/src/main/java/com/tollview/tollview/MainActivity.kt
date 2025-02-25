package com.tollview.tollview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.tollview.tollview.auth.AuthViewModel
import com.tollview.tollview.ui.TollViewTheme
import com.tollview.tollview.ui.login.LoginScreen
import com.tollview.tollview.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TollViewTheme {
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()
                val navController = rememberNavController()

                if (currentUser != null) {
                    AppNavHost(navController = navController, onLogout = {
                        authViewModel.refreshUser()
                    })
                } else {
                    LoginScreen(onLoginSuccess = { authViewModel.refreshUser() })
                }
            }
        }
    }
}
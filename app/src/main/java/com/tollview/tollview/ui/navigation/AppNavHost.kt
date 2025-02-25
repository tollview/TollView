package com.tollview.tollview.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tollview.tollview.ui.home.HomeScreen
import com.tollview.tollview.ui.history.HistoryScreen
import com.tollview.tollview.ui.profile.ProfileScreen

@Composable
fun AppNavHost(navController: NavHostController, onLogout: () -> Unit) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
            composable("history") { HistoryScreen() }
            composable("profile") { ProfileScreen(onLogout) }
        }
    }
}
package com.irblaster.app.ui.screens

import androidx.compose.runtime.Composable
import com.irblaster.app.viewmodel.MainViewModel

/**
 * Backward-compatible Composable alias delegating to MainScreen.
 */
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    MainScreen(viewModel = viewModel)
}

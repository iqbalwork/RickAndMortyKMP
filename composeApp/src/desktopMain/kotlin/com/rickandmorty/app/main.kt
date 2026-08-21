package com.rickandmorty.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rickandmorty.app.di.initializeKoin

fun main() {
    initializeKoin()
    application {
        val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = "Rick and Morty KMP",
            state = windowState
        ) {
            App()
        }
    }
}

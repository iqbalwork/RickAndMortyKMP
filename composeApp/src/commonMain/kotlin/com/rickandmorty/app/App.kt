package com.rickandmorty.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rickandmorty.app.core.designsystem.theme.RickMortyTheme

@Composable
fun App() {
    RickMortyTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Rick & Morty KMP")
        }
    }
}

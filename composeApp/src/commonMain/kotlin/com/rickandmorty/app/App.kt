package com.rickandmorty.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rickandmorty.app.core.designsystem.theme.RickMortyTheme
import com.rickandmorty.app.core.designsystem.theme.SpaceBlack
import com.rickandmorty.app.navigation.RickMortyNavHost

@Composable
fun App() {
    RickMortyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SpaceBlack
        ) {
            RickMortyNavHost()
        }
    }
}

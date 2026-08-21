package com.rickandmorty.app

import androidx.compose.ui.window.ComposeUIViewController
import com.rickandmorty.app.di.initializeKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        enforceStrictPlistSanityCheck = false
        initializeKoin()
    }
) {
    App()
}

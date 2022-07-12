// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class)

package dev.victor.canvas

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.*
import dev.victor.canvas.ui.Layer
import dev.victor.canvas.ui.Layers
import dev.victor.canvas.ui.MainView
import dev.victor.canvas.utils.getPreferredWindowSize
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() = application {
    Window(
        title = "Canvas",
        onCloseRequest = ::exitApplication,
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = getPreferredWindowSize(1280, 720)
        )
    ) {
//        lateinit var layers: MutableState<Layers>
//        layers = remember { mutableStateOf(Layers(this.window) { layers.value = it }) }
        var layers = remember { Layers(window) }
        MainView(layers)
    }
}

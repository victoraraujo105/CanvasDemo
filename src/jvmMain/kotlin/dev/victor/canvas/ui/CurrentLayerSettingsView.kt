package dev.victor.canvas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.godaddy.android.colorpicker.ClassicColorPicker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BackgroundSettingsView(
    layers: Layers,
    modifier: Modifier = Modifier
) {
    Expandable(
        label = "Background",
        labelAlign = TextAlign.Center,
        labelColor = LocalContentColor.current,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = layers::loadImage,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.colors.backgroundMedium
                )
            ) {
                Text("Load BG Image")
            }
            ClassicColorPicker(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                layers.background
            ) { color ->
                layers.apply { background = color.toColor() }
            }
        }
    }
}

@Composable
fun CirclesColorPicker(
    layers: Layers,
    modifier: Modifier = Modifier
) {
    Expandable(
        label = "Circles",
        labelAlign = TextAlign.Center,
        labelColor = LocalContentColor.current,
        modifier = modifier
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { layers.selected?.points?.run {
                    hull.also { hull ->
                        rawPoints.clear()
                        rawPoints.addAll(hull)
                    }
                } },
                enabled = layers.selected?.points != null,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.colors.backgroundMedium
                )
            ) {
                Text("Clear Inner Points", fontSize = 12.sp)
            }
            Slider(
                value = layers.circleRadius,
                valueRange = 1f..30f,
                onValueChange = { layers.circleRadius = it }
            )
            ClassicColorPicker(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                layers.circleFill
            ) { color ->
                layers.apply { circleFill = color.toColor() }
            }
        }
    }
}

@Composable
fun CurrentLayerSettingsView(
    layer: Layer?,
    modifier: Modifier = Modifier
) {
    Expandable(
        label = "Current Layer Settings",
        labelAlign = TextAlign.Center,
//        labelSize = 20.sp,
        labelColor = LocalContentColor.current,
        modifier = modifier
    ) {
        Column(
            modifier.padding(top = 6.dp)
        ) {
            Expandable(
                label = "Fill",
                modifier = Modifier.weight(1f, false)
            ) {
                ClassicColorPicker(
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    layer?.run { fill } ?: Color.Black
                ) { color ->
                    layer?.apply { fill = color.toColor() }
                }
            }

            Expandable(
                label = "Stroke",
                modifier = Modifier.weight(1f, false)
            ) {
                Column(Modifier.fillMaxSize()) {
                    layer?.apply {
                        Slider(
                            value = stroke.width.value,
                            onValueChange = {
                                stroke.width = it.dp
                            },
                            valueRange = 0.1f..100f
                        )}
                    ClassicColorPicker(
                        Modifier
                            .fillMaxSize()
                            .padding(vertical = 12.dp),
                        layer?.run { stroke.color } ?: Color.White
                    ) { color ->
                        layer?.apply { stroke.color = color.toColor() }
                    }
                }
            }
        }
    }
}

@Composable
fun Expandable(
    label: String,
    labelSize: TextUnit = 16.sp,
    labelColor: Color = LocalContentColor.current.copy(alpha = 0.8f),
    labelAlign: TextAlign = TextAlign.Left,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.run { if (isExpanded) this else Modifier }
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        with(LocalDensity.current) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = Icons.Filled.run { if (!isExpanded) KeyboardArrowUp else KeyboardArrowDown },
                    contentDescription = null,
                    tint = labelColor,
                    modifier = Modifier.requiredHeight(labelSize.toDp())
                )
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = labelColor,
                    fontSize = labelSize,
                    textAlign = labelAlign
                )
            }
        }
        if (isExpanded) {
            content()
        }
    }
}
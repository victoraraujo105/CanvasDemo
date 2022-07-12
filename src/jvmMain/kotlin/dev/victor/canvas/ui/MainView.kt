@file:OptIn(ExperimentalComposeUiApi::class)

package dev.victor.canvas.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.victor.canvas.*
import java.awt.event.MouseEvent
import java.util.*

@ExperimentalComposeUiApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainView(layers: Layers) {
    val panelState = remember { PanelState() }

    val animatedSize = if (panelState.splitter.isResizing) {
        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
    } else {
        animateDpAsState(
            if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
            SpringSpec(stiffness = Spring.StiffnessLow)
        ).value
    }

    MaterialTheme(
        colors = AppTheme.colors.material
    ) {
        Surface {
            VerticalSplittable(
                Modifier.fillMaxSize(),
                panelState.splitter,
                onResize = {
                    panelState.expandedSize =
                        (panelState.expandedSize + it).coerceAtLeast(panelState.expandedSizeMin)
                }
            ) {
                Box(
                    modifier = Modifier.background(layers.background)
                ) {
                    layers.backgroundImage?.also { img ->
                        Image(
                            BitmapPainter(img),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    with(LocalDensity.current) {
                        for (layer in layers) {
                            MyCanvas(layer, layers.showTriangulation, layers.circleRadius.toDp())
                        }
                    }
                }
                ResizablePanel(Modifier.width(animatedSize).fillMaxHeight().background(MaterialTheme.colors.surface),
                    panelState) {
                    Column(Modifier.fillMaxSize()) {
                        with(LocalDensity.current) {
                            LayerListViewTopBar()
                            LayerListView(layers, Modifier.weight(0.5f, false))
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = {
                                            val i = layers.layers.indexOf(layers.selected)
                                            if (i > 0) Collections.swap(layers.layers, i-1, i)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = AppTheme.colors.backgroundMedium
                                        ),
                                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                                        contentPadding = PaddingValues(5.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, "Move Layer Up", Modifier.heightIn
                                            (max = 20.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            val i = layers.layers.indexOf(layers.selected)
                                            if (i < layers.layers.lastIndex) Collections.swap(layers.layers, i, i+1)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = AppTheme.colors.backgroundMedium
                                        ),
                                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                                        contentPadding = PaddingValues(5.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, "Move Layer Down", Modifier.heightIn
                                            (max = 20.dp))
                                    }
                                }
                                Box(Modifier.fillMaxWidth()) {
                                    Checkbox(
                                        checked = layers.showTriangulation,
                                        onCheckedChange = { layers.showTriangulation = it },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                    Text("Show Triangulation", textAlign = TextAlign.Center, modifier = Modifier
                                        .fillMaxWidth().align(Alignment.Center))
                                }
                                BackgroundSettingsView(layers, modifier = Modifier.weight(0.75f, false))
                                CirclesColorPicker(layers, modifier = Modifier.weight(0.75f, false))
                                CurrentLayerSettingsView(layers.selected, modifier = Modifier.weight(0.75f, false))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyCanvas(
    layer: Layer,
    showTriangulation: Boolean = false,
    circleRadius: Dp = 10.dp
) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (layer.item.selected) {
                        this.onPointerEvent(PointerEventType.Press) {
                            when(it.awtEventOrNull?.button) {
                                MouseEvent.BUTTON1 ->
                                    with(it.awtEventOrNull!!) {
                                        when(clickCount) {
                                            1 -> { layer.points.rawPoints.add(Point(x.toFloat(), y.toFloat())) }
                                            else -> {}
                                        }
                                    }
                            }
                        }
                    } else this
                }

        ) {

        }
        with (LocalDensity.current) {
            with (layer) {
                if (points.size > 1)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val path = Path()
                        path.reset()
                        points.hull.also { points ->
                            points[0].apply { path.moveTo(x, y) }
                            for (point in points.drop(1)) {
                                point.apply { path.lineTo(x, y) }
                            }
                            path.close()
                        }
                        drawPath(path = path, color = layer.fill)
                        layer.stroke.apply { drawPath(path = path, color = color, style = Stroke(width = width.toPx())) }

                         if (showTriangulation) points.rawPoints.triangulation()?.forEach { it.apply {
                            path.reset()
                            path.moveTo(a.x, a.y)
                            path.lineTo(b.x, b.y)
                            path.lineTo(c.x, c.y)
                            path.close()
                        }
                            layer.stroke.apply { drawPath(
                                path = path,
                                color = color,
                                style = Stroke()
                            ) }
                        }
                    }
            }
        }

        if (layer.item.selected) Box(
            modifier = Modifier.fillMaxSize()
        ) {
            with(layer.points) {
                for (i in rawPoints.indices) {
                    DraggableCircle(
                        center = rawPoints[i],
                        radius = circleRadius,
                        onDrag = { rawPoints[i] = rawPoints[i] + it },
                        onDelete = { rawPoints.remove(rawPoints[i]) },
                        color = layer.circleFill
                    )
                }
            }
        }
    }
}
@Composable
fun DraggableCircle(
    center: Point = Point(0f, 0f),
    radius: Dp = 10.dp,
    color: Color = Color.Red,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onDrag: (Point) -> Unit = { dragAmount -> },
    onDelete: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset((center.x - radius.toPx()).toInt(), (center.y - radius.toPx()).toInt())
            }
            .clip(CircleShape)
            .size(radius*2)
            .background(color)
            .clickable { onClick() }
            .then(modifier)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    onDrag(dragAmount.toPoint())
                }
            }
            .onPointerEvent(PointerEventType.Press) {
                when(it.awtEventOrNull?.button) {
                    MouseEvent.BUTTON3 -> with(it.awtEventOrNull!!) {
                        when(clickCount) {
                            1 -> {
                                onDelete()
                            }
                        }
                    }
                }
            }
    )
}

fun Offset.toPoint() = Point(x, y)
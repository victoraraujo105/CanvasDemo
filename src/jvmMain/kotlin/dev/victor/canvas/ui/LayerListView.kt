package dev.victor.canvas.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.victor.canvas.Point
import dev.victor.canvas.convexHull
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.io.File

@Composable
fun LayerListView(state: Layer) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LayerListViewTopBar()
    }
}

@Composable
fun LayerListViewTopBar() = Surface {
    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Layers",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun LayerListItem(fontSize: TextUnit, height: Dp, model: Layer.Item, onClose: (()
-> Unit)? = null, onClick: ()
-> Unit =

    {}) {
    var editing by remember { mutableStateOf(false) }
    var shouldRequestFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val selected = model.selected
    Row(
        modifier = Modifier
            .wrapContentHeight()
//            .height(height)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .combinedClickable(
//                enabled = !editing,
                onClick = onClick,
                onDoubleClick = {
                    editing = true
                    shouldRequestFocus = true
                    onClick()
                }
            )
            .run {
                if (selected) this.background(Color.White.copy(alpha = 0.25f))
                else this
            }
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        if (editing) {
            BasicTextField(
                value = model.name,
                enabled = editing,
                onValueChange = { value -> model.name = value
                    println(value)
                },
                modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clipToBounds()
                    .onFocusEvent { state -> editing = state.hasFocus }
                    .onKeyEvent { event ->
                        println(event.key == Key.Enter)
                        println(event.type)
                        editing = !(event.key == Key.Enter)
                        !editing
                    }
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { editing = false }),
                textStyle = TextStyle(color = Color.White, fontSize = fontSize),
                cursorBrush = Brush.horizontalGradient(listOf(Color.White, Color.White))
            )
            if (shouldRequestFocus) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    shouldRequestFocus = false
                }
            }
        } else {
            Text(
                text = model.name,
                color = if (!selected) LocalContentColor.current.copy(alpha = 0.60f) else LocalContentColor.current,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clipToBounds(),
                softWrap = true,
                fontSize = fontSize,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        onClose?.also {
            Icon(
                Icons.Default.Close,
                tint = LocalContentColor.current.copy(alpha = 0.30f),
                contentDescription = "Close",
                modifier = Modifier
                    .heightIn(max = height)
                    .padding(end = 4.dp)
                    .clickable {
                        onClose()
                    }
            )
        }
    }
}


@ExperimentalComposeUiApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayerListView(
    layers: Layers,
    modifier: Modifier = Modifier
) {
    with(LocalDensity.current) {
        Box(modifier) {
            val scrollState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                state = scrollState
            ) {
                item {
                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    layers.load()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.surface
                                )
                            ) {
                                Text("Load")
                            }
                            Spacer(Modifier.width(30.dp))
                            Button(
                                onClick = {
                                    layers.save()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.surface
                                )
                            ) {
                                Text("Save")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    layers.addLayer()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.surface
                                )
                            ) {
                                Text("New Layer")
                            }
                        }
                    }
                }
                items(layers.map { it to it.item }) { (layer, item) ->
                    LayerListItem(14.sp, 14.sp.toDp()*1.5f, item, { layers.remove(layer) }
                    ) {
                        layers.selected = layer
                    }
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(scrollState),
                Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

class Layer(
    private val parent: Layers
) {

    class Item {
        var name: String by mutableStateOf("")
        var selected: Boolean by mutableStateOf(false)

        fun toLayerItemModel() = LayerItemModel(name, selected)
    }

    class PointData {
        var rawPoints: MutableList<Point> = mutableStateListOf()
        val hull: List<Point>
            get() = rawPoints.convexHull()
        val size: Int
            get() = rawPoints.size
    }

    class Stroke {
        var width: Dp by mutableStateOf(2.dp)
        var color: Color by mutableStateOf(Color.White)

        fun toLayerStrokeModel() = LayerStrokeModel(width.value, color.value)
    }

    var item: Item by mutableStateOf(Item())
    var points: PointData by mutableStateOf(PointData())
    var fill: Color by mutableStateOf(Color.Black)
    var stroke: Stroke by mutableStateOf(Stroke())
    val circleFill: Color
        get() = parent.circleFill

    fun toLayerModel() = LayerModel(
        item = item.toLayerItemModel(),
        points = points.rawPoints,
        fill = fill.value,
        stroke = stroke.toLayerStrokeModel()
    )
}

class Layers(
    private val window: ComposeWindow
) {
    private var _selected: Layer? by mutableStateOf(null)
    var selected: Layer?
        get() = _selected
        set(value) {
            selected?.apply {
                item.selected = false
            }
            value?.apply {
                item.selected = true
            }
            _selected = value
        }
    var layers: SnapshotStateList<Layer> = mutableStateListOf()
    val size: Int
        get() = layers.size
    var layerCounter: Long = 0
    var circleFill: Color by mutableStateOf(Color.White)
    var circleRadius: Float by mutableStateOf(10f)
    var background: Color by mutableStateOf(AppTheme.colors.backgroundDark)
    var backgroundImage: ImageBitmap? by mutableStateOf(null)
    var showTriangulation: Boolean by mutableStateOf(false)

    fun addLayer() {
        layerCounter++
        var newLayer = Layer(this)
        newLayer.item.name = "Layer $layerCounter"
        layers.add(newLayer)
    }

    operator fun iterator() = layers.iterator()
    fun <R> map(transform: (Layer) -> R) = layers.map(transform)
    fun remove(layer: Layer) = layers.remove(layer)

    fun toLayersModel() = LayersModel(
        selected = selected?.toLayerModel(),
        layers = layers.map { it.toLayerModel() },
        layerCounter = layerCounter,
        circleFill = circleFill.value,
        background = background.value
    )

    fun load() {
        FileDialog(window, "Load State", FileDialog.LOAD).apply {
            isVisible = true
        }.apply {
            file?.also { filename ->
                val path = directory + filename
                val file = File(path)
                Json { ignoreUnknownKeys = true }.decodeFromString<LayersModel>(file.readText()).toLayers(this@Layers)
            }
        }
    }

    fun loadImage() {
        FileDialog(window, "Load Image", FileDialog.LOAD).apply {
            isVisible = true
        }.apply {
            file?.also { filename ->
                val path = directory + filename
                backgroundImage = loadImageBitmap(File(path).inputStream())
            }
        }
    }

    fun save() {
        FileDialog(window, "Save Current State", FileDialog.SAVE).apply {
            file = "*.json"
            isVisible = true
        }.apply {
            file?.also { filename ->
                val path = directory + filename
                val file = File(path)
                file.createNewFile()
                file.writeText(Json.encodeToString(toLayersModel()))
            }
        }
    }
}

@Serializable
data class LayerItemModel(
    val name: String,
    val selected: Boolean
) {
    fun toLayerItem() = Layer.Item().also {
        it.name = name
        it.selected = false
    }
}

@Serializable
data class LayerStrokeModel(
    val width: Float,
    val color: ULong
) {
    fun toLayerStroke() = Layer.Stroke().also {
        it.width = width.dp
        it.color = Color(color)
    }
}

@Serializable
data class LayerModel(
    val item: LayerItemModel,
    val points: List<Point>,
    val fill: ULong,
    val stroke: LayerStrokeModel
) {
    fun toLayer(parent: Layers) = Layer(parent).also {
        it.item = item.toLayerItem()
        it.points = Layer.PointData().also { pd ->
            pd.rawPoints.addAll(points)
        }
        it.fill = Color(fill)
        it.stroke = stroke.toLayerStroke()
    }
}

@Serializable
data class LayersModel(
    val layers: List<LayerModel>,
    val selected: LayerModel?,
    val layerCounter: Long,
    val circleFill: ULong,
    val background: ULong
) {
    fun toLayers(layersDest: Layers) = layersDest
    .also {
        it.layers.apply {
            clear()
            addAll(layers.map { layerModel -> layerModel.toLayer(it) })
        }
        it.selected = null
        it.layerCounter = layerCounter
        it.circleFill = Color(circleFill)
        it.background = Color(background)
    }
}

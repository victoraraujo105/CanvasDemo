package dev.victor.canvas

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable

@Serializable
data class Point(
    var x: Float,
    var y: Float
) {
    operator fun minus(b: Point) = Point(
        x - b.x,
        y - b.y
    )

    operator fun plus(b: Point) = Point(
        x + b.x,
        y + b.y
    )

    operator fun div(k: Int) = Point(x/k, y/k)

//    fun equals(b: dev.victor.canvas.Point) = (x == b.x && y == b.y)

    fun toVector() = Vector(x, y)
    fun toOffset() = Offset(x, y)
}

sealed class Rotation {
    object Clockwise: Rotation()
    object Straight: Rotation()
    object CounterClockwise: Rotation()

    companion object {
        fun get(a: Point, b: Point, c: Point): Rotation {
            val ab = (b - a).toVector()
            val ac = (c - a).toVector()
            (ab cross ac).also {k ->
                return when {
                    k > 0f -> Clockwise
                    k == 0f -> Straight
                    else -> CounterClockwise
                }
            }
        }
    }
}



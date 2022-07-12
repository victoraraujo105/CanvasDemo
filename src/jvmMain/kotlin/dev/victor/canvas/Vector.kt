package dev.victor.canvas

import kotlin.math.sqrt

data class Vector(
    var x: Float,
    var y: Float
) {
    infix fun dot(b: Vector) = x*b.x + y*b.y
    infix fun cross(b: Vector) = x*b.y - y*b.x

    fun norm2() = this dot this
    fun norm() = sqrt(norm2())

    fun normalize(): Vector {
        val n = norm()
        x /= n
        y /= n
        return this
    }
}
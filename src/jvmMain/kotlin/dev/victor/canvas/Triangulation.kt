package dev.victor.canvas

import operations.determinant
import real.Matrix

data class Triangle(
    var a: Point,
    var b: Point,
    var c: Point
) {
    override fun equals(other: Any?): Boolean {
        return other?.let { other ->
           other is Triangle && other.toSet() == toSet()
        } ?: false
    }

    fun toSet() = setOf(a, b, c)
}


fun List<Point>.isTriangleValid(a: Point, b: Point, c: Point): Boolean {
    if (a == b || b == c || c == a) return false
    val pts = setOf(a, b, c)
    val a2 = a.toVector().norm2()
    val b2 = b.toVector().norm2()
    val c2 = c.toVector().norm2()
    val CCW = Rotation.get(a, b, c) is Rotation.CounterClockwise
    return this.filter { it !in pts }.all { p ->
        Matrix(
            4, 4, floatArrayOf(
                a.x, a.y, a2, 1f,
                b.x, b.y, b2, 1f,
                c.x, c.y, c2, 1f,
                p.x, p.y, p.toVector().norm2(), 1f,
            )
        ).determinant().run { if (CCW) this else -1*this } > 0
    }
}

fun List<Point>.getValidTriangle(a: Point, b: Point, c: Point? = null) = firstOrNull() { p ->
    p != c && isTriangleValid(a, b, p)
}?.let { p ->
    Triangle(a, b, p)
}
//fun List<Point>.getValidTriangles(a: Point, b: Point, c: Point? = null) = mapNotNull { p ->
//    if (p != c && isTriangleValid(a, b, p)) Triangle(a, b, p) else null
//}

fun List<Point>.getValidTriangle() = first().let { a ->
    var out: Triangle? = null
    first { b ->
        getValidTriangle(a, b)?.apply { out = this } != null
    }
    out
}

fun List<Point>.triangulation(): Set<Triangle>? {
    if (size < 3) return null
    val Q = mutableListOf(getValidTriangle()!!)
    var out = mutableSetOf<Triangle>()
    while(Q.isNotEmpty()) {

        Q.removeFirst().apply {
            out.add(this)
            listOfNotNull(
                getValidTriangle(b, c, a),
                getValidTriangle(a, c, b),
                getValidTriangle(a, b, c)
            ).filter { it !in out }.forEach { tri ->
                Q.add(tri)
            }
        }
    }
    return out
}
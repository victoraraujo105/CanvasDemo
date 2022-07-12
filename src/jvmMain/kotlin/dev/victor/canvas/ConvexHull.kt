package dev.victor.canvas

fun List<Point>.sum() = reduce(Point::plus)

fun List<Point>.barycenter() = sum()/size

fun List<Point>.sortedByPolarAngle(): List<Point> {
    return if (size < 2) toList() else {
        val p0 = minOfWith(
            comparator = { a, b ->
                when {
                    a.y < b.y || (a.y == b.y && a.x < b.x) -> -1
                    a == b -> 0
                    else -> 1
                }
            },
            selector = { it }
        )
        val i = Vector(1f, 0f)
        sortedBy { p ->
            (p - p0).toVector().normalize() dot i
        }
    }
}

fun List<Point>.convexHull(): List<Point> {
    return if (size < 2) toList() else {
        val sorted = sortedByPolarAngle()
        var hull = sorted.take(2).toMutableList()
        for (p in sorted.drop(2)) {
            while(hull.size > 1 && (Rotation.get(
                    hull[hull.lastIndex - 1],
                    hull.last(),
                    p
                ) !is Rotation.CounterClockwise)) {
                hull.removeLast()
            }
            hull.add(p)
        }
        hull
    }
}
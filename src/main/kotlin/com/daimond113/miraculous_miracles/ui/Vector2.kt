package com.daimond113.miraculous_miracles.ui

import kotlin.math.sqrt

class Vector2 internal constructor(var x: Float, var y: Float) {
    private val magnitude: Float
        get() = sqrt((x * x + y * y).toDouble()).toFloat()


    val normalized: Vector2
        get() = if (magnitude != 0f && magnitude != 1f) this / magnitude else this

    operator fun minus(vec: Vector2): Vector2 {
        return Vector2(x - vec.x, y - vec.y)
    }

    operator fun div(n: Float): Vector2 {
        return Vector2(x / n, y / n)
    }

    operator fun times(n: Float): Vector2 {
        return Vector2(x * n, y * n)
    }
}

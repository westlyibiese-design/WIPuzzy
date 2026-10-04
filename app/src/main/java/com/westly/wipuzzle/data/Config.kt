package com.westly.wipuzzle.data

import kotlin.math.max
import kotlin.math.roundToInt

const val TOTAL_LEVELS = 100

/** One campaign world: a contiguous range of levels sharing a grid size. */
data class World(val name: String, val min: Int, val max: Int, val size: Int) {
    val count: Int get() = max - min + 1
}

/** Every campaign level is 3x3. 4x4 exists only in Custom Puzzle mode. */
val WORLDS = listOf(
    World("Rookie Ruins", 1, 25, 3),
    World("Aurora Circuit", 26, 50, 3),
    World("Obsidian Rift", 51, 75, 3),
    World("Omnitrix Core", 76, 100, 3),
)

fun worldForLevel(level: Int): World =
    WORLDS.firstOrNull { level in it.min..it.max } ?: WORLDS.first()

fun sizeForLevel(level: Int): Int = worldForLevel(level).size

fun imageAssetPath(level: Int): String = "images/stage$level.jpg"
fun imageAssetUri(level: Int): String = "file:///android_asset/images/stage$level.jpg"

data class Par(val moves: Int, val seconds: Int)

fun parForSize(size: Int): Par {
    val tiles = size * size
    return Par(
        moves = (tiles * 4.5).roundToInt(),
        seconds = (tiles * 6.0).roundToInt(),
    )
}

fun starsFor(size: Int, moves: Int, seconds: Int): Int {
    val par = parForSize(size)
    return when {
        moves <= par.moves && seconds <= par.seconds -> 3
        moves <= par.moves * 1.6 && seconds <= par.seconds * 1.6 -> 2
        else -> 1
    }
}

fun scoreFor(size: Int, moves: Int, seconds: Int, stars: Int): Int {
    val tiles = size * size
    val par = parForSize(size)
    val base = tiles * 120
    val moveEff = max(0, par.moves - moves) * 15
    val timeEff = max(0, par.seconds - seconds) * 8
    val starBonus = stars * 250
    return max(base, base + moveEff + timeEff + starBonus)
}

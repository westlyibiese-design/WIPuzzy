package com.westly.wipuzzle.game

import kotlin.random.Random

/**
 * Sliding engine with an external holding slot.
 *
 * Positions are board indices (row-major, 0 until size*size) or [HOLD].
 * The board starts completely full and the holding slot is the single empty
 * position. The slot connects to exactly one board cell, the anchor
 * (top row, first column), so tiles enter or leave the board only there.
 */
class SlidingEngine(val size: Int) {
    companion object {
        const val EMPTY = -1
        const val HOLD = -2
    }

    init {
        require(size >= 2) { "Puzzle size must be at least 2x2" }
    }

    val tileCount: Int = size * size
    private val anchorIndex = 0

    private var board = IntArray(tileCount) { it }
    private var holdTile = EMPTY
    private var emptyPos = HOLD

    var moveCount: Int = 0
        private set

    fun isSolved(): Boolean {
        if (holdTile != EMPTY) return false
        for (i in board.indices) if (board[i] != i) return false
        return true
    }

    private fun movablePositions(): List<Int> {
        if (emptyPos == HOLD) return listOf(anchorIndex)
        val row = emptyPos / size
        val col = emptyPos % size
        val out = ArrayList<Int>(5)
        if (row > 0) out.add(emptyPos - size)
        if (row < size - 1) out.add(emptyPos + size)
        if (col > 0) out.add(emptyPos - 1)
        if (col < size - 1) out.add(emptyPos + 1)
        if (emptyPos == anchorIndex && holdTile != EMPTY) out.add(HOLD)
        return out
    }

    fun canMove(pos: Int): Boolean = movablePositions().contains(pos)

    /** Slides the tile at [pos] (board index or HOLD) into the empty position. */
    fun move(pos: Int, countMove: Boolean = true): Boolean {
        if (!canMove(pos)) return false
        if (pos == HOLD) {
            board[emptyPos] = holdTile
            holdTile = EMPTY
        } else if (emptyPos == HOLD) {
            holdTile = board[pos]
            board[pos] = EMPTY
        } else {
            board[emptyPos] = board[pos]
            board[pos] = EMPTY
        }
        emptyPos = pos
        if (countMove) moveCount++
        return true
    }

    /** Random legal moves from the solved state, so the result is always solvable. */
    fun shuffle(iterations: Int = 0) {
        board = IntArray(tileCount) { it }
        holdTile = EMPTY
        emptyPos = HOLD
        moveCount = 0

        val total = if (iterations > 0) iterations else maxOf(160, tileCount * 30)
        var lastEmpty = Int.MIN_VALUE
        repeat(total) {
            val all = movablePositions()
            val filtered = all.filter { it != lastEmpty }
            val pool = if (filtered.isNotEmpty()) filtered else all
            val pick = pool[Random.nextInt(pool.size)]
            lastEmpty = emptyPos
            move(pick, countMove = false)
        }
        if (isSolved()) {
            shuffle(total + 12)
            return
        }
        moveCount = 0
    }

    /** result[tile] = current position of that tile (board index or HOLD). */
    fun placement(): IntArray {
        val result = IntArray(tileCount) { EMPTY }
        for (i in board.indices) {
            val t = board[i]
            if (t != EMPTY) result[t] = i
        }
        if (holdTile != EMPTY) result[holdTile] = HOLD
        return result
    }
}

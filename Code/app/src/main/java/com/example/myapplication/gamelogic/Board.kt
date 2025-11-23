package com.example.myapplication.gamelogic

class Board(val size: Int = 10) {
    val grid: Array<Array<Cell>> = Array(size) { row ->
        Array(size) { col -> Cell(row, col) }
    }

    val geckos = mutableListOf<Gecko>()

    fun placeGecko(gecko: Gecko, startRow: Int, startCol: Int): Boolean {
        val positions = mutableListOf<Cell>()

        for (i in 0 until gecko.size) {
            val r = if (gecko.orientation == Orientation.VERTICAL) startRow + i else startRow
            val c = if (gecko.orientation == Orientation.HORIZONTAL) startCol + i else startCol

            if (r !in 0 until size || c !in 0 until size || grid[r][c].hasGecko) {
                return false // Invalid placement
            }

            positions.add(grid[r][c])
        }

        positions.forEach {
            it.hasGecko = true
        }

        gecko.positions.addAll(positions)
        geckos.add(gecko)
        return true
    }

    fun attack(row: Int, col: Int): Boolean {
        val cell = grid[row][col]
        cell.isHit = true
        return cell.hasGecko
    }

    fun allGeckosDefeated(): Boolean = geckos.all { it.isDefeated }
}

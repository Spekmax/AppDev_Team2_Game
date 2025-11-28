package com.example.myapplication.gamelogic
class Gecko(
    val name: String,
    val size: Int,
    var orientation: Orientation,
    val positions: MutableList<Cell> = mutableListOf()
) { val isDefeated: Boolean
        get() = positions.all { it.isHit }
}

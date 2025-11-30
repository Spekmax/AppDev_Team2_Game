package com.example.myapplication.gamelogic

data class Cell(
    val row: Int,
    val column: Int,
    var hasGecko: Boolean = false,
    var isHit: Boolean = false
)

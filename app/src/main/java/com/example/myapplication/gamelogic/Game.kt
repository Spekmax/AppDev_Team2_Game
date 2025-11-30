package com.example.myapplication.gamelogic

class Game(val player1: Player, val player2: Player) {
    private var currentPlayer = player1

    fun takeTurn(row: Int, col: Int): Boolean {
        val opponent = if (currentPlayer == player1) player2 else player1
        val hit = opponent.board.attack(row, col)

        if (!hit) {
            currentPlayer = opponent // Switch turns
        }

        return hit
    }

    fun checkWinner(): Player? {
        return when {
            player1.board.allGeckosDefeated() -> player2
            player2.board.allGeckosDefeated() -> player1
            else -> null
        }
    }
}

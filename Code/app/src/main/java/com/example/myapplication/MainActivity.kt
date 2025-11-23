package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.myapplication.gamelogic.Game
import com.example.myapplication.gamelogic.Player


class MainActivity : ComponentActivity() {
    private lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val player1 = Player("You")
        val player2 = Player("Computer")
        game = Game(player1, player2)

        setContent {
            Text("Battle Gecko's!")
        }
    }

    private fun onCellClicked(row: Int, col: Int) {
        val hit = game.takeTurn(row, col)

        if (hit) {
            // update UI to show hit
        } else {
            // update UI to show miss
        }

        game.checkWinner()?.let { winner ->
            Toast.makeText(this, "${winner.name} wins!", Toast.LENGTH_SHORT).show()
        }
    }
}
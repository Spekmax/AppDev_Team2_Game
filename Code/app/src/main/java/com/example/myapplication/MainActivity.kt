package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.content.Intent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.example.myapplication.gamelogic.*

import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var game: Game
    private lateinit var logoutButton: Button
    private lateinit var boardGrid: GridLayout
    private lateinit var tileViews: Array<Array<ImageView>>

    private lateinit var btnRotate: ImageButton
    private lateinit var btnConfirm: ImageButton
    private lateinit var btnAttack: ImageButton

    private lateinit var gecko2View: ImageView
    private lateinit var gecko3View: ImageView
    private lateinit var gecko4View: ImageView

    private var currentOrientation = Orientation.VERTICAL
    private var gameState = GameState.PLACING

    private var selectedGeckoSize = 0

    private var gecko2Used = false
    private var gecko3Used = false
    private var gecko4Used = false

    private var geckosPlaced = 0
    private val totalGeckos = 3

    private var previewCells = mutableListOf<Pair<Int, Int>>()

    private val geckoPieces = mapOf(
        2 to listOf(R.drawable.gecko_2_1, R.drawable.gecko_2_2),

        3 to listOf(
            R.drawable.gecko_3_1,
            R.drawable.gecko_3_2,
            R.drawable.gecko_3_3
        ),

        4 to listOf(
            R.drawable.gecko_4_1,
            R.drawable.gecko_4_2,
            R.drawable.gecko_4_3,
            R.drawable.gecko_4_4
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load layout BEFORE findViewById
        setContentView(R.layout.activity_main)

        // Now we can safely get view references
        logoutButton = findViewById(R.id.logoutButton)
        boardGrid = findViewById(R.id.boardGrid)
        btnRotate = findViewById(R.id.btnRotate)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnAttack = findViewById(R.id.btnAttack)

        gecko2View = findViewById(R.id.gecko2)
        gecko3View = findViewById(R.id.gecko3)
        gecko4View = findViewById(R.id.gecko4)

        // Logout logic
        logoutButton.setOnClickListener {
            val prefs = getSharedPreferences("users", MODE_PRIVATE)
            prefs.edit().remove("loggedInUser").apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val player1 = Player("You")
        val player2 = Player("Computer")
        game = Game(player1, player2)

        setupBoard()
        setupButtons()
        setupGeckoSelector()
    }

    /* ---------------------------- GECKO SELECTOR UI ---------------------------- */

    private fun setupGeckoSelector() {

        fun refreshAlpha() {
            gecko2View.alpha = if (gecko2Used) 0.2f else 0.5f
            gecko3View.alpha = if (gecko3Used) 0.2f else 0.5f
            gecko4View.alpha = if (gecko4Used) 0.2f else 0.5f
        }

        gecko2View.setOnClickListener {
            if (!gecko2Used) {
                refreshAlpha()
                gecko2View.alpha = 1f
                selectedGeckoSize = 2
            }
        }

        gecko3View.setOnClickListener {
            if (!gecko3Used) {
                refreshAlpha()
                gecko3View.alpha = 1f
                selectedGeckoSize = 3
            }
        }

        gecko4View.setOnClickListener {
            if (!gecko4Used) {
                refreshAlpha()
                gecko4View.alpha = 1f
                selectedGeckoSize = 4
            }
        }

        refreshAlpha()
    }

    /* ---------------------------- BOARD SETUP ---------------------------- */

    private fun setupBoard() {
        val size = 10
        tileViews = Array(size) { Array(size) { ImageView(this) } }

        val boardContainer = findViewById<View>(R.id.boardContainer)

        boardContainer.post {
            val tileSize = min(boardContainer.measuredWidth, boardContainer.measuredHeight) / size

            boardGrid.removeAllViews()
            boardGrid.rowCount = size
            boardGrid.columnCount = size

            for (r in 0 until size) {
                for (c in 0 until size) {
                    val tile = ImageView(this).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = tileSize
                            height = tileSize
                            setMargins(1, 1, 1, 1)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setImageResource(R.drawable.tile)
                        setOnClickListener { onCellClicked(r, c) }
                    }
                    tileViews[r][c] = tile
                    boardGrid.addView(tile)
                }
            }
        }
    }

    /* ---------------------------- BUTTON LOGIC ---------------------------- */

    private fun setupButtons() {

        btnRotate.setOnClickListener {
            currentOrientation =
                if (currentOrientation == Orientation.VERTICAL) Orientation.HORIZONTAL
                else Orientation.VERTICAL

            if (previewCells.isNotEmpty()) {
                val (r, c) = previewCells.first()
                showGeckoPreview(r, c)
            }
        }

        btnConfirm.setOnClickListener {

            if (gameState != GameState.PLACING) return@setOnClickListener

            if (selectedGeckoSize == 0) {
                Toast.makeText(this, "Select a gecko first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (previewCells.isEmpty()) {
                Toast.makeText(this, "Tap a tile to preview!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val placed = placeGecko()

            if (!placed) {
                Toast.makeText(this, "Cannot place gecko there.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (selectedGeckoSize) {
                2 -> gecko2Used = true
                3 -> gecko3Used = true
                4 -> gecko4Used = true
            }

            selectedGeckoSize = 0
            clearPreview()
            previewCells.clear()

            geckosPlaced++
            if (geckosPlaced >= totalGeckos) {
                gameState = GameState.PLAYER_TURN
                Toast.makeText(this, "All geckos placed!", Toast.LENGTH_SHORT).show()
            }
        }

        btnAttack.setOnClickListener {
            if (gameState == GameState.PLAYER_TURN) {
                gameState = GameState.SELECTING_ATTACK_TILE
                Toast.makeText(this, "Tap a tile to attack!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /* ---------------------------- TAP LOGIC ---------------------------- */

    private fun onCellClicked(row: Int, col: Int) {
        when (gameState) {
            GameState.PLACING -> showGeckoPreview(row, col)
            GameState.SELECTING_ATTACK_TILE -> {
                handlePlayerAttack(row, col)
                gameState = GameState.PLAYER_TURN
            }
            else -> {}
        }
    }

    /* ---------------------------- PREVIEW ---------------------------- */

    private fun showGeckoPreview(row: Int, col: Int) {
        clearPreview()
        previewCells.clear()

        val size = selectedGeckoSize
        if (size == 0) return

        val board = game.player1.board

        if (size == 4) {
            val cells = listOf(
                row to col,
                row to col + 1,
                row + 1 to col,
                row + 1 to col + 1
            )

            if (cells.any { (r, c) -> r !in 0..9 || c !in 0..9 || board.grid[r][c].hasGecko })
                return

            previewCells.addAll(cells)

        } else {
            for (i in 0 until size) {
                val r = if (currentOrientation == Orientation.VERTICAL) row + i else row
                val c = if (currentOrientation == Orientation.HORIZONTAL) col + i else col

                if (r !in 0..9 || c !in 0..9 || board.grid[r][c].hasGecko)
                    return

                previewCells.add(r to c)
            }
        }

        previewCells.forEach { (r, c) ->
            tileViews[r][c].alpha = 0.5f
            tileViews[r][c].setImageResource(R.drawable.tile)
        }
    }

    private fun clearPreview() {
        previewCells.forEach { (r, c) ->
            if (!game.player1.board.grid[r][c].hasGecko) {
                tileViews[r][c].alpha = 1f
                tileViews[r][c].setImageResource(R.drawable.tile)
            }
        }
    }

    /* ---------------------------- PLACE GECKO ---------------------------- */

    private fun placeGecko(): Boolean {

        val size = selectedGeckoSize
        val board = game.player1.board
        val pieces = geckoPieces[size] ?: return false

        // must not overlap
        if (previewCells.any { (r, c) -> board.grid[r][c].hasGecko })
            return false

        val gecko = Gecko("G$geckosPlaced", size, currentOrientation)

        if (size == 4) {

            val (r, c) = previewCells[0]

            val order = listOf(
                r to (c + 1),      // top-right
                r to c,            // top-left
                (r + 1) to c,      // bottom-left
                (r + 1) to (c + 1) // bottom-right
            )

            order.forEachIndexed { i, (rr, cc) ->
                val cell = board.grid[rr][cc]
                cell.hasGecko = true
                gecko.positions.add(cell)

                tileViews[rr][cc].apply {
                    rotation = 0f
                    alpha = 1f
                    scaleType = ImageView.ScaleType.FIT_XY
                    setImageResource(pieces[i])
                }
            }

        } else {

            previewCells.forEachIndexed { i, (r, c) ->
                val cell = board.grid[r][c]
                cell.hasGecko = true
                gecko.positions.add(cell)

                tileViews[r][c].apply {
                    alpha = 1f
                    scaleType = ImageView.ScaleType.FIT_XY
                    setImageResource(pieces[i])
                    rotation =
                        if (currentOrientation == Orientation.HORIZONTAL) -90f else 0f
                }
            }
        }

        board.geckos.add(gecko)
        return true
    }

    /* ---------------------------- ATTACK ---------------------------- */

    private fun handlePlayerAttack(row: Int, col: Int) {
        val hit = game.takeTurn(row, col)
        val tile = tileViews[row][col]

        val frames = if (hit)
            intArrayOf(R.drawable.hit_1, R.drawable.hit_2, R.drawable.hit_3)
        else
            intArrayOf(R.drawable.miss_1, R.drawable.miss_2, R.drawable.miss_3)

        animateTile(tile, frames)

        game.checkWinner()?.let {
            gameState = GameState.GAME_OVER
            Toast.makeText(this, "${it.name} wins!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateTile(tile: ImageView, frames: IntArray, delay: Long = 80L) {
        var index = 0
        tile.post(object : Runnable {
            override fun run() {
                tile.setImageResource(frames[index])
                index++
                if (index < frames.size) tile.postDelayed(this, delay)
            }
        })
    }
}
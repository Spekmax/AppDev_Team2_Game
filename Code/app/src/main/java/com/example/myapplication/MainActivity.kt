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
import com.example.myapplication.gamelogic.ComputerPlayer
import android.os.Handler
import android.os.Looper

import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var game: Game
    private lateinit var logoutButton: Button

    private lateinit var playerBoardGrid: GridLayout

    private lateinit var attackBoardGrid: GridLayout

    private lateinit var playerTileViews: Array<Array<ImageView>>

    private lateinit var attackTileViews: Array<Array<ImageView>>

    private lateinit var computerPlayer: ComputerPlayer

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
        playerBoardGrid = findViewById(R.id.playerBoardGrid)
        attackBoardGrid = findViewById(R.id.attackBoardGrid)
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

        computerPlayer = ComputerPlayer()
        computerPlayer.placeGeckosRandomly(game.player2.board)

        debugShowComputerGeckos() //uncomment to see computer's geckos in logs

        setupBoard()
        setupButtons()
        setupGeckoSelector()

        switchToBoard(showPlayerBoard = true)
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
        playerTileViews = Array(size) { Array(size) { ImageView(this) } }
        attackTileViews = Array(size) { Array(size) { ImageView(this) } }

        val boardContainer = findViewById<View>(R.id.boardsFrameLayout)

        boardContainer.post {
            val tileSize = min(boardContainer.measuredWidth, boardContainer.measuredHeight) / size

            // Setup PLAYER BOARD (shows your geckos)
            setupSingleBoard(playerBoardGrid, playerTileViews, tileSize, size, isPlayerBoard = true)

            // Setup ATTACK BOARD (shows your attacks)
            setupSingleBoard(attackBoardGrid, attackTileViews, tileSize, size, isPlayerBoard = false)
        }
    }

    private fun setupSingleBoard(
        grid: GridLayout,
        tiles: Array<Array<ImageView>>,
        tileSize: Int,
        size: Int,
        isPlayerBoard: Boolean
    ) {
        grid.removeAllViews()
        grid.rowCount = size
        grid.columnCount = size

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
                tiles[r][c] = tile
                grid.addView(tile)
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
                Toast.makeText(this, "All geckos placed! Your turn to attack", Toast.LENGTH_SHORT).show()
            }
        }

        btnAttack.setOnClickListener {
            if (gameState == GameState.PLAYER_TURN) {
                gameState = GameState.SELECTING_ATTACK_TILE
                switchToBoard(showPlayerBoard = false)
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
            playerTileViews[r][c].alpha = 0.5f
            playerTileViews[r][c].setImageResource(R.drawable.tile)
        }
    }

    private fun clearPreview() {
        previewCells.forEach { (r, c) ->
            if (!game.player1.board.grid[r][c].hasGecko) {
                playerTileViews[r][c].alpha = 1f
                playerTileViews[r][c].setImageResource(R.drawable.tile)
            }
        }
    }

    private fun switchToBoard(showPlayerBoard: Boolean) {
        if (showPlayerBoard) {
            playerBoardGrid.visibility = View.VISIBLE
            attackBoardGrid.visibility = View.GONE
        } else {
            playerBoardGrid.visibility = View.GONE
            attackBoardGrid.visibility = View.VISIBLE
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

                playerTileViews[rr][cc].apply {
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

                playerTileViews[r][c].apply {
                    alpha = 1f
                    scaleType = ImageView.ScaleType.FIT_XY
                    setImageResource(pieces[i])
                    rotation = if (currentOrientation == Orientation.HORIZONTAL) -90f else 0f
                }
            }
        }

        board.geckos.add(gecko)
        return true
    }

    /* ---------------------------- ATTACK ---------------------------- */

    private fun handlePlayerAttack(row: Int, col: Int) {
        if (game.player2.board.grid[row][col].isHit) {
            Toast.makeText(this, "Already attacked this cell!", Toast.LENGTH_SHORT).show()
            return
        }

        val hit = game.player2.board.attack(row, col)
        val tile = attackTileViews[row][col]

        val frames = if (hit)
            intArrayOf(R.drawable.hit_1, R.drawable.hit_2, R.drawable.hit_3)
        else
            intArrayOf(R.drawable.miss_1, R.drawable.miss_2, R.drawable.miss_3)

        animateTile(tile, frames)

        game.checkWinner()?.let {
            gameState = GameState.GAME_OVER
            Toast.makeText(this, "${it.name} wins!", Toast.LENGTH_SHORT).show()
            return
        }

        if (hit) {
            gameState = GameState.PLAYER_TURN
            Toast.makeText(this, "Hit! Go again!", Toast.LENGTH_SHORT).show()
        } else {
            gameState = GameState.COMPUTER_TURN
            Toast.makeText(this, "Miss! Computer's turn", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                executeComputerTurn()
            }, 1500)
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

    private fun executeComputerTurn() {
        if (gameState != GameState.COMPUTER_TURN) return

        switchToBoard(showPlayerBoard = true)

        val (row, col) = computerPlayer.decideNextAttack()

        val hit = game.player1.board.attack(row, col)

        val geckoDefeated = game.player1.board.geckos.any { gecko ->
            gecko.positions.any { it.row == row && it.column == col }
                    && gecko.isDefeated
        }

        computerPlayer.processAttackResult(row, col, hit, geckoDefeated)

        val tile = playerTileViews[row][col]
        val frames = if (hit)
            intArrayOf(R.drawable.hit_1, R.drawable.hit_2, R.drawable.hit_3)
        else
            intArrayOf(R.drawable.miss_1, R.drawable.miss_2, R.drawable.miss_3)

        animateTile(tile, frames)

        val message = if (hit) "Computer hit your gecko at ($row, $col)!"
        else "Computer missed at ($row, $col)"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        game.checkWinner()?.let {
            gameState = GameState.GAME_OVER
            Toast.makeText(this, "${it.name} wins!", Toast.LENGTH_LONG).show()
            return
        }

        // turn management system
        if (hit) {
            Handler(Looper.getMainLooper()).postDelayed({
                executeComputerTurn()
            }, 1500)
        } else {
            gameState = GameState.PLAYER_TURN
            Toast.makeText(this, "Your turn!", Toast.LENGTH_SHORT).show()
        }
    }

    /* ---------------------------- DEBUG, SHOW ENEMY GECKOS ---------------------------- */
    private fun debugShowComputerGeckos() {
        println("========== COMPUTER'S GECKOS ==========")

        val computerBoard = game.player2.board

        computerBoard.geckos.forEachIndexed { index, gecko ->
            println("\nGecko ${index + 1}: ${gecko.name}")
            println("  Size: ${gecko.size}")
            println("  Orientation: ${gecko.orientation}")
            println("  Positions:")

            gecko.positions.forEach { cell ->
                println("    - Row ${cell.row}, Col ${cell.column}")
            }
        }

        println("\n========== VISUAL GRID ==========")
        for (row in 0 until 10) {
            val rowString = buildString {
                for (col in 0 until 10) {
                    if (computerBoard.grid[row][col].hasGecko) {
                        append("G ")
                    } else {
                        append(". ")
                    }
                }
            }
            println("Row $row: $rowString")
        }
        println("=======================================")
    }
}
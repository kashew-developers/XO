package `in`.kashewdevelopers.xo

import `in`.kashewdevelopers.xo.databinding.ActivityGamePlayBinding
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class GamePlayActivity : AppCompatActivity() {

    // For X, player = true, for O player = false
    private var playerXChance = true

    private var playWithAI = false
    private var xVictoryCount = 0
    private var oVictoryCount = 0

    // Moves played
    private var numberOfMovesPlayed = 0

    // Grid Data - Stores all moves made by player
    private var board = Array(3) { IntArray(3) }


    private var backPressed = false
    private lateinit var backPressedToast: Toast

    private enum class StrikeType { ROW, COLUMN, DIAGONAL }

    private lateinit var binding: ActivityGamePlayBinding

    // widgets
    private lateinit var gameBlocks: Array<ImageView>
    private lateinit var strikes: Array<TextView>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()

        // Get Game Mode
        playWithAI = intent.getBooleanExtra(getString(R.string.playWithAI), false)

        // randomly generate a chance
        // 1st player is always player X
        // AI or Opponent is player O
        playerXChance = Random.nextBoolean()

        changeHeading()

        if (playWithAI && (!playerXChance)) {
            aiChance()
        }
    }

    override fun onBackPressed() {
        if (backPressed) {
            backPressedToast.cancel()
            super.onBackPressed()
            return
        }

        backPressed = true
        backPressedToast.show()
        @Suppress("DEPRECATION")
        Handler().postDelayed({ backPressed = false }, 2000)
    }


    // initialization
    @SuppressLint("ShowToast")
    private fun initialization() {
        initializeWidgets()

        backPressedToast = Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT)

        binding.grid.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.grid.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setGridSize()
            }
        })
    }

    private fun initializeWidgets() {
        gameBlocks = arrayOf(binding.r0c0, binding.r0c1, binding.r0c2, binding.r1c0,
                binding.r1c1, binding.r1c2, binding.r2c0, binding.r2c1, binding.r2c2)

        strikes = arrayOf(binding.col0Strike, binding.col1Strike, binding.col2Strike,
                binding.row0Strike, binding.row1Strike, binding.row2Strike,
                binding.primaryDiagonalStrike, binding.secondaryDiagonalStrike)

        configureWidgetVisibility()
    }

    private fun configureWidgetVisibility() {
        for (strike in strikes) {
            strike.visibility = View.GONE
        }
        binding.scoreSection.visibility = View.INVISIBLE
    }


    // functionality
    private fun makeMove(block: ImageView, r: Int, c: Int) {
        numberOfMovesPlayed++

        block.isClickable = false

        // Set X or O
        block.setImageResource(if (playerXChance) R.drawable.x_icon else R.drawable.o_icon)
        block.tag = if (playerXChance) getString(R.string.x) else getString(R.string.o)

        // Store move made by player to board matrix
        // Set Matrix value, Player X = 1, Player Y = 2, Empty Block = 0
        board[r][c] = if (playerXChance) 1 else 2

        // Change Player
        playerXChance = !playerXChance

        // Change Heading
        changeHeading()

        /*
         * evaluate game & save result
         * if result == -1, continue game
         * if result == 0, draw
         * if result == 1, player X won
         * if result == 2, player O won
         *
         * check game mode, if playWithAI = true
         * check current player, if playerXChance = false
         * its AI's chance
         */
        val result = evaluate(true)
        if (result == -1) {
            if (playWithAI && (!playerXChance)) {
                aiChance()
            }
        } else {
            gameOver(result)
        }
    }

    private fun evaluate(cut: Boolean): Int {
        // Check Rows
        for (i in 0..3) {
            if (board[i][0] != 0 && equals(board[i][0], board[i][1], board[i][2])) {
                if (cut) makeCuts(StrikeType.ROW, i)
                return board[i][0]
            }
        }

        // Check Columns
        for (i in 0..2) {
            if (board[0][i] != 0 && equals(board[0][i], board[1][i], board[2][i])) {
                if (cut) makeCuts(StrikeType.COLUMN, i)
                return board[0][i]
            }
        }

        // Check Diagonals
        if (board[0][0] != 0 && equals(board[0][0], board[1][1], board[2][2])) {
            if (cut) makeCuts(StrikeType.DIAGONAL, 0)
            return board[0][0]
        } else if (board[0][2] != 0 && equals(board[0][2], board[1][1], board[2][0])) {
            if (cut) makeCuts(StrikeType.DIAGONAL, 1)
            return board[0][2]
        }

        // Draw Game
        if (numberOfMovesPlayed == 9)
            return 0

        return -1
    }

    private fun equals(a: Int, b: Int, c: Int): Boolean = (a == b && b == c)

    private fun gameOver(action: Int) {
        // This method is called when the game is over
        // Either a player wins, or all the blocks are filled

        // Disable all blocks
        disableAllBlocks()

        // if action == 1, player X won
        // if action == 2, player O won
        // else all blocks are filled, Draw game
        when (action) {
            1 -> {
                binding.heading.setText(if (playWithAI) R.string.you_won else R.string.player_x_won)
                xVictoryCount++
            }
            2 -> {
                binding.heading.setText(if (playWithAI) R.string.ai_won else R.string.player_o_won)
                oVictoryCount++
            }
            0 -> binding.heading.setText(R.string.draw_game)
        }

        binding.scoreSection.visibility = View.VISIBLE
        binding.yourLabel.text = "$xVictoryCount"
        binding.yourScore.setText(if (playWithAI) R.string.you else R.string.player_x)
        binding.opponentLabel.text = "$oVictoryCount"
        binding.opponentScore.setText(if (playWithAI) R.string.ai else R.string.player_o)

        when {
            xVictoryCount > oVictoryCount -> {
                binding.yourLabel.setTypeface(null, Typeface.BOLD_ITALIC)
                binding.opponentLabel.setTypeface(null, Typeface.NORMAL)
            }
            xVictoryCount < oVictoryCount -> {
                binding.yourLabel.setTypeface(null, Typeface.NORMAL)
                binding.opponentLabel.setTypeface(null, Typeface.BOLD_ITALIC)
            }
            else -> {
                binding.yourLabel.setTypeface(null, Typeface.NORMAL)
                binding.opponentLabel.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun aiChance() {
        disableAllBlocks()
        binding.reset.isEnabled = false

        @Suppress("DEPRECATION")
        Handler().postDelayed({
            val row: Int
            val column: Int

            if (numberOfMovesPlayed < 1) {
                row = Random.nextInt(3)
                column = Random.nextInt(3)
            } else {
                val ans = minimax(10, playerXChance)
                row = ans / 3
                column = ans % 3
            }

            makeMove(gameBlocks[3 * row + column], row, column)
            enableAllEmptyBlocks()
            binding.reset.isEnabled = true
        }, 400)
    }

    private fun minimax(depth: Int, curPlayer: Boolean): Int {
        var finalDepth: Int? = null
        var finalPos = 0
        var tempDepth: Int

        for (i in 0..2) {
            for (j in 0..2) {

                if (board[i][j] == 0) {
                    board[i][j] = if (curPlayer) 1 else 2
                    numberOfMovesPlayed++

                    val result = evaluate(false)
                    tempDepth = if (result != -1) {
                        if (result == 0) 0
                        else (if (curPlayer == playerXChance) 1 else -1) * depth
                    } else {
                        minimax(depth - 1, !curPlayer)
                    }

                    board[i][j] = 0
                    numberOfMovesPlayed--

                    finalDepth = finalDepth ?: tempDepth

                    if (curPlayer == playerXChance) {
                        if (finalDepth <= tempDepth) {
                            finalDepth = tempDepth
                            finalPos = i * 3 + j
                        }
                        if (finalDepth == depth) break
                    } else {
                        if (finalDepth >= tempDepth) {
                            finalDepth = tempDepth
                            finalPos = i * 3 + j
                        }
                        if (finalDepth == -depth) break
                    }
                }
            }
        }
        return if (depth == 10) finalPos else finalDepth ?: 0
    }


    // handle UI functionality
    private fun changeHeading() {
        if (playerXChance) {
            binding.heading.setText(if (playWithAI) R.string.your_chance else R.string.x_chance)
        } else {
            binding.heading.setText(if (playWithAI) R.string.ai_chance else R.string.o_chance)
        }
    }

    private fun setGridSize() {
        val height = binding.grid.measuredHeight
        val width = binding.grid.measuredWidth
        val min = width.coerceAtMost(height)

        val layoutParams = binding.grid.layoutParams
        layoutParams.height = min
        layoutParams.width = min
        binding.grid.layoutParams = layoutParams
    }

    private fun disableAllBlocks() {
        for (block in gameBlocks) {
            block.isClickable = false
        }
    }

    private fun enableAllEmptyBlocks() {
        for (block in gameBlocks) {
            block.tag?.let { block.isClickable = true }
        }
    }

    private fun makeCuts(strikeType: StrikeType, strikeIndex: Int) {
        var index = 0
        if (strikeType == StrikeType.ROW) {
            index = strikeIndex + 3
        } else if (strikeType == StrikeType.DIAGONAL) {
            index = strikeIndex + 6
        }

        strikes[index].visibility = View.VISIBLE
    }


    // handle widget clicks
    fun onBlockClicked(clickedBlock: View) {
        // if a block is clicked, find which block was clicked
        // send that blocks view & coordinates to makeMove method
        var index = 0
        for (block in gameBlocks) {
            if (block.id == clickedBlock.id) break
            index++
        }

        val row = index / 3
        val col = index % 3
        makeMove(clickedBlock as ImageView, row, col)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onResetClicked(view: View) {
        // Set initial values
        numberOfMovesPlayed = 0
        board = Array(3) { IntArray(3) }

        playerXChance = Random.nextBoolean()

        // Show Heading
        changeHeading()

        // Enable all blocks & Set Block text to blank
        for (block in gameBlocks) {
            with(block) {
                isClickable = true
                setImageResource(0)
                tag = null
            }
        }

        // remove all strikes
        for (each in strikes) {
            each.visibility = View.GONE
        }

        if (playWithAI && (!playerXChance)) {
            aiChance()
        }
    }

}
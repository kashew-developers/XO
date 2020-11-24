package `in`.kashewdevelopers.xo

import `in`.kashewdevelopers.xo.databinding.ActivityGamePlayBinding
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlin.random.Random

class GameFriend : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var gameRoom: GameRoom

    private var gameRoomId: String? = null
    var name: String? = null
    var createdGame = false

    private lateinit var progressDialog: ProgressDialog

    private lateinit var binding: ActivityGamePlayBinding

    // widgets
    private lateinit var gameBlocks: Array<ImageView>
    private lateinit var strikes: Array<TextView>

    private var numberOfMovesPlayed = 0
    private var creatorVictoryCount = 0
    private var joineeVictoryCount = 0

    private enum class StrikeType { ROW, COLUMN, DIAGONAL }

    // Grid Data - Stores all moves made by player
    private var board = Array(3) { IntArray(3) }

    private var backPressed = false
    private lateinit var backPressedToast: Toast
    private lateinit var opponentLeftToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()

        with(progressDialog) {
            setTitle("Starting Game")
            setMessage("please wait")
            setCancelable(false)
            show()
        }

        initializeDbElements()
    }

    override fun onBackPressed() {
        if (backPressed) {
            backPressedToast.cancel()
            super.onBackPressed()
            return
        }

        backPressed = true
        backPressedToast.show()
        Handler().postDelayed({ backPressed = false }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseReference.removeEventListener(valueEventListener)
        val updateData = mapOf<String, Any>("leftGame" to true)
        databaseReference.updateChildren(updateData)
    }


    // initialization
    @SuppressLint("ShowToast")
    private fun initialization() {
        progressDialog = ProgressDialog(this)

        backPressedToast = Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT)
        opponentLeftToast = Toast.makeText(this, R.string.opponent_player_left, Toast.LENGTH_LONG)
        opponentLeftToast.setGravity(Gravity.CENTER, 0, 0)

        initializeWidgets()

        // get data from intent
        gameRoomId = intent.getStringExtra("gameRoomId")
        createdGame = intent.getBooleanExtra("createGame", false)
        name = intent.getStringExtra("name")
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

    private fun initializeDbElements() {
        // initialize database variables
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("game_rooms").child(gameRoomId ?: "")

        // database change listener
        valueEventListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gameRoom = dataSnapshot.getValue(GameRoom::class.java)!!

                // wait until both players have joined the game room
                if ((createdGame && gameRoom.joineeName == null) ||
                        (!createdGame && gameRoom.creatorName == null)) {
                    with(progressDialog) {
                        setTitle("Starting Game")
                        setMessage("waiting for 2nd player to join")
                        setCancelable(false)
                        show()
                    }
                    return
                }

                progressDialog.dismiss()

                analyzeData()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        databaseReference.addValueEventListener(valueEventListener)

        // insert creator's / joinee's data
        val initialData = mutableMapOf<String, Any?>()
        if (createdGame) {
            initialData["creatorChance"] = Random.nextBoolean()
            initialData["creatorName"] = name
        } else {
            initialData["joineeName"] = name
        }
        initialData["gameFinished"] = false
        initialData["newGame"] = true

        databaseReference.updateChildren(initialData)
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@GameFriend, "Error starting game, please try again", Toast.LENGTH_SHORT).show()
                    backPressed = true
                    onBackPressed()
                }
    }


    // functionality
    private fun equals(a: Int, b: Int, c: Int): Boolean = (a == b && b == c)

    private fun analyzeData() {
        if (gameRoom.leftGame == true) {
            backPressed = true
            onBackPressed()
            opponentLeftToast.show()
            return
        }

        if (gameRoom.gameFinished) {
            disableAllBlocks()
            // game has ended, IF now it is my chance
            // this means the game ended due to the previous move played
            // by the opponent, show the opponent's move
            if (createdGame == gameRoom.creatorChance) {
                if (gameRoom.movePlayed != null) {
                    val r = (gameRoom.movePlayed ?: 0) / 3
                    val c = (gameRoom.movePlayed ?: 0) % 3
                    makeMove(gameBlocks[(gameRoom.movePlayed ?: 0)], r, c, false)
                }
            }
            binding.reset.visibility = View.VISIBLE
            return
        }

        // newGame = True, when any one player Resets the games
        if (gameRoom.newGame) {
            binding.reset.visibility = View.INVISIBLE
            numberOfMovesPlayed = 0
            board = Array(3) { IntArray(3) }
            gameBlocks.forEach {
                it.tag = null
                it.setImageResource(0)
                it.isClickable = true
            }

            strikes.forEach {
                it.visibility = View.GONE
            }
        }

        changeHeading()

        // if it is my chance
        //    1. disable all blocks
        //    2. emulate opponent's previous move (if any)
        //    3. enable empty blocks
        // else, disable all blocks (waiting for other player)
        if (createdGame == gameRoom.creatorChance) {
            disableAllBlocks()
            if (gameRoom.movePlayed != null) {
                val r = (gameRoom.movePlayed ?: 0) / 3
                val c = (gameRoom.movePlayed ?: 0) % 3
                makeMove(gameBlocks[gameRoom.movePlayed ?: 0], r, c, false)
            }
            enableAllEmptyBlocks()
        } else {
            disableAllBlocks()
        }
    }

    private fun makeMove(block: ImageView, r: Int, c: Int, myTurn: Boolean) {
        numberOfMovesPlayed++

        block.isClickable = false

        // Set X or O
        block.setImageResource(if (createdGame == myTurn) R.drawable.x_icon else R.drawable.o_icon)
        block.tag = if (createdGame == myTurn) getString(R.string.x) else getString(R.string.o)

        // Store move made by player to board matrix
        // Set Matrix value, Player X = 1, Player Y = 2, Empty Block = 0
        board[r][c] = if (createdGame == myTurn) 1 else 2

        /*
         * evaluate game & save result
         * if result == -1, continue game
         * if result == 0, draw
         * if result == 1, player X won
         * if result == 2, player O won
         */
        val result = evaluate(true)
        if (result != -1) {
            gameOver(result)
        }
    }

    private fun evaluate(cut: Boolean): Int {
        // Check Rows
        for (i in 0..2) {
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
        return if (numberOfMovesPlayed == 9) 0 else -1
    }

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
                binding.heading.text = if (createdGame) getString(R.string.you_won)
                else getString(R.string.someone_won, gameRoom.creatorName)
                creatorVictoryCount++
            }
            2 -> {
                binding.heading.text = if (createdGame) getString(R.string.someone_won, gameRoom.joineeName)
                else getString(R.string.you_won)
                joineeVictoryCount++
            }
            0 -> binding.heading.setText(R.string.draw_game)
        }

        binding.scoreSection.visibility = View.VISIBLE
        binding.yourScore.text = "${if (createdGame) creatorVictoryCount else joineeVictoryCount}"
        binding.yourLabel.text = (if (createdGame) gameRoom.creatorName else gameRoom.joineeName)
                ?: ""
        binding.opponentScore.text = "${if (createdGame) joineeVictoryCount else creatorVictoryCount}"
        binding.opponentLabel.text = "${if (createdGame) gameRoom.joineeName else gameRoom.creatorName}"

        val yourScore = if (createdGame) creatorVictoryCount else joineeVictoryCount
        val opponentScore = if (createdGame) joineeVictoryCount else creatorVictoryCount

        when {
            yourScore > opponentScore -> {
                binding.yourLabel.setTypeface(null, Typeface.BOLD_ITALIC)
                binding.opponentLabel.setTypeface(null, Typeface.NORMAL)
            }
            yourScore < opponentScore -> {
                binding.yourLabel.setTypeface(null, Typeface.NORMAL)
                binding.opponentLabel.setTypeface(null, Typeface.BOLD_ITALIC)
            }
            else -> {
                binding.yourLabel.setTypeface(null, Typeface.NORMAL)
                binding.opponentLabel.setTypeface(null, Typeface.NORMAL)
            }
        }

        binding.reset.visibility = View.VISIBLE
    }

    private fun uploadMove(block: ImageView, r: Int, c: Int) {
        binding.progress.visibility = View.VISIBLE

        board[r][c] = if (createdGame) 1 else 2
        numberOfMovesPlayed++

        val updateData = mapOf<String, Any>("creatorChance" to !gameRoom.creatorChance,
                "movePlayed" to (r * 3 + c),
                "gameFinished" to (evaluate(false) != -1),
                "newGame" to false)

        databaseReference.updateChildren(updateData)
                .addOnSuccessListener {
                    board[r][c] = 0
                    numberOfMovesPlayed--
                    makeMove(block, r, c, true)
                    binding.progress.visibility = View.GONE
                }
                .addOnFailureListener {
                    board[r][c] = 0
                    numberOfMovesPlayed--
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this@GameFriend, "Network Error, please try again",
                            Toast.LENGTH_LONG).show()
                }
    }


    // handle UI functionality
    private fun changeHeading() {
        var heading = when {
            createdGame == gameRoom.creatorChance -> "Your"
            createdGame -> gameRoom.joineeName
            else -> gameRoom.creatorName
        }
        heading += " (${if (gameRoom.creatorChance) "X" else "O"}) chance"
        binding.heading.text = heading
    }

    private fun disableAllBlocks() {
        gameBlocks.forEach { it.isClickable = false }
    }

    private fun enableAllEmptyBlocks() {
        gameBlocks.forEach {
            if (it.tag == null) it.isClickable = true
        }
    }

    private fun makeCuts(strikeType: StrikeType, strikeIndex: Int) {
        val index = when (strikeType) {
            StrikeType.ROW -> strikeIndex + 3
            StrikeType.DIAGONAL -> strikeIndex + 6
            else -> strikeIndex
        }
        strikes[index].visibility = View.VISIBLE
    }


    // handle widget clicks
    fun onBlockClicked(clickedBlock: View) {
        //// if a block is clicked, find which block was clicked
        //// send that blocks view & coordinates to makeMove method
        var index = 0
        gameBlocks.forEach {
            if (it.id != clickedBlock.id)
                index++
        }

        val row = index / 3
        val col = index % 3
        uploadMove(clickedBlock as ImageView, row, col)
    }

    fun onResetClicked(view: View) {
        binding.progress.visibility = View.VISIBLE

        val resetData = mapOf<String, Any?>("gameFinished" to false,
                "creatorChance" to Random.nextBoolean(),
                "movePlayed" to null,
                "newGame" to true)

        databaseReference.updateChildren(resetData)
                .addOnSuccessListener {
                    binding.progress.visibility = View.GONE
                }
                .addOnFailureListener {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this@GameFriend, "Error Resetting, please try again",
                            Toast.LENGTH_LONG).show()
                }
    }

}
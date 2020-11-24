package `in`.kashewdevelopers.xo

import `in`.kashewdevelopers.xo.databinding.ActivityGameCreateJoinBinding
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class GameCreateJoin : AppCompatActivity() {

    private lateinit var binding: ActivityGameCreateJoinBinding

    private var gameRoomId = ""
    private var gameRoomIdLength = 10
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameCreateJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        initialize()
    }


    // initialization
    private fun initialize() {
        binding.name.requestFocus()
        binding.createSection.visibility = View.GONE
        binding.joinGameRoomId.visibility = View.GONE
        binding.cancelButton.visibility = View.GONE
        binding.startGameButton.visibility = View.GONE
    }


    // handle widget clicks
    fun onCreateGameClicked(view: View) {
        if (binding.name.text.toString().isEmpty()) {
            binding.name.error = "Enter Name"
            return
        }

        binding.name.isEnabled = false

        binding.createGameButton.visibility = View.GONE
        binding.joinGameButton.visibility = View.GONE

        binding.createSection.visibility = View.VISIBLE
        binding.cancelButton.visibility = View.VISIBLE
        binding.startGameButton.visibility = View.VISIBLE

        val charSet = "UB8jJNmT5ubqyzgCPvlISEo3FWGHL1246kpfnYZ0MAs9ViKrtw7RxheQdDcaXO"
        val random = Random()
        val temp = StringBuilder()
        for (i in 0 until gameRoomIdLength) {
            temp.append(charSet[random.nextInt(62)])
        }
        gameRoomId = temp.toString()
        binding.creatorGameRoomId.text = gameRoomId
    }

    fun onCopyClicked(view: View) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("gameRoomId",
                binding.creatorGameRoomId.text.toString())

        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, R.string.game_room_id_copied, Toast.LENGTH_LONG).show()
    }

    fun onShareClicked(view: View) {
        val shareBody = binding.creatorGameRoomId.text.toString()
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Send Game Room ID using"))
    }

    fun onJoinGameClicked(view: View) {
        if (binding.name.text.toString().isEmpty()) {
            binding.name.error = "Enter Name"
            return
        }

        binding.name.isEnabled = false

        binding.createGameButton.visibility = View.GONE
        binding.joinGameButton.visibility = View.GONE

        binding.cancelButton.visibility = View.VISIBLE
        binding.startGameButton.visibility = View.VISIBLE
        binding.joinGameRoomId.visibility = View.VISIBLE
        binding.joinGameRoomId.requestFocus()
    }

    fun onCancelClicked(view: View) {
        binding.name.isEnabled = true

        binding.createGameButton.visibility = View.VISIBLE
        binding.joinGameButton.visibility = View.VISIBLE

        binding.createSection.visibility = View.GONE
        binding.joinGameRoomId.visibility = View.GONE

        binding.cancelButton.visibility = View.GONE
        binding.startGameButton.visibility = View.GONE
    }

    fun onStartClicked(view: View) {
        if (binding.joinGameRoomId.visibility == View.VISIBLE) {
            if (binding.joinGameRoomId.text.isEmpty()) {
                binding.joinGameRoomId.error = "Enter Game Room ID"
                return
            } else if (binding.joinGameRoomId.text.length != gameRoomIdLength) {
                binding.joinGameRoomId.error = "Invalid Game Room ID"
                return
            }

            gameRoomId = binding.joinGameRoomId.text.toString()

            val i = Intent(this, GameFriend::class.java)
            with(i) {
                putExtra("gameRoomId", gameRoomId)
                putExtra("createGame", false)
                putExtra("name", binding.name.text.toString())
            }
            startActivity(i)
        } else {
            val i = Intent(this, GameFriend::class.java)
            with(i) {
                putExtra("gameRoomId", binding.creatorGameRoomId.text.toString())
                putExtra("createGame", true)
                putExtra("name", binding.name.text.toString())
            }
            startActivity(i)
        }
    }

}

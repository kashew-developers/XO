package `in`.kashewdevelopers.xo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var backPressed = false
    private lateinit var pressBackAgain: Toast

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pressBackAgain = Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT)
    }

    override fun onBackPressed() {
        if (backPressed) {
            pressBackAgain.cancel()
            super.onBackPressed()
            return
        }

        backPressed = true
        pressBackAgain.show()
        Handler().postDelayed({ backPressed = false }, 2000)
    }


    // handle widget clicks
    fun playWithAIClicked(view: View) {
        val i = Intent(this, GamePlayActivity::class.java)
        i.putExtra(getString(R.string.playWithAI), true)
        startActivity(i)
    }

    fun twoPlayerClicked(view: View) {
        val i = Intent(this, GamePlayActivity::class.java)
        i.putExtra(getString(R.string.twoPlayer), false)
        startActivity(i)
    }

    fun playOnlineClicked(view: View) = startActivity(Intent(this, AuthenticateUser::class.java))

    fun aboutClicked(view: View) = startActivity(Intent(this, AboutActivity::class.java))

}
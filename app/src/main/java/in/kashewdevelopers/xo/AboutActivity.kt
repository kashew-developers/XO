package `in`.kashewdevelopers.xo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    fun gitHubLink(view: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))))
    }

}

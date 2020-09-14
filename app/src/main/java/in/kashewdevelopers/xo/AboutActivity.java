package in.kashewdevelopers.xo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void gitHubLink(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/kashew-developers/XO")));
    }

}

package in.kashewdevelopers.xo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/paintfont.ttf");
        ((TextView) findViewById(R.id.appName)).setTypeface(typeface);
        ((TextView) findViewById(R.id.developer)).setTypeface(typeface);
        ((TextView) findViewById(R.id.openSourceLink)).setTypeface(typeface);

    }

    public void gitHubLink(View view){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.github.com/kashew-developers/XO")));
    }

}

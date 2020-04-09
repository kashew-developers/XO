package in.kashewdevelopers.xo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {


    private boolean backPressed = false;
    private Toast pressBackAgain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pressBackAgain = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/paintfont.ttf");
        ((TextView) findViewById(R.id.x)).setTypeface(typeface);
        ((TextView) findViewById(R.id.and)).setTypeface(typeface);
        ((TextView) findViewById(R.id.o)).setTypeface(typeface);

    }


    public void onSinglePlayerClicked(View view) {

        pressBackAgain.cancel();
        Intent i = new Intent(this, GamePlayActivity.class);
        i.putExtra("singlePlayer", true);
        startActivity(i);

    }


    public void onMultiPlayerClicked(View view) {

        pressBackAgain.cancel();
        Intent i = new Intent(this, GamePlayActivity.class);
        i.putExtra("singlePlayer", false);
        startActivity(i);

    }


    public void onPlayWithFriendsClicked(View view){

        pressBackAgain.cancel();
        Intent i = new Intent(this, GameCreateJoin.class);
        i.putExtra("withFriends", false);
        startActivity(i);

    }


    public void onAboutClicked(View view){

        startActivity(new Intent(this, AboutActivity.class));

    }

    @Override
    public void onBackPressed() {

        if (backPressed) {
            pressBackAgain.cancel();
            super.onBackPressed();
        }

        backPressed = true;
        pressBackAgain.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }
}
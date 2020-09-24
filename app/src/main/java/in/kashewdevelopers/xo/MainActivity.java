package in.kashewdevelopers.xo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    private boolean backPressed = false;
    private Toast pressBackAgain;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this);

        pressBackAgain = Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT);
    }

    @Override
    public void onBackPressed() {
        if (backPressed) {
            pressBackAgain.cancel();
            super.onBackPressed();
            return;
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


    // handle widget clicks
    public void playWithAIClicked(View view) {
        Intent i = new Intent(this, GamePlayActivity.class);
        i.putExtra(getString(R.string.playWithAI), true);
        startActivity(i);
    }

    public void twoPlayerClicked(View view) {
        Intent i = new Intent(this, GamePlayActivity.class);
        i.putExtra(getString(R.string.twoPlayer), false);
        startActivity(i);
    }

    public void playOnlineClicked(View view) {
        startActivity(new Intent(this, AuthenticateUser.class));
    }

    public void aboutClicked(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

}
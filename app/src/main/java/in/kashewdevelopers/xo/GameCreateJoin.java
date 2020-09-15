package in.kashewdevelopers.xo;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameCreateJoin extends AppCompatActivity {

    Button createGameButton, joinGameButton, cancelButton, startGameButton;
    LinearLayout createSection;
    EditText name, joinGameRoomId;
    TextView creatorGameRoomId;
    ImageView copyButton, shareButton;

    String gameRoomId;
    int gameRoomIdLength = 10;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_create_join);

        progressDialog = new ProgressDialog(this);
        initialize();
    }

    // initialization
    public void initialize() {
        name = findViewById(R.id.name);
        name.requestFocus();

        createGameButton = findViewById(R.id.createGameButton);

        createSection = findViewById(R.id.createSection);
        createSection.setVisibility(View.GONE);

        creatorGameRoomId = findViewById(R.id.creatorGameRoomId);
        copyButton = findViewById(R.id.copyIcon);
        shareButton = findViewById(R.id.shareIcon);

        joinGameButton = findViewById(R.id.joinGameButton);
        joinGameRoomId = findViewById(R.id.joinGameRoomId);
        joinGameRoomId.setVisibility(View.GONE);

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.GONE);
        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setVisibility(View.GONE);
    }


    // handle widget clicks
    public void onCreateGameClicked(View view) {
        if (name.getText().toString().isEmpty()) {
            name.setError("Enter Name");
            return;
        }

        name.setEnabled(false);

        createGameButton.setVisibility(View.GONE);
        joinGameButton.setVisibility(View.GONE);

        createSection.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.VISIBLE);

        String charSet = "UB8jJNmT5ubqyzgCPvlISEo3FWGHL1246kpfnYZ0MAs9ViKrtw7RxheQdDcaXO";
        Random random = new Random();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < gameRoomIdLength; i++) {
            temp.append(charSet.charAt(random.nextInt(62)));
        }
        gameRoomId = temp.toString();
        creatorGameRoomId.setText(gameRoomId);
    }

    public void onCopyClicked(View v) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("gameRoomId", creatorGameRoomId.getText().toString());
        if (clipboardManager == null)
            return;

        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(GameCreateJoin.this, R.string.game_room_id_copied, Toast.LENGTH_LONG).show();
    }

    public void onShareClicked(View v) {
        String shareBody = creatorGameRoomId.getText().toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Send Game Room ID using"));
    }

    public void onJoinGameClicked(View view) {
        if (name.getText().toString().isEmpty()) {
            name.setError("Enter Name");
            return;
        }

        name.setEnabled(false);

        createGameButton.setVisibility(View.GONE);
        joinGameButton.setVisibility(View.GONE);

        cancelButton.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.VISIBLE);
        joinGameRoomId.setVisibility(View.VISIBLE);
        joinGameRoomId.requestFocus();
    }

    public void onCancelClicked(View view) {
        name.setEnabled(true);

        createGameButton.setVisibility(View.VISIBLE);
        joinGameButton.setVisibility(View.VISIBLE);

        createSection.setVisibility(View.GONE);
        joinGameRoomId.setVisibility(View.GONE);

        cancelButton.setVisibility(View.GONE);
        startGameButton.setVisibility(View.GONE);
    }

    public void onStartClicked(View view) {
        if (joinGameRoomId.getVisibility() == View.VISIBLE) {
            if (joinGameRoomId.getText().toString().isEmpty()) {
                joinGameRoomId.setError("Enter Game Room ID");
                return;
            } else if (joinGameRoomId.getText().toString().length() != gameRoomIdLength) {
                joinGameRoomId.setError("Invalid Game Room ID");
                return;
            }

            gameRoomId = joinGameRoomId.getText().toString();

            Intent i = new Intent(this, GameFriend.class);
            i.putExtra("gameRoomId", gameRoomId);
            i.putExtra("createGame", false);
            i.putExtra("name", name.getText().toString());
            startActivity(i);
        } else {
            Intent i = new Intent(this, GameFriend.class);
            i.putExtra("gameRoomId", creatorGameRoomId.getText().toString());
            i.putExtra("createGame", true);
            i.putExtra("name", name.getText().toString());
            startActivity(i);
        }
    }

}

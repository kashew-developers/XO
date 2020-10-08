package in.kashewdevelopers.xo;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import in.kashewdevelopers.xo.databinding.ActivityGameCreateJoinBinding;

public class GameCreateJoin extends AppCompatActivity {

    ActivityGameCreateJoinBinding binding;

    String gameRoomId;
    int gameRoomIdLength = 10;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameCreateJoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        initialize();
    }


    // initialization
    public void initialize() {
        binding.name.requestFocus();
        binding.createSection.setVisibility(View.GONE);
        binding.joinGameRoomId.setVisibility(View.GONE);
        binding.cancelButton.setVisibility(View.GONE);
        binding.startGameButton.setVisibility(View.GONE);
    }


    // handle widget clicks
    public void onCreateGameClicked(View view) {
        if (binding.name.getText().toString().isEmpty()) {
            binding.name.setError("Enter Name");
            return;
        }

        binding.name.setEnabled(false);

        binding.createGameButton.setVisibility(View.GONE);
        binding.joinGameButton.setVisibility(View.GONE);

        binding.createSection.setVisibility(View.VISIBLE);
        binding.cancelButton.setVisibility(View.VISIBLE);
        binding.startGameButton.setVisibility(View.VISIBLE);

        String charSet = "UB8jJNmT5ubqyzgCPvlISEo3FWGHL1246kpfnYZ0MAs9ViKrtw7RxheQdDcaXO";
        Random random = new Random();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < gameRoomIdLength; i++) {
            temp.append(charSet.charAt(random.nextInt(62)));
        }
        gameRoomId = temp.toString();
        binding.creatorGameRoomId.setText(gameRoomId);
    }

    public void onCopyClicked(View v) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("gameRoomId", binding.creatorGameRoomId.getText().toString());
        if (clipboardManager == null)
            return;

        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(GameCreateJoin.this, R.string.game_room_id_copied, Toast.LENGTH_LONG).show();
    }

    public void onShareClicked(View v) {
        String shareBody = binding.creatorGameRoomId.getText().toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Send Game Room ID using"));
    }

    public void onJoinGameClicked(View view) {
        if (binding.name.getText().toString().isEmpty()) {
            binding.name.setError("Enter Name");
            return;
        }

        binding.name.setEnabled(false);

        binding.createGameButton.setVisibility(View.GONE);
        binding.joinGameButton.setVisibility(View.GONE);

        binding.cancelButton.setVisibility(View.VISIBLE);
        binding.startGameButton.setVisibility(View.VISIBLE);
        binding.joinGameRoomId.setVisibility(View.VISIBLE);
        binding.joinGameRoomId.requestFocus();
    }

    public void onCancelClicked(View view) {
        binding.name.setEnabled(true);

        binding.createGameButton.setVisibility(View.VISIBLE);
        binding.joinGameButton.setVisibility(View.VISIBLE);

        binding.createSection.setVisibility(View.GONE);
        binding.joinGameRoomId.setVisibility(View.GONE);

        binding.cancelButton.setVisibility(View.GONE);
        binding.startGameButton.setVisibility(View.GONE);
    }

    public void onStartClicked(View view) {
        if (binding.joinGameRoomId.getVisibility() == View.VISIBLE) {
            if (binding.joinGameRoomId.getText().toString().isEmpty()) {
                binding.joinGameRoomId.setError("Enter Game Room ID");
                return;
            } else if (binding.joinGameRoomId.getText().toString().length() != gameRoomIdLength) {
                binding.joinGameRoomId.setError("Invalid Game Room ID");
                return;
            }

            gameRoomId = binding.joinGameRoomId.getText().toString();

            Intent i = new Intent(this, GameFriend.class);
            i.putExtra("gameRoomId", gameRoomId);
            i.putExtra("createGame", false);
            i.putExtra("name", binding.name.getText().toString());
            startActivity(i);
        } else {
            Intent i = new Intent(this, GameFriend.class);
            i.putExtra("gameRoomId", binding.creatorGameRoomId.getText().toString());
            i.putExtra("createGame", true);
            i.putExtra("name", binding.name.getText().toString());
            startActivity(i);
        }
    }

}

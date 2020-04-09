package in.kashewdevelopers.xo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class GameCreateJoin extends Activity {

    Button createGameButton, joinGameButton, cancelButton, startGameButton;
    LinearLayout createSection;
    EditText name, joinGameRoomId;
    TextView creatorGameRoomId;
    ImageView copyButton, shareButton;

    String gameRoomId;
    int gameRoomIdLength = 10;
    ProgressDialog progressDialog;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_create_join);
        progressDialog = new ProgressDialog(this);


        // Initialize widgets
        name = findViewById(R.id.name);

        createGameButton = findViewById(R.id.createGameButton);
        createSection = findViewById(R.id.createSection);
        creatorGameRoomId = findViewById(R.id.creatorGameRoomId);
        copyButton = findViewById(R.id.copyIcon);
        shareButton = findViewById(R.id.shareIcon);

        joinGameButton = findViewById(R.id.joinGameButton);
        joinGameRoomId = findViewById(R.id.joinGameRoomId);

        cancelButton = findViewById(R.id.cancelButton);
        startGameButton = findViewById(R.id.startGameButton);


        // configure widgets
        createSection.setVisibility(View.GONE);
        copyButton.setClickable(false);
        shareButton.setClickable(false);
        joinGameRoomId.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        startGameButton.setVisibility(View.GONE);
        name.requestFocus();


        // widgets onClick
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("gameRoomId", creatorGameRoomId.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(GameCreateJoin.this, "Game Room Id Copied", Toast.LENGTH_LONG).show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = creatorGameRoomId.getText().toString();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Send Game Room ID using"));
            }
        });

    }

//    public void createGame(View view) {
//
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference().child("game_rooms");
//
////        ref.setValue("Hello");
//
//        GameRoom gameRoom = new GameRoom("Kashew", "kashewdevelopers@gmail.com");
//        ref.setValue(gameRoom);
//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                GameRoom gr = dataSnapshot.getValue(GameRoom.class);
//                Log.d("KashewDevelopers", "Value : " + gr.username + " > " + gr.email);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }


    public void onCreateGameClicked(View view) {

        if( name.getText().toString().isEmpty() ){
            name.setError("Enter Name");
            return;
        }

        name.setEnabled(false);

        progressDialog.setTitle("Creating Game Room");
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

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

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("game_rooms").child(gameRoomId);
        databaseReference.setValue(gameRoomId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        creatorGameRoomId.setText(gameRoomId);
                        copyButton.setClickable(true);
                        shareButton.setClickable(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        new AlertDialog.Builder(GameCreateJoin.this)
                                .setTitle("Error")
                                .setMessage("Could'nt Create Game, please try again")
                                .show();
                    }
                });

    }


    public void onJoinGameClicked(View view) {

        if( name.getText().toString().isEmpty() ){
            name.setError("Enter Name");
            return;
        }

        name.setEnabled(false);

        createGameButton.setVisibility(View.GONE);
        joinGameButton.setVisibility(View.GONE);

        cancelButton.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.VISIBLE);
        joinGameRoomId.setVisibility(View.VISIBLE);

    }


    public void onCancelClicked(View view){

        name.setEnabled(true);

        createGameButton.setVisibility(View.VISIBLE);
        joinGameButton.setVisibility(View.VISIBLE);

        createSection.setVisibility(View.GONE);
        joinGameRoomId.setVisibility(View.GONE);

        cancelButton.setVisibility(View.GONE);
        startGameButton.setVisibility(View.GONE);

    }


    public void onStartClicked(View view){

        if( joinGameRoomId.getVisibility() == View.VISIBLE ){

            if( joinGameRoomId.getText().toString().isEmpty() ){
                joinGameRoomId.setError("Enter Game Room ID");
                return;
            }
            else if( joinGameRoomId.getText().toString().length() != gameRoomIdLength ){
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

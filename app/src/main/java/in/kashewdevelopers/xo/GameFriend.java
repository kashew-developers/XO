package in.kashewdevelopers.xo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameFriend extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    GameRoom gameRoom;
    Random random;

    String gameRoomId, name;
    boolean createdGame;

    ProgressDialog progressDialog;

    // widgets
    private TextView[] ALL_BLOCKS;
    private TextView HEADING;
    private TextView SCORE;
    private Button NEW_GAME;
    private RelativeLayout PROGRESS;

    private int movesPlayed = 0, creatorVictoryCount = 0, joineeVictoryCount = 0;


    //// Grid Data - Stores all moves made by player
    private int[][] board = new int[3][3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);
        progressDialog = new ProgressDialog(this);
        random = new Random();

        // initializing widgets
        ALL_BLOCKS = new TextView[]{findViewById(R.id.r0c0), findViewById(R.id.r0c1), findViewById(R.id.r0c2),
                findViewById(R.id.r1c0), findViewById(R.id.r1c1), findViewById(R.id.r1c2),
                findViewById(R.id.r2c0), findViewById(R.id.r2c1), findViewById(R.id.r2c2)};
        HEADING = findViewById(R.id.heading);
        SCORE = findViewById(R.id.score);
        NEW_GAME = findViewById(R.id.resest);
        NEW_GAME.setText(R.string.new_game);
        NEW_GAME.setVisibility(View.GONE);
        PROGRESS = findViewById(R.id.progress);


        // set font to blocks
        setFontToAllBlocks();


        // get data from intent
        gameRoomId = getIntent().getStringExtra("gameRoomId");
        createdGame = getIntent().getBooleanExtra("createGame", false);
        name = getIntent().getStringExtra("name");


        progressDialog.setTitle("Starting Game");
        progressDialog.setMessage("please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();


        // initialize database variables
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("game_rooms").child(gameRoomId);


        // database change listener
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                gameRoom = dataSnapshot.getValue(GameRoom.class);

                Log.d("KashewDevelopers", "Data : " + createdGame + " > " + gameRoom);

                if (gameRoom == null) return;

                // wait until both players have joined the game room
                if ((createdGame && gameRoom.joineeName == null) ||
                        (!createdGame && gameRoom.creatorName == null)) {
                    progressDialog.setTitle("Starting Game");
                    progressDialog.setMessage("waiting for 2nd player to join");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    return;
                }

                progressDialog.dismiss();

                analyzeData();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // insert creator's / joinee's data
        Map<String, Object> initialData = new HashMap<>();
        if( createdGame ){
            initialData.put("creatorChance", random.nextBoolean());
            initialData.put("creatorName", name);
        }
        else {
            initialData.put("joineeName", name);
        }
        initialData.put("gameFinished", false);

        databaseReference.updateChildren(initialData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(GameFriend.this, "Error starting game, please try again", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                });


    }


    public void changeHeading() {

        //// Change heading according to the current players chance
        String heading = (createdGame == gameRoom.creatorChance) ? "Your" :
                (createdGame ? gameRoom.joineeName : gameRoom.creatorName);
        heading += " (" + (gameRoom.creatorChance ? "X" : "O") + ") chance";
        HEADING.setText(heading);

    }


    public void analyzeData() {

        if( gameRoom.gameFinished ){
            NEW_GAME.setVisibility(View.VISIBLE);
            disableAllBlocks();
            return;
        }

        NEW_GAME.setVisibility(View.GONE);

        changeHeading();

        // if it is my chance
        //    1. disable all blocks
        //    2. emulate other player's move (if any)
        //    3. enable empty blocks
        // else, disable all blocks (waiting for other player)
        if (createdGame == gameRoom.creatorChance) {
            disableAllBlocks();
            if (gameRoom.movePlayed != null) {
                int r = gameRoom.movePlayed / 3;
                int c = gameRoom.movePlayed % 3;
                makeMove(ALL_BLOCKS[gameRoom.movePlayed], r, c, false);
            }
            enableAllEmptyBlocks();
        } else {
            disableAllBlocks();
        }

    }


    public void disableAllBlocks() {

        //// Disable all blocks
        for (TextView block : ALL_BLOCKS) {
            block.setEnabled(false);
        }

    }


    public void enableAllEmptyBlocks() {

        for (TextView block : ALL_BLOCKS) {
            if (block.getText().toString().equals(getString(R.string.blank)))
                block.setEnabled(true);
        }

    }


    public void onBlockClicked(View block) {

        //// if a block is clicked, find which block was clicked
        //// send that blocks view & coordinates to makeMove method

        int row = 0, col = 0;
        switch (block.getId()) {
            case R.id.r0c0: row = col = 0; break;
            case R.id.r0c1: row = 0; col = 1; break;
            case R.id.r0c2: row = 0; col = 2; break;
            case R.id.r1c0: row = 1; col = 0; break;
            case R.id.r1c1: row = 1; col = 1; break;
            case R.id.r1c2: row = 1; col = 2; break;
            case R.id.r2c0: row = 2; col = 0; break;
            case R.id.r2c1: row = 2; col = 1; break;
            case R.id.r2c2: row = 2; col = 2; break;
        }

        uploadMove((TextView) block, row, col);

    }


    public void uploadMove(final TextView block, final int r, final int c) {

        PROGRESS.setVisibility(View.VISIBLE);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("creatorChance", !gameRoom.creatorChance);
        updateData.put("movePlayed", r * 3 + c);
        board[r][c] = createdGame ? 1 : 2;
        updateData.put("gameFinished", (evaluate(false) != -1));

        databaseReference.updateChildren(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        board[r][c] = 0;
                        makeMove(block, r, c, true);
                        PROGRESS.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        board[r][c] = 0;
                        PROGRESS.setVisibility(View.GONE);
                        Toast.makeText(GameFriend.this, "Network Error, please try again",
                                Toast.LENGTH_LONG).show();
                    }
                });


    }


    public void makeMove(TextView block, int r, int c, boolean myTurn) {

        //// Increment Moves
        movesPlayed++;

        block.setEnabled(false);

        //// Set block Text & Color
        if (createdGame == myTurn) {
            block.setText(R.string.x);
            block.setTextColor(Color.parseColor(getString(R.string.x_color)));
        } else {
            block.setText(R.string.o);
            block.setTextColor(Color.parseColor(getString(R.string.o_color)));
        }


        //// Store move made by player to board matrix
        //// Set Matrix value, Player X = 1, Player Y = 2, Empty Block = 0
        board[r][c] = (createdGame == myTurn) ? 1 : 2;


        //// evaluate game & save result
        //// if result == -1, continue game
        //// if result == 0, draw
        //// if result == 1, player X won
        //// if result == 2, player O won
        int result = evaluate(true);
        if (result != -1) {
            gameOver(result);
        }
    }


    public int evaluate(boolean cut) {

        //// Check Rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && equals(board[i][0], board[i][1], board[i][2])) {
                if (cut) makeCuts(3 * i, 3 * i + 1, 3 * i + 2, "row");
                return board[i][0];
            }
        }

        //// Check Columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != 0 && equals(board[0][i], board[1][i], board[2][i])) {
                if (cut) makeCuts(i, 3 + i, 6 + i, "column");
                return board[0][i];
            }
        }


        //// Check Diagonals
        if (board[0][0] != 0 && equals(board[0][0], board[1][1], board[2][2])) {
            if (cut) makeCuts(0, 4, 8, "diagonalPrimary");
            return board[0][0];
        } else if (board[0][2] != 0 && equals(board[0][2], board[1][1], board[2][0])) {
            if (cut) makeCuts(2, 4, 6, "diagonalSecondary");
            return board[0][2];
        }

        //// Draw Game
        if (movesPlayed == 9)
            return 0;

        return -1;

    }


    public boolean equals(int a, int b, int c) { return (a == b && b == c); }


    public void makeCuts(int pos_1, int pos_2, int pos_3, String cutType) {

        //// Pos1, Pos2, Pos3 positions on blocks in ALL_BLOCKS
        //// cutType = row | column | diagonalPrimary | diagonalSecondary

        switch (cutType) {
            case "row":
                ALL_BLOCKS[pos_1].setForeground(getDrawable(R.drawable.cut_horizontal));
                ALL_BLOCKS[pos_2].setForeground(getDrawable(R.drawable.cut_horizontal));
                ALL_BLOCKS[pos_3].setForeground(getDrawable(R.drawable.cut_horizontal));
                break;
            case "column":
                ALL_BLOCKS[pos_1].setForeground(getDrawable(R.drawable.cut_vertical));
                ALL_BLOCKS[pos_2].setForeground(getDrawable(R.drawable.cut_vertical));
                ALL_BLOCKS[pos_3].setForeground(getDrawable(R.drawable.cut_vertical));
                break;
            case "diagonalPrimary":
                ALL_BLOCKS[pos_1].setForeground(getDrawable(R.drawable.cut_diagonal_primary));
                ALL_BLOCKS[pos_2].setForeground(getDrawable(R.drawable.cut_diagonal_primary));
                ALL_BLOCKS[pos_3].setForeground(getDrawable(R.drawable.cut_diagonal_primary));
                break;
            case "diagonalSecondary":
                ALL_BLOCKS[pos_1].setForeground(getDrawable(R.drawable.cut_diagonal_secondary));
                ALL_BLOCKS[pos_2].setForeground(getDrawable(R.drawable.cut_diagonal_secondary));
                ALL_BLOCKS[pos_3].setForeground(getDrawable(R.drawable.cut_diagonal_secondary));
                break;
        }

    }


    public void gameOver(int action) {

        //// This method is called when the game is over
        //// Either a player wins, or all the blocks are filled

        //// Disable all blocks
        disableAllBlocks();


        //// if action == 1, player X won
        //// if action == 2, player O won
        //// else all blocks are filled, Draw game
        if (action == 1) {
            HEADING.setText(createdGame ? "You won the game" : gameRoom.creatorName + " won the game");
            creatorVictoryCount++;
        } else if (action == 2) {
            HEADING.setText(createdGame ? gameRoom.joineeName + " won the game" : "You won the game");
            joineeVictoryCount++;
        } else if (action == 0)
            HEADING.setText(getString(R.string.draw_game));


        String score = "Score\n" +
                (createdGame ? "You" : gameRoom.creatorName) + " : " + creatorVictoryCount + "\n" +
                (createdGame ? gameRoom.joineeName : "You") + " : " + joineeVictoryCount;

        SCORE.setText(score);

    }


    public void reset(View v) {

        PROGRESS.setVisibility(View.VISIBLE);

        Map<String, Object> resetData = new HashMap<>();
        resetData.put("gameFinished", false);
        resetData.put("creatorChance", random.nextBoolean());
        resetData.put("movePlayed", null);

        databaseReference.updateChildren(resetData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        movesPlayed = 0;
                        board = new int[3][3];
                        for (TextView block : ALL_BLOCKS) {
                            block.setText(R.string.blank);
                            block.setEnabled(true);
                            block.setForeground(null);
                        }
                        disableAllBlocks();
                        PROGRESS.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        PROGRESS.setVisibility(View.GONE);
                        Toast.makeText(GameFriend.this, "Error Resetting, please try again",
                                Toast.LENGTH_LONG).show();
                    }
                });

    }


    public void setFontToAllBlocks() {

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/paintfont.ttf");
        for (TextView block : ALL_BLOCKS) {
            block.setTypeface(typeface);
        }

    }


}

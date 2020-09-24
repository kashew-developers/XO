package in.kashewdevelopers.xo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
    ValueEventListener valueEventListener;
    GameRoom gameRoom;
    Random random;

    String gameRoomId, name;
    boolean createdGame;

    ProgressDialog progressDialog;

    // widgets
    TextView headingTV;
    ImageView[] gameBlocks;
    TextView[] strikes;
    ConstraintLayout scoreSection, grid;
    TextView yourScoreLabelTV, yourScoreTV;
    TextView opponentScoreLabelTV, opponentScoreTV;
    Button resetButton;
    RelativeLayout progressBar;
    AdView adView;

    private int numberOfMovesPlayed = 0, creatorVictoryCount = 0, joineeVictoryCount = 0;
    public int STRIKE_ROW = 0, STRIKE_COLUMN = 1, STRIKE_DIAGONAL = 2;

    // Grid Data - Stores all moves made by player
    private int[][] board = new int[3][3];

    private boolean backPressed = false;
    private Toast backPressedToast, opponentLeftToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        initialization();
        manageAds();

        progressDialog.setTitle("Starting Game");
        progressDialog.setMessage("please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        initializeDbElements();
    }

    @Override
    public void onBackPressed() {
        if (backPressed) {
            backPressedToast.cancel();
            super.onBackPressed();
            return;
        }

        backPressed = true;
        backPressedToast.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("leftGame", true);
        databaseReference.updateChildren(updateData);
    }


    // initialization
    @SuppressLint("ShowToast")
    public void initialization() {
        progressDialog = new ProgressDialog(this);
        random = new Random();

        backPressedToast = Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT);
        opponentLeftToast = Toast.makeText(this, R.string.opponent_player_left, Toast.LENGTH_LONG);
        opponentLeftToast.setGravity(Gravity.CENTER, 0, 0);

        initializeWidgets();

        // get data from intent
        gameRoomId = getIntent().getStringExtra("gameRoomId");
        createdGame = getIntent().getBooleanExtra("createGame", false);
        name = getIntent().getStringExtra("name");
    }

    public void initializeWidgets() {
        headingTV = findViewById(R.id.heading);

        grid = findViewById(R.id.grid);
        gameBlocks = new ImageView[]{findViewById(R.id.r0c0), findViewById(R.id.r0c1), findViewById(R.id.r0c2),
                findViewById(R.id.r1c0), findViewById(R.id.r1c1), findViewById(R.id.r1c2),
                findViewById(R.id.r2c0), findViewById(R.id.r2c1), findViewById(R.id.r2c2)};

        strikes = new TextView[]{findViewById(R.id.col_0_strike), findViewById(R.id.col_1_strike),
                findViewById(R.id.col_2_strike), findViewById(R.id.row_0_strike),
                findViewById(R.id.row_1_strike), findViewById(R.id.row_2_strike),
                findViewById(R.id.primary_diagonal_strike), findViewById(R.id.secondary_diagonal_strike)};

        scoreSection = findViewById(R.id.scoreSection);
        yourScoreLabelTV = findViewById(R.id.your_label);
        yourScoreTV = findViewById(R.id.your_score);
        opponentScoreLabelTV = findViewById(R.id.opponent_label);
        opponentScoreTV = findViewById(R.id.opponent_score);
        resetButton = findViewById(R.id.reset);
        progressBar = findViewById(R.id.progress);

        adView = findViewById(R.id.adView);

        configureWidgetVisibility();
    }

    public void configureWidgetVisibility() {
        for (TextView strike : strikes) {
            strike.setVisibility(View.GONE);
        }
        scoreSection.setVisibility(View.INVISIBLE);
    }

    public void initializeDbElements() {
        // initialize database variables
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("game_rooms").child(gameRoomId);

        // database change listener
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                gameRoom = dataSnapshot.getValue(GameRoom.class);

                if (gameRoom == null)
                    return;

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
        };
        databaseReference.addValueEventListener(valueEventListener);

        // insert creator's / joinee's data
        Map<String, Object> initialData = new HashMap<>();
        if (createdGame) {
            initialData.put("creatorChance", random.nextBoolean());
            initialData.put("creatorName", name);
        } else {
            initialData.put("joineeName", name);
        }
        initialData.put("gameFinished", false);
        initialData.put("newGame", true);

        databaseReference.updateChildren(initialData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(GameFriend.this, "Error starting game, please try again", Toast.LENGTH_SHORT).show();
                        backPressed = true;
                        onBackPressed();
                    }
                });
    }


    // functionality
    public boolean equals(int a, int b, int c) {
        return (a == b && b == c);
    }

    public void analyzeData() {
        if (gameRoom.leftGame != null && gameRoom.leftGame) {
            backPressed = true;
            onBackPressed();
            opponentLeftToast.show();
            return;
        }

        if (gameRoom.gameFinished) {
            disableAllBlocks();
            // game has ended, IF now it is my chance
            // this means the game ended due to the previous move played
            // by the opponent, show the opponent's move
            if (createdGame == gameRoom.creatorChance) {
                if (gameRoom.movePlayed != null) {
                    int r = gameRoom.movePlayed / 3;
                    int c = gameRoom.movePlayed % 3;
                    makeMove(gameBlocks[gameRoom.movePlayed], r, c, false);
                }
            }
            resetButton.setVisibility(View.VISIBLE);
            return;
        }

        // newGame = True, when any one player Resets the games
        if (gameRoom.newGame) {
            resetButton.setVisibility(View.INVISIBLE);
            numberOfMovesPlayed = 0;
            board = new int[3][3];
            for (ImageView block : gameBlocks) {
                block.setTag(null);
                block.setImageResource(0);
                block.setClickable(true);
            }

            for (TextView each : strikes) {
                each.setVisibility(View.GONE);
            }
        }

        changeHeading();

        // if it is my chance
        //    1. disable all blocks
        //    2. emulate opponent's previous move (if any)
        //    3. enable empty blocks
        // else, disable all blocks (waiting for other player)
        if (createdGame == gameRoom.creatorChance) {
            disableAllBlocks();
            if (gameRoom.movePlayed != null) {
                int r = gameRoom.movePlayed / 3;
                int c = gameRoom.movePlayed % 3;
                makeMove(gameBlocks[gameRoom.movePlayed], r, c, false);
            }
            enableAllEmptyBlocks();
        } else {
            disableAllBlocks();
        }
    }

    public void makeMove(ImageView block, int r, int c, boolean myTurn) {
        numberOfMovesPlayed++;

        block.setClickable(false);

        // Set X or O
        block.setImageResource(createdGame == myTurn ?
                R.drawable.x_icon :
                R.drawable.o_icon);
        block.setTag(createdGame == myTurn ?
                getString(R.string.x) :
                getString(R.string.o));

        // Store move made by player to board matrix
        // Set Matrix value, Player X = 1, Player Y = 2, Empty Block = 0
        board[r][c] = (createdGame == myTurn) ? 1 : 2;

        /*
         * evaluate game & save result
         * if result == -1, continue game
         * if result == 0, draw
         * if result == 1, player X won
         * if result == 2, player O won
         */
        int result = evaluate(true);
        if (result != -1) {
            gameOver(result);
        }
    }

    public int evaluate(boolean cut) {
        // Check Rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != 0 && equals(board[i][0], board[i][1], board[i][2])) {
                if (cut) makeCuts(STRIKE_ROW, i);
                return board[i][0];
            }
        }

        // Check Columns
        for (int i = 0; i < 3; i++) {
            if (board[0][i] != 0 && equals(board[0][i], board[1][i], board[2][i])) {
                if (cut) makeCuts(STRIKE_COLUMN, i);
                return board[0][i];
            }
        }

        // Check Diagonals
        if (board[0][0] != 0 && equals(board[0][0], board[1][1], board[2][2])) {
            if (cut) makeCuts(STRIKE_DIAGONAL, 0);
            return board[0][0];
        } else if (board[0][2] != 0 && equals(board[0][2], board[1][1], board[2][0])) {
            if (cut) makeCuts(STRIKE_DIAGONAL, 1);
            return board[0][2];
        }

        // Draw Game
        if (numberOfMovesPlayed == 9)
            return 0;

        return -1;
    }

    public void gameOver(int action) {
        // This method is called when the game is over
        // Either a player wins, or all the blocks are filled

        // Disable all blocks
        disableAllBlocks();

        // if action == 1, player X won
        // if action == 2, player O won
        // else all blocks are filled, Draw game
        if (action == 1) {
            headingTV.setText(createdGame ? getString(R.string.you_won) :
                    String.format(getString(R.string.someone_won), gameRoom.creatorName));
            creatorVictoryCount++;
        } else if (action == 2) {
            headingTV.setText(createdGame ?
                    String.format(getString(R.string.someone_won), gameRoom.joineeName) :
                    getString(R.string.you_won));
            joineeVictoryCount++;
        } else if (action == 0)
            headingTV.setText(R.string.draw_game);

        scoreSection.setVisibility(View.VISIBLE);
        yourScoreTV.setText(String.valueOf(createdGame ? creatorVictoryCount : joineeVictoryCount));
        yourScoreLabelTV.setText(createdGame ? gameRoom.creatorName : gameRoom.joineeName);
        opponentScoreTV.setText(String.valueOf(createdGame ? joineeVictoryCount : creatorVictoryCount));
        opponentScoreLabelTV.setText(createdGame ? gameRoom.joineeName : gameRoom.creatorName);

        int yourScore = (createdGame ? creatorVictoryCount : joineeVictoryCount);
        int opponentScore = (createdGame ? joineeVictoryCount : creatorVictoryCount);

        if (yourScore > opponentScore) {
            yourScoreLabelTV.setTypeface(null, Typeface.BOLD_ITALIC);
            opponentScoreLabelTV.setTypeface(null, Typeface.NORMAL);
        } else if (yourScore < opponentScore) {
            yourScoreLabelTV.setTypeface(null, Typeface.NORMAL);
            opponentScoreLabelTV.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            yourScoreLabelTV.setTypeface(null, Typeface.NORMAL);
            opponentScoreLabelTV.setTypeface(null, Typeface.NORMAL);
        }

        resetButton.setVisibility(View.VISIBLE);
    }

    public void uploadMove(final ImageView block, final int r, final int c) {
        progressBar.setVisibility(View.VISIBLE);

        board[r][c] = createdGame ? 1 : 2;
        numberOfMovesPlayed++;

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("creatorChance", !gameRoom.creatorChance);
        updateData.put("movePlayed", r * 3 + c);
        updateData.put("gameFinished", (evaluate(false) != -1));
        updateData.put("newGame", false);

        databaseReference.updateChildren(updateData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        board[r][c] = 0;
                        numberOfMovesPlayed--;
                        makeMove(block, r, c, true);
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        board[r][c] = 0;
                        numberOfMovesPlayed--;
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GameFriend.this, "Network Error, please try again",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    // handle UI functionality
    public void changeHeading() {
        String heading = (createdGame == gameRoom.creatorChance) ? "Your" :
                (createdGame ? gameRoom.joineeName : gameRoom.creatorName);
        heading += " (" + (gameRoom.creatorChance ? "X" : "O") + ") chance";
        headingTV.setText(heading);
    }

    public void disableAllBlocks() {
        for (ImageView block : gameBlocks) {
            block.setClickable(false);
        }
    }

    public void enableAllEmptyBlocks() {
        for (ImageView block : gameBlocks) {
            if (block.getTag() == null)
                block.setClickable(true);
        }
    }

    public void makeCuts(int strikeType, int strikeIndex) {
        if (strikeType == STRIKE_ROW) {
            strikeIndex = strikeIndex + 3;
        } else if (strikeType == STRIKE_DIAGONAL) {
            strikeIndex = strikeIndex + 6;
        }

        strikes[strikeIndex].setVisibility(View.VISIBLE);
    }


    // handle widget clicks
    public void onBlockClicked(View clickedBlock) {
        //// if a block is clicked, find which block was clicked
        //// send that blocks view & coordinates to makeMove method
        int index = 0;
        for (ImageView block : gameBlocks) {
            if (block.getId() == clickedBlock.getId())
                break;
            index++;
        }

        int row = index / 3, col = index % 3;
        uploadMove((ImageView) clickedBlock, row, col);
    }

    public void onResetClicked(View v) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> resetData = new HashMap<>();
        resetData.put("gameFinished", false);
        resetData.put("creatorChance", random.nextBoolean());
        resetData.put("movePlayed", null);
        resetData.put("newGame", true);

        databaseReference.updateChildren(resetData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GameFriend.this, "Error Resetting, please try again",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void manageAds() {
        adView.loadAd(new AdRequest.Builder().build());
    }

}
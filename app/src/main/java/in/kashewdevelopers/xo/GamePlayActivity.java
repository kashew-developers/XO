package in.kashewdevelopers.xo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GamePlayActivity extends Activity {

    //// For X player = true, for O player = false
    private boolean player = true;

    private boolean singlePlayer;
    private int x_won = 0, o_won = 0;

    //// Moves played
    private int moves = 0;

    //// Grid Data - Stores all moves made by player
    private int[][] gridData = new int[3][3];


    private boolean backPressed = false;
    private Toast pressBackAgain;


    //// WWidget Variables
    private TextView[] ALL_BLOCKS;
    private TextView HEADING;
    private TextView SCORE;
    private Button RESET;


    public void changeHeading() {

        //// Change heading according to the current players chance
        //// if player (true), its X's chance, else its O's chance
        if (player) {
            if (singlePlayer)
                HEADING.setText(R.string.your_chance);
            else
                HEADING.setText(R.string.x_chance);
        } else {
            if (singlePlayer)
                HEADING.setText(R.string.computer_chance);
            else
                HEADING.setText(R.string.o_chance);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);


        ALL_BLOCKS = new TextView[]{findViewById(R.id.r0c0), findViewById(R.id.r0c1), findViewById(R.id.r0c2),
                findViewById(R.id.r1c0), findViewById(R.id.r1c1), findViewById(R.id.r1c2),
                findViewById(R.id.r2c0), findViewById(R.id.r2c1), findViewById(R.id.r2c2)};
        HEADING = findViewById(R.id.heading);
        SCORE = findViewById(R.id.score);
        RESET = findViewById(R.id.resest);
        pressBackAgain = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);


        //// Set FontType of all Blocks to paintfont
        setFontToAllBlocks();


        //// Get Game Mode
        singlePlayer = getIntent().getBooleanExtra("singlePlayer", false);


        changeHeading();


        if (singlePlayer) {

            //// if singlePlayer = true, generate a random number (0 or 1)
            //// if random number == 0, computer gets 1st chance
            if ((int) (Math.random() * 2) == 0) {
                player = false;
                changeHeading();
                computerChance();
            }
        }

    }


    public void onBlockClicked(View block) {

        //// if a block is clicked, find which block was clicked
        //// send that blocks view & coordinates to makeMove method
        for (int i = 0; i < ALL_BLOCKS.length; ++i) {
            if (ALL_BLOCKS[i].equals(block)) {
                makeMove((TextView) block, i / 3, i % 3);
            }
        }

    }


    public void makeMove(TextView block, int r, int c) {

        //// Increment Moves
        moves++;

        //// Disable Block
        block.setEnabled(false);


        //// Set block Text & Color
        //// if player (true), X made a move
        //// else O made a move
        if (player) {
            block.setText(R.string.x);
            block.setTextColor(Color.parseColor(getString(R.string.x_color)));
        } else {
            block.setText(R.string.o);
            block.setTextColor(Color.parseColor(getString(R.string.o_color)));
        }


        //// Store move made by player to gridData
        //// Set Matrix value, Player X = 1, Player Y = 2
        gridData[r][c] = player ? 1 : 2;


        //// Change Player
        player = !player;


        //// Change Heading
        changeHeading();


        //// check gameState, if game is not over
        //// check game mode, if singlePlayer = true
        //// check current player, if player = false
        //// its computer's chance
        if (!gameState()) {
            if (singlePlayer && !player) {
                computerChance();
            }
        }

    }


    public boolean gameState() {

        //// Check Rows
        for (int i = 0; i < 3; i++) {
            if (gridData[i][0] != 0 && equals(gridData[i][0], gridData[i][1], gridData[i][2])) {
                makeCuts(3 * i + 0, 3 * i + 1, 3 * i + 2, "row");
                return gameOver(gridData[i][0]);
            }
        }

        //// Check Columns
        for (int i = 0; i < 3; i++) {
            if (gridData[0][i] != 0 && equals(gridData[0][i], gridData[1][i], gridData[2][i])) {
                makeCuts(3 * 0 + i, 3 * 1 + i, 3 * 2 + i, "column");
                return gameOver(gridData[0][i]);
            }

        }


        //// Check Diagonals
        if (gridData[0][0] != 0 && equals(gridData[0][0], gridData[1][1], gridData[2][2])) {
            makeCuts(3 * 0 + 0, 3 * 1 + 1, 3 * 2 + 2, "diagonalPrimary");
            return gameOver(gridData[0][0]);
        } else if (gridData[0][2] != 0 && equals(gridData[0][2], gridData[1][1], gridData[2][0])) {
            makeCuts(3 * 0 + 2, 3 * 1 + 1, 3 * 2 + 0, "diagonalSecondary");
            return gameOver(gridData[0][2]);
        } else if (moves == 9)        // Tie Game
            return gameOver(3);

        return false;

    }


    public boolean gameOver(int action) {

        //// This method is called when the game is over
        //// Either a player wins, or all the blocks are filled


        //// Disable all blocks
        disableAllBlocks();


        //// if action == 1, player X won
        //// if action == 2, player O won
        //// else all blocks are filled, Tie game
        if (action == 1) {
            if (singlePlayer)
                HEADING.setText("You won the game");
            else
                HEADING.setText("Player \"X\" won the game");
            x_won++;
        } else if (action == 2) {
            if (singlePlayer)
                HEADING.setText("Computer won the game");
            else
                HEADING.setText("Player \"O\" won the game");
            o_won++;
        } else if (action == 3)
            HEADING.setText("Tie Game");

        if (singlePlayer)
            SCORE.setText("Score\nYou : " + x_won + "\nComputer : " + o_won);
        else
            SCORE.setText("Score\nPlayer X : " + x_won + "\nPlayer O : " + o_won);

        return true;
    }


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


    public boolean equals(int a, int b, int c) {
        return (a == b && b == c);
    }


    public void reset(View v) {

        //// Set initial values
        player = true;
        moves = 0;
        gridData = new int[3][3];


        //// Show Heading
        changeHeading();


        //// Enable all blocks & Set Block text to blank
        for (TextView block : ALL_BLOCKS) {
            block.setText(R.string.blank);
            block.setEnabled(true);
            block.setForeground(null);
        }

        if (singlePlayer) {

            //// if singlePlayer = true, generate a random number (0 or 1)
            //// if random number == 0, computer gets 1st chance
            if ((int) (Math.random() * 2) == 0) {
                player = false;
                changeHeading();
                computerChance();
            }
        }

    }


    public void setFontToAllBlocks() {

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/paintfont.ttf");
        for (TextView block : ALL_BLOCKS) {
            block.setTypeface(typeface);
        }

    }


    public void computerChance() {


        disableAllBlocks();
        RESET.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int temp, row, column;

                do {
                    temp = (int) (Math.random() * 9);
                    row = temp / 3;
                    column = temp % 3;

                } while (gridData[row][column] != 0);

                makeMove(ALL_BLOCKS[3 * row + column], row, column);
                enableAllEmptyBlocks();
                RESET.setEnabled(true);
            }
        }, 1000);

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
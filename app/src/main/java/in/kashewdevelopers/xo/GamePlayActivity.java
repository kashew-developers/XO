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

import java.util.ArrayList;
import java.util.Arrays;

public class GamePlayActivity extends Activity {

    //// For X player = true, for O player = false
    private boolean playerXChance = true;

    private boolean singlePlayerMode;
    private int xVictoryCount = 0, oVictoryCount = 0;

    //// Moves played
    private int movesPlayed = 0;

    //// Grid Data - Stores all moves made by player
    private int[][] board = new int[3][3];


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
        if (playerXChance) {
            HEADING.setText(singlePlayerMode ? R.string.your_chance : R.string.x_chance);
        } else {
            HEADING.setText(singlePlayerMode ? R.string.computer_chance : R.string.o_chance);
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
        singlePlayerMode = getIntent().getBooleanExtra("singlePlayer", false);


        changeHeading();


        if (singlePlayerMode) {

            //// if singlePlayerMode = true, generate a random number (0 or 1)
            //// if random number == 0, computer gets 1st chance
            if ((int) (Math.random() * 2) == 0) {
                playerXChance = false;
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
        movesPlayed++;

        //// Disable Block
        block.setEnabled(false);


        //// Set block Text & Color
        //// if playerXChance (true), X made a move
        //// else O made a move
        if (playerXChance) {
            block.setText(R.string.x);
            block.setTextColor(Color.parseColor(getString(R.string.x_color)));
        } else {
            block.setText(R.string.o);
            block.setTextColor(Color.parseColor(getString(R.string.o_color)));
        }


        //// Store move made by player to board matrix
        //// Set Matrix value, Player X = 1, Player Y = 2, Empty Block = 0
        board[r][c] = playerXChance ? 1 : 2;


        //// Change Player
        playerXChance = !playerXChance;


        //// Change Heading
        changeHeading();


        //// evaluate game & save result
        //// if result == -1, continue game
        //// if result == 0, draw
        //// if result == 1, player X won
        //// if result == 2, player Y won
        //// check game mode, if singlePlayerMode = true
        //// check current player, if playerXChance = false
        //// its computer's chance
        int result = evaluate(board, true);
        if (result == -1) {
            if (singlePlayerMode && !playerXChance) {
                computerChance();
            }
        } else {
            gameOver(result);
        }
    }


    public int evaluate(int[][] board, boolean cut) {

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


    public void gameOver(int action) {

        //// This method is called when the game is over
        //// Either a player wins, or all the blocks are filled

        //// Disable all blocks
        disableAllBlocks();


        //// if action == 1, player X won
        //// if action == 2, player O won
        //// else all blocks are filled, Draw game
        if (action == 1) {
            HEADING.setText(singlePlayerMode ? "You won the game" : "Player \"X\" won the game");
            xVictoryCount++;
        } else if (action == 2) {
            HEADING.setText(singlePlayerMode ? "Computer won the game" : "Player \"O\" won the game");
            oVictoryCount++;
        } else if (action == 0)
            HEADING.setText("Draw Game");

        if (singlePlayerMode)
            SCORE.setText("Score\nYou : " + xVictoryCount + "\nComputer : " + oVictoryCount);
        else
            SCORE.setText("Score\nPlayer X : " + xVictoryCount + "\nPlayer O : " + oVictoryCount);

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
        playerXChance = true;
        movesPlayed = 0;
        board = new int[3][3];


        //// Show Heading
        changeHeading();


        //// Enable all blocks & Set Block text to blank
        for (TextView block : ALL_BLOCKS) {
            block.setText(R.string.blank);
            block.setEnabled(true);
            block.setForeground(null);
        }

        if (singlePlayerMode) {

            //// if singlePlayer = true, generate a random number (0 or 1)
            //// if random number == 0, computer gets 1st chance
            if ((int) (Math.random() * 2) == 0) {
                playerXChance = false;
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


    public ArrayList<Integer> minimax(int board[][], int depth, boolean curPlayer) {

        int result = evaluate(board, false);

        if (result != -1) {
            int ret;
            if (result == 0) ret = 0;
            else ret = ((result == 1) == playerXChance) ? 1 : -1;
            return new ArrayList<>(Arrays.asList(ret, depth - 1, -1));
        }

        int[][] tempBoard = new int[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(board[i], 0, tempBoard[i], 0, 3);
        }

        ArrayList<Integer> answer = new ArrayList<>();
        answer.add(((depth & 1) == 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        answer.add(-1);
        answer.add(-1);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tempBoard[i][j] == 0) {

                    tempBoard[i][j] = curPlayer ? 1 : 2;
                    movesPlayed++;

                    ArrayList<Integer> temp = minimax(tempBoard, depth + 1, !curPlayer);
                    if (temp.get(2) == -1) temp.set(2, 3 * i + j);

                    if ((depth & 1) == 0) {
                        if (temp.get(0) < answer.get(0)) answer = temp;
                        else if (temp.get(0).equals(answer.get(0)) && temp.get(1) < answer.get(1))
                            answer = temp;
                    } else {
                        if (temp.get(0) > answer.get(0)) answer = temp;
                        else if (temp.get(0).equals(answer.get(0)) && temp.get(1) < answer.get(1))
                            answer = temp;
                    }

                    tempBoard[i][j] = 0;
                    movesPlayed--;

                }
            }
        }

        return answer;

    }


    public void computerChance() {

        disableAllBlocks();
        RESET.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                int row, column;

                if (movesPlayed < 1)
                    row = column = 1;
                else {
                    ArrayList<Integer> ans = minimax(board, 0, playerXChance);
                    row = ans.get(2) / 3;
                    column = ans.get(2) % 3;
                }

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
package in.kashewdevelopers.xo;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;

import in.kashewdevelopers.xo.databinding.ActivityGamePlayBinding;

public class GamePlayActivity extends AppCompatActivity {

    // For X, player = true, for O player = false
    private boolean playerXChance = true;

    private boolean playWithAI;
    private int xVictoryCount = 0, oVictoryCount = 0;

    // Moves played
    private int numberOfMovesPlayed = 0;

    // Grid Data - Stores all moves made by player
    private int[][] board = new int[3][3];


    private boolean backPressed = false;
    private Toast backPressedToast;

    public int STRIKE_ROW = 0, STRIKE_COLUMN = 1, STRIKE_DIAGONAL = 2;

    ActivityGamePlayBinding binding;

    // widgets
    ImageView[] gameBlocks;
    TextView[] strikes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGamePlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialization();
        manageAds();

        // Get Game Mode
        playWithAI = getIntent().getBooleanExtra(getString(R.string.playWithAI), false);

        // randomly generate a number from 0 to 1
        // 1st player is always player X
        // AI or Opponent is player O
        playerXChance = ((int) (Math.random() * 2) == 0);

        changeHeading();

        if (playWithAI && (!playerXChance)) {
            aiChance();
        }
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


    // initialization
    @SuppressLint("ShowToast")
    public void initialization() {
        initializeWidgets();

        backPressedToast = Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT);

        binding.grid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.grid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setGridSize();
            }
        });
    }

    public void initializeWidgets() {
        gameBlocks = new ImageView[]{binding.r0c0, binding.r0c1, binding.r0c2, binding.r1c0,
                binding.r1c1, binding.r1c2, binding.r2c0, binding.r2c1, binding.r2c2};

        strikes = new TextView[]{binding.col0Strike, binding.col1Strike, binding.col2Strike,
                binding.row0Strike, binding.row1Strike, binding.row2Strike,
                binding.primaryDiagonalStrike, binding.secondaryDiagonalStrike};

        configureWidgetVisibility();
    }

    public void configureWidgetVisibility() {
        for (TextView strike : strikes) {
            strike.setVisibility(View.GONE);
        }
        binding.scoreSection.setVisibility(View.INVISIBLE);
    }


    // functionality
    public void makeMove(ImageView block, int r, int c) {
        numberOfMovesPlayed++;

        block.setClickable(false);

        // Set X or O
        block.setImageResource(playerXChance ? R.drawable.x_icon : R.drawable.o_icon);
        block.setTag(playerXChance ? getString(R.string.x) : getString(R.string.o));

        // Store move made by player to board matrix
        // Set Matrix value, Player X = 1, Player Y = 2, Empty Block = 0
        board[r][c] = playerXChance ? 1 : 2;

        // Change Player
        playerXChance = !playerXChance;

        // Change Heading
        changeHeading();

        /*
         * evaluate game & save result
         * if result == -1, continue game
         * if result == 0, draw
         * if result == 1, player X won
         * if result == 2, player O won
         *
         * check game mode, if playWithAI = true
         * check current player, if playerXChance = false
         * its AI's chance
         */
        int result = evaluate(true);
        if (result == -1) {
            if (playWithAI && (!playerXChance)) {
                aiChance();
            }
        } else {
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

    public boolean equals(int a, int b, int c) {
        return (a == b && b == c);
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
            binding.heading.setText(playWithAI ? R.string.you_won : R.string.player_x_won);
            xVictoryCount++;
        } else if (action == 2) {
            binding.heading.setText(playWithAI ? R.string.ai_won : R.string.player_o_won);
            oVictoryCount++;
        } else if (action == 0)
            binding.heading.setText(R.string.draw_game);

        binding.scoreSection.setVisibility(View.VISIBLE);
        binding.yourLabel.setText(String.valueOf(xVictoryCount));
        binding.yourScore.setText(playWithAI ? R.string.you : R.string.player_x);
        binding.opponentLabel.setText(String.valueOf(oVictoryCount));
        binding.opponentScore.setText(playWithAI ? R.string.ai : R.string.player_o);

        if (xVictoryCount > oVictoryCount) {
            binding.yourLabel.setTypeface(null, Typeface.BOLD_ITALIC);
            binding.opponentLabel.setTypeface(null, Typeface.NORMAL);
        } else if (xVictoryCount < oVictoryCount) {
            binding.yourLabel.setTypeface(null, Typeface.NORMAL);
            binding.opponentLabel.setTypeface(null, Typeface.BOLD_ITALIC);
        } else {
            binding.yourLabel.setTypeface(null, Typeface.NORMAL);
            binding.opponentLabel.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void aiChance() {
        disableAllBlocks();
        binding.reset.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                int row, column;

                if (numberOfMovesPlayed < 1) {
                    row = (int) (Math.random() * 3);
                    column = (int) (Math.random() * 3);
                } else {
                    int ans = minimax(10, playerXChance);
                    row = ans / 3;
                    column = ans % 3;
                }

                makeMove(gameBlocks[3 * row + column], row, column);
                enableAllEmptyBlocks();
                binding.reset.setEnabled(true);
            }
        }, 400);
    }

    public int minimax(int depth, boolean curPlayer) {
        Integer finalDepth = null, finalPos = null;
        int tempDepth;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                if (board[i][j] == 0) {
                    board[i][j] = curPlayer ? 1 : 2;
                    numberOfMovesPlayed++;

                    int result = evaluate(false);
                    if (result != -1) {
                        if (result == 0) tempDepth = 0;
                        else tempDepth = ((curPlayer == playerXChance) ? 1 : -1) * depth;
                    } else {
                        tempDepth = minimax(depth - 1, !curPlayer);
                    }

                    board[i][j] = 0;
                    numberOfMovesPlayed--;

                    if (finalDepth == null) finalDepth = tempDepth;

                    if (curPlayer == playerXChance) {
                        if (finalDepth <= tempDepth) {
                            finalDepth = tempDepth;
                            finalPos = i * 3 + j;
                        }
                        if (finalDepth == depth) break;
                    } else {
                        if (finalDepth >= tempDepth) {
                            finalDepth = tempDepth;
                            finalPos = i * 3 + j;
                        }
                        if (finalDepth == -depth) break;
                    }
                }
            }
        }
        if (depth == 10) return finalPos;
        else return finalDepth;
    }


    // handle UI functionality
    public void changeHeading() {
        if (playerXChance) {
            binding.heading.setText(playWithAI ? R.string.your_chance : R.string.x_chance);
        } else {
            binding.heading.setText(playWithAI ? R.string.ai_chance : R.string.o_chance);
        }
    }

    public void setGridSize() {
        int height = binding.grid.getMeasuredHeight();
        int width = binding.grid.getMeasuredWidth();
        int min = Math.min(width, height);

        ViewGroup.LayoutParams layoutParams = binding.grid.getLayoutParams();
        layoutParams.height = min;
        layoutParams.width = min;
        binding.grid.setLayoutParams(layoutParams);
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
        // if a block is clicked, find which block was clicked
        // send that blocks view & coordinates to makeMove method
        int index = 0;
        for (ImageView block : gameBlocks) {
            if (block.getId() == clickedBlock.getId())
                break;
            index++;
        }

        int row = index / 3, col = index % 3;
        makeMove((ImageView) clickedBlock, row, col);
    }

    public void onResetClicked(View v) {
        // Set initial values
        numberOfMovesPlayed = 0;
        board = new int[3][3];

        playerXChance = ((int) (Math.random() * 2) == 0);

        // Show Heading
        changeHeading();

        // Enable all blocks & Set Block text to blank
        for (ImageView block : gameBlocks) {
            block.setClickable(true);
            block.setImageResource(0);
            block.setTag(null);
        }

        // remove all strikes
        for (TextView each : strikes) {
            each.setVisibility(View.GONE);
        }

        if (playWithAI && (!playerXChance)) {
            aiChance();
        }
    }

    public void manageAds() {
        binding.adView.loadAd(new AdRequest.Builder().build());
    }

}
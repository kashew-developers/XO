package in.kashewdevelopers.xo;

import androidx.annotation.NonNull;

public class GameRoom {

    public Boolean creatorChance, gameFinished, newGame;
    public String creatorName, joineeName;
    public Integer movePlayed;

    GameRoom() {
    }

    GameRoom(Boolean creatorChance, String creatorName, String joineeName, Integer movePlayed) {
        this.creatorChance = creatorChance;
        this.creatorName = creatorName;
        this.joineeName = joineeName;
        this.movePlayed = movePlayed;
    }

    @NonNull
    @Override
    public String toString() {
        return "creatorChance : " + creatorChance +
                ", creatorName : " + creatorName +
                ", joineeName : " + joineeName +
                ", newGame : " + newGame +
                ", gameFinished : " + gameFinished +
                ", movePlayed : " + movePlayed;
    }
}

package `in`.kashewdevelopers.xo

data class GameRoom(var creatorChance: Boolean,
                    var creatorName: String,
                    var joineeName: String,
                    var movePlayed: Int) {

    var leftGame: Boolean = false
    var newGame: Boolean = false
    var gameFinished: Boolean = false

    override fun toString(): String {
        return "creatorChance : $creatorChance, " +
                "creatorName : $creatorName, " +
                "joineeName : $joineeName, " +
                "newGame : $newGame, " +
                "gameFinished : $gameFinished, " +
                "leftGame : $leftGame, " +
                "movePlayed : $movePlayed"
    }

}
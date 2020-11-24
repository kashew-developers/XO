package `in`.kashewdevelopers.xo

class GameRoom() {

    var creatorChance: Boolean = false
    var creatorName: String? = null
    var joineeName: String? = null
    var movePlayed: Int? = null
    var leftGame: Boolean? = null
    var newGame: Boolean = false
    var gameFinished: Boolean = false

    constructor(creatorChance: Boolean,
                creatorName: String?,
                joineeName: String?,
                movePlayed: Int?) : this() {

        this.creatorChance = creatorChance
        this.creatorName = creatorName
        this.joineeName = joineeName
        this.movePlayed = movePlayed
    }


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
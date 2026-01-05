package UPCE.BMTA.Game2048.model

data class GameState(
    val tiles: List<Tile>,
    val score: Int,
    val bestScore: Int,
    val gameOver: Boolean,
    val hasWon: Boolean,
    val showWinOverlay: Boolean = true
)
package UPCE.BMTA.Game2048.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import UPCE.BMTA.Game2048.model.GameState
import UPCE.BMTA.Game2048.model.Tile
import UPCE.BMTA.Game2048.repository.GameRepository
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    private val gridSize = 4
    private val totalCells = gridSize * gridSize

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private var nextTileId = 0
    private var bestScore = 0

    init {
        bestScore = repository.loadBestScore()
        loadGame()
    }

    fun newGame() {
        nextTileId = 0
        val initialTiles = mutableListOf<Tile>()

        repeat(2) {
            addRandomTile(initialTiles)
        }

        _gameState.value = GameState(
            tiles = initialTiles,
            score = 0,
            bestScore = bestScore,
            gameOver = false,
            hasWon = false
        )

        saveGame()
    }

    private fun addRandomTile(tiles: MutableList<Tile>) {
        val emptyPositions = (0 until totalCells).filter { position ->
            tiles.none { it.position == position }
        }

        if (emptyPositions.isEmpty()) return

        val position = emptyPositions[Random.nextInt(emptyPositions.size)]
        val value = if (Random.nextFloat() < 0.9f) 2 else 4

        tiles.add(Tile(nextTileId++, value, position))
    }

    fun move(direction: Direction) {
        val currentState = _gameState.value ?: return
        if (currentState.gameOver) return

        val tiles = currentState.tiles.toMutableList()
        var score = currentState.score
        var moved = false

        val groups = when (direction) {
            Direction.LEFT, Direction.RIGHT -> {
                (0 until gridSize).map { row ->
                    tiles.filter { it.position / gridSize == row }
                        .sortedBy { it.position % gridSize }
                }
            }
            Direction.UP, Direction.DOWN -> {
                (0 until gridSize).map { col ->
                    tiles.filter { it.position % gridSize == col }
                        .sortedBy { it.position / gridSize }
                }
            }
        }

        val newTiles = mutableListOf<Tile>()

        groups.forEachIndexed { index, group ->
            val sortedGroup = if (direction == Direction.RIGHT || direction == Direction.DOWN) {
                group.reversed()
            } else {
                group
            }

            val merged = mutableListOf<Tile>()
            var i = 0

            while (i < sortedGroup.size) {
                val current = sortedGroup[i]

                if (i + 1 < sortedGroup.size && sortedGroup[i + 1].value == current.value) {
                    val newValue = current.value * 2
                    score += newValue

                    val newPosition = calculateNewPosition(index, merged.size, direction)
                    merged.add(Tile(nextTileId++, newValue, newPosition))
                    moved = true
                    i += 2
                } else {
                    val newPosition = calculateNewPosition(index, merged.size, direction)
                    if (newPosition != current.position) moved = true
                    merged.add(current.copy(position = newPosition))
                    i++
                }
            }

            newTiles.addAll(merged)
        }

        if (moved) {
            addRandomTile(newTiles)

            if (score > bestScore) {
                bestScore = score
            }

            val hasWon = newTiles.any { it.value >= 2048 } && !currentState.hasWon
            val gameOver = isGameOver(newTiles)

            _gameState.value = GameState(
                tiles = newTiles,
                score = score,
                bestScore = bestScore,
                gameOver = gameOver,
                hasWon = hasWon || currentState.hasWon
            )

            saveGame()
        }
    }

    private fun calculateNewPosition(groupIndex: Int, positionInGroup: Int, direction: Direction): Int {
        return when (direction) {
            Direction.LEFT -> groupIndex * gridSize + positionInGroup
            Direction.RIGHT -> groupIndex * gridSize + (gridSize - 1 - positionInGroup)
            Direction.UP -> positionInGroup * gridSize + groupIndex
            Direction.DOWN -> (gridSize - 1 - positionInGroup) * gridSize + groupIndex
        }
    }

    private fun isGameOver(tiles: List<Tile>): Boolean {
        if (tiles.size < totalCells) return false

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val position = row * gridSize + col
                val tile = tiles.find { it.position == position } ?: continue

                val neighbors = listOf(
                    position - gridSize,
                    position + gridSize,
                    if (col > 0) position - 1 else -1,
                    if (col < gridSize - 1) position + 1 else -1
                )

                for (neighborPos in neighbors) {
                    if (neighborPos >= 0 && neighborPos < totalCells) {
                        val neighbor = tiles.find { it.position == neighborPos }
                        if (neighbor?.value == tile.value) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    fun saveGame() {
        _gameState.value?.let { repository.saveGame(it) }
    }

    private fun loadGame() {
        val savedState = repository.loadGame()
        if (savedState != null) {
            nextTileId = (savedState.tiles.maxOfOrNull { it.id } ?: -1) + 1
            bestScore = savedState.bestScore.coerceAtLeast(bestScore)
            _gameState.value = savedState
        } else {
            newGame()
        }
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}
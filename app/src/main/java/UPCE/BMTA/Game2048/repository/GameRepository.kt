package UPCE.BMTA.Game2048.repository

import android.content.Context
import UPCE.BMTA.Game2048.model.GameState
import UPCE.BMTA.Game2048.model.Tile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GameRepository(private val context: Context) {

    private val fileName = "game_state.json"
    private val bestScoreFileName = "best_score.json"

    fun saveGame(gameState: GameState) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("score", gameState.score)
            jsonObject.put("bestScore", gameState.bestScore)
            jsonObject.put("gameOver", gameState.gameOver)
            jsonObject.put("hasWon", gameState.hasWon)
            jsonObject.put("showWinOverlay", gameState.showWinOverlay)

            val tilesArray = JSONArray()
            gameState.tiles.forEach { tile ->
                val tileObject = JSONObject()
                tileObject.put("id", tile.id)
                tileObject.put("value", tile.value)
                tileObject.put("position", tile.position)
                tilesArray.put(tileObject)
            }
            jsonObject.put("tiles", tilesArray)

            val file = File(context.filesDir, fileName)
            file.writeText(jsonObject.toString())

            saveBestScore(gameState.bestScore)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveBestScore(bestScore: Int) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("bestScore", bestScore)
            val file = File(context.filesDir, bestScoreFileName)
            file.writeText(jsonObject.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadBestScore(): Int {
        try {
            val file = File(context.filesDir, bestScoreFileName)
            if (!file.exists()) return 0

            val jsonString = file.readText()
            val jsonObject = JSONObject(jsonString)
            return jsonObject.getInt("bestScore")
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    fun loadGame(): GameState? {
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return null

            val jsonString = file.readText()
            val jsonObject = JSONObject(jsonString)

            val score = jsonObject.getInt("score")
            val bestScore = if (jsonObject.has("bestScore")) {
                jsonObject.getInt("bestScore")
            } else {
                loadBestScore()
            }
            val gameOver = jsonObject.getBoolean("gameOver")
            val hasWon = jsonObject.getBoolean("hasWon")
            val showWinOverlay = if (jsonObject.has("showWinOverlay")) {
                jsonObject.getBoolean("showWinOverlay")
            } else {
                true
            }

            val tilesArray = jsonObject.getJSONArray("tiles")
            val tiles = mutableListOf<Tile>()

            for (i in 0 until tilesArray.length()) {
                val tileObject = tilesArray.getJSONObject(i)
                tiles.add(
                    Tile(
                        id = tileObject.getInt("id"),
                        value = tileObject.getInt("value"),
                        position = tileObject.getInt("position")
                    )
                )
            }

            return GameState(tiles, score, bestScore, gameOver, hasWon, showWinOverlay)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun deleteGame() {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
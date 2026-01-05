package UPCE.BMTA.Game2048.repository

import android.content.Context
import UPCE.BMTA.Game2048.model.GameState
import UPCE.BMTA.Game2048.model.Tile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GameRepository(private val context: Context) {
    
    private val fileName = "game_state.json"
    
    fun saveGame(gameState: GameState) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("score", gameState.score)
            jsonObject.put("gameOver", gameState.gameOver)
            jsonObject.put("hasWon", gameState.hasWon)
            
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadGame(): GameState? {
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return null
            
            val jsonString = file.readText()
            val jsonObject = JSONObject(jsonString)
            
            val score = jsonObject.getInt("score")
            val gameOver = jsonObject.getBoolean("gameOver")
            val hasWon = jsonObject.getBoolean("hasWon")
            
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
            
            return GameState(tiles, score, gameOver, hasWon)
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

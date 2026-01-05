package UPCE.BMTA.Game2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import UPCE.BMTA.Game2048.model.Tile
import UPCE.BMTA.Game2048.ui.theme.Game2048Theme
import UPCE.BMTA.Game2048.viewmodel.GameViewModel
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Game2048Theme {
                GameScreen(viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveGame()
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.observeAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with title and score
            GameHeader(
                score = gameState?.score ?: 0,
                onNewGame = { viewModel.newGame() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {
                GameGrid(
                    tiles = gameState?.tiles ?: emptyList(),
                    onSwipe = { direction -> viewModel.move(direction) }
                )

                // Game Over Overlay
                if (gameState?.gameOver == true || (gameState?.hasWon == true && gameState?.gameOver == false)) {
                    GameOverOverlay(
                        hasWon = gameState?.hasWon ?: false,
                        onTryAgain = { viewModel.newGame() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Swipe to move tiles. Combine tiles with the same number to reach 2048!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GameHeader(score: Int, onNewGame: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = "2048",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Score and Button Column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Score Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFBBADA0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE",
                        fontSize = 12.sp,
                        color = Color(0xFFEEE4DA),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = score.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // New Game Button
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8F7A66)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("New Game", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GameGrid(
    tiles: List<Tile>,
    onSwipe: (GameViewModel.Direction) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val gridSize = 4
    val fullGrid = (0 until gridSize * gridSize).map { position ->
        tiles.find { it.position == position }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        // Reset offsets when starting a new drag
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragEnd = {
                        if (abs(offsetX) > abs(offsetY)) {
                            if (abs(offsetX) > 50) {
                                if (offsetX > 0) {
                                    onSwipe(GameViewModel.Direction.RIGHT)
                                } else {
                                    onSwipe(GameViewModel.Direction.LEFT)
                                }
                            }
                        } else {
                            if (abs(offsetY) > 50) {
                                if (offsetY > 0) {
                                    onSwipe(GameViewModel.Direction.DOWN)
                                } else {
                                    onSwipe(GameViewModel.Direction.UP)
                                }
                            }
                        }
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBBADA0)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false // Disable scrolling on the grid
        ) {
            items(fullGrid) { tile ->
                TileItem(tile = tile)
            }
        }
    }
}

@Composable
fun TileItem(tile: Tile?) {
    val (backgroundColor, textColor) = getTileColors(tile?.value)

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (tile != null) {
                Text(
                    text = tile.value.toString(),
                    fontSize = when {
                        tile.value < 100 -> 32.sp
                        tile.value < 1000 -> 28.sp
                        else -> 24.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    hasWon: Boolean,
    onTryAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (hasWon) "You Win!" else "Game Over!",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Button(
                onClick = onTryAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8F7A66)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Try Again",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun getTileColors(value: Int?): Pair<Color, Color> {
    return when (value) {
        null -> Pair(Color(0xFFCDC1B4), Color.Transparent)
        2 -> Pair(Color(0xFFEEE4DA), Color(0xFF776E65))
        4 -> Pair(Color(0xFFEDE0C8), Color(0xFF776E65))
        8 -> Pair(Color(0xFFF2B179), Color(0xFFF9F6F2))
        16 -> Pair(Color(0xFFF59563), Color(0xFFF9F6F2))
        32 -> Pair(Color(0xFFF67C5F), Color(0xFFF9F6F2))
        64 -> Pair(Color(0xFFF65E3B), Color(0xFFF9F6F2))
        128 -> Pair(Color(0xFFEDCF72), Color(0xFFF9F6F2))
        256 -> Pair(Color(0xFFEDCC61), Color(0xFFF9F6F2))
        512 -> Pair(Color(0xFFEDC850), Color(0xFFF9F6F2))
        1024 -> Pair(Color(0xFFEDC53F), Color(0xFFF9F6F2))
        2048 -> Pair(Color(0xFFEDC22E), Color(0xFFF9F6F2))
        else -> Pair(Color(0xFF3C3A32), Color(0xFFF9F6F2))
    }
}
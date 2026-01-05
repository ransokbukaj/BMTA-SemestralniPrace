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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { paddingValues ->
        if (isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(200.dp)
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GameHeaderVertical(
                            score = gameState?.score ?: 0,
                            bestScore = gameState?.bestScore ?: 0,
                            onNewGame = { viewModel.newGame() }
                        )

                        Text(
                            text = stringResource(R.string.instructions),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.85f)
                            .aspectRatio(1f)
                    ) {
                        GameGrid(
                            tiles = gameState?.tiles ?: emptyList(),
                            onSwipe = { direction -> viewModel.move(direction) }
                        )

                        if (gameState?.gameOver == true || (gameState?.hasWon == true && gameState?.showWinOverlay == true)) {
                            GameOverOverlay(
                                hasWon = gameState?.hasWon ?: false,
                                onTryAgain = { viewModel.newGame() },
                                onKeepPlaying = { viewModel.continueGame() }
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameHeader(
                    score = gameState?.score ?: 0,
                    bestScore = gameState?.bestScore ?: 0,
                    onNewGame = { viewModel.newGame() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                ) {
                    GameGrid(
                        tiles = gameState?.tiles ?: emptyList(),
                        onSwipe = { direction -> viewModel.move(direction) }
                    )

                    if (gameState?.gameOver == true || (gameState?.hasWon == true && gameState?.showWinOverlay == true)) {
                        GameOverOverlay(
                            hasWon = gameState?.hasWon ?: false,
                            onTryAgain = { viewModel.newGame() },
                            onKeepPlaying = { viewModel.continueGame() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.instructions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GameHeader(score: Int, bestScore: Int, onNewGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreCard(
                label = stringResource(R.string.best),
                score = bestScore,
                modifier = Modifier.weight(1f)
            )

            ScoreCard(
                label = stringResource(R.string.score),
                score = score,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onNewGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.game_button)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.new_game), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GameHeaderVertical(score: Int, bestScore: Int, onNewGame: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScoreCard(
            label = stringResource(R.string.best),
            score = bestScore,
            modifier = Modifier.fillMaxWidth()
        )

        ScoreCard(
            label = stringResource(R.string.score),
            score = score,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onNewGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.game_button)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.new_game), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.score_card_background)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = colorResource(R.color.score_label),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = score.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.white)
            )
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
            containerColor = colorResource(R.color.grid_background)
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
            userScrollEnabled = false
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
    onTryAgain: () -> Unit,
    onKeepPlaying: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.game_over_overlay)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (hasWon) stringResource(R.string.you_win) else stringResource(R.string.game_over),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.white)
            )

            if (hasWon) {
                Button(
                    onClick = onKeepPlaying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.game_button)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(
                        text = stringResource(R.string.keep_playing),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onTryAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.game_button)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(200.dp)
            ) {
                Text(
                    text = stringResource(R.string.try_again),
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
        null -> Pair(colorResource(R.color.tile_empty), Color.Transparent)
        2 -> Pair(colorResource(R.color.tile_2), colorResource(R.color.tile_text_light))
        4 -> Pair(colorResource(R.color.tile_4), colorResource(R.color.tile_text_dark))
        8 -> Pair(colorResource(R.color.tile_8), colorResource(R.color.tile_text_dark))
        16 -> Pair(colorResource(R.color.tile_16), colorResource(R.color.tile_text_dark))
        32 -> Pair(colorResource(R.color.tile_32), colorResource(R.color.tile_text_dark))
        64 -> Pair(colorResource(R.color.tile_64), colorResource(R.color.tile_text_dark))
        128 -> Pair(colorResource(R.color.tile_128), colorResource(R.color.tile_text_dark))
        256 -> Pair(colorResource(R.color.tile_256), colorResource(R.color.tile_text_light))
        512 -> Pair(colorResource(R.color.tile_512), colorResource(R.color.tile_text_light))
        1024 -> Pair(colorResource(R.color.tile_1024), colorResource(R.color.tile_text_light))
        2048 -> Pair(colorResource(R.color.tile_2048), colorResource(R.color.tile_text_light))
        else -> Pair(colorResource(R.color.tile_super), colorResource(R.color.tile_text_light))
    }
}
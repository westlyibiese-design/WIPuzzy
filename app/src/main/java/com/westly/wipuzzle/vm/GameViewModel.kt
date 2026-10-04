package com.westly.wipuzzle.vm

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.westly.wipuzzle.audio.SoundEngine
import com.westly.wipuzzle.data.LevelRecord
import com.westly.wipuzzle.data.Progress
import com.westly.wipuzzle.data.SaveStore
import com.westly.wipuzzle.data.Settings
import com.westly.wipuzzle.data.TOTAL_LEVELS
import com.westly.wipuzzle.data.imageAssetPath
import com.westly.wipuzzle.data.scoreFor
import com.westly.wipuzzle.data.sizeForLevel
import com.westly.wipuzzle.data.starsFor
import com.westly.wipuzzle.data.worldForLevel
import com.westly.wipuzzle.game.SlidingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Screen {
    data object Home : Screen
    data object Levels : Screen
    data object Game : Screen
    data object Custom : Screen
    data object Settings : Screen
}

data class SessionResult(
    val moves: Int,
    val seconds: Int,
    val stars: Int,
    val score: Int,
    val isNewBest: Boolean,
    val achievements: List<String>,
)

/** level == 0 means a custom (photo) puzzle. */
data class GameUi(
    val level: Int,
    val gridSize: Int,
    val image: ImageBitmap?,
    val loading: Boolean,
    val placement: List<Int>,
    val moves: Int = 0,
    val seconds: Int = 0,
    val paused: Boolean = false,
    val finished: Boolean = false,
    val result: SessionResult? = null,
) {
    val isCustom: Boolean get() = level == 0
}

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SaveStore(app)
    val sound = SoundEngine(app)

    var screen by mutableStateOf<Screen>(Screen.Home)
        private set
    var progress by mutableStateOf(store.loadProgress())
        private set
    var settings by mutableStateOf(store.loadSettings())
        private set
    var game by mutableStateOf<GameUi?>(null)
        private set

    @set:JvmName("assignCustomSource") var customSource by mutableStateOf<ImageBitmap?>(null)
        private set
    @set:JvmName("assignCustomSize") var customSize by mutableStateOf(3)
        private set

    private var engine: SlidingEngine? = null
    private var timerJob: Job? = null
    private var loadJob: Job? = null

    init {
        sound.apply(settings)
    }

    // ---------------- navigation ----------------

    fun goHome() {
        screen = Screen.Home
    }

    fun goLevels() {
        screen = Screen.Levels
    }

    fun goCustom() {
        screen = Screen.Custom
    }

    fun goSettings() {
        screen = Screen.Settings
    }

    fun back() {
        when (screen) {
            Screen.Game -> exitGame()
            Screen.Levels, Screen.Custom, Screen.Settings -> screen = Screen.Home
            Screen.Home -> Unit
        }
    }

    // ---------------- campaign ----------------

    fun openLevel(level: Int) {
        if (level < 1 || level > TOTAL_LEVELS || level > progress.unlocked) return
        progress = progress.copy(current = level)
        store.saveProgress(progress)
        beginSession(level, sizeForLevel(level), null)
        screen = Screen.Game
    }

    fun nextLevel() {
        val g = game ?: return
        val next = g.level + 1
        if (!g.isCustom && next <= TOTAL_LEVELS && next <= progress.unlocked) openLevel(next) else exitGame()
    }

    // ---------------- custom ----------------

    fun setCustomSource(image: ImageBitmap?) {
        customSource = image
    }

    fun setCustomSize(size: Int) {
        customSize = size.coerceIn(3, 4)
    }

    fun startCustom(image: ImageBitmap, size: Int) {
        beginSession(0, size, image)
        screen = Screen.Game
    }

    // ---------------- session ----------------

    private fun beginSession(level: Int, gridSize: Int, preset: ImageBitmap?) {
        timerJob?.cancel()
        loadJob?.cancel()
        val eng = SlidingEngine(gridSize).also { it.shuffle() }
        engine = eng
        game = GameUi(
            level = level,
            gridSize = gridSize,
            image = preset,
            loading = preset == null,
            placement = eng.placement().toList(),
        )
        loadJob = viewModelScope.launch {
            val img = preset ?: loadAssetImage(level)
            game = game?.copy(image = img, loading = false)
            startTimer()
        }
    }

    private suspend fun loadAssetImage(level: Int): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            getApplication<Application>().assets.open(imageAssetPath(level)).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val g = game ?: break
                if (!g.paused && !g.finished && !g.loading) {
                    game = g.copy(seconds = g.seconds + 1)
                }
            }
        }
    }

    fun tapTile(tile: Int) {
        val g = game ?: return
        val e = engine ?: return
        if (g.paused || g.finished || g.loading) return
        val pos = g.placement.getOrNull(tile) ?: return
        if (!e.move(pos)) return
        sound.click()
        val solved = e.isSolved()
        game = g.copy(placement = e.placement().toList(), moves = e.moveCount, finished = solved)
        if (solved) {
            viewModelScope.launch {
                delay(700)
                finishSession()
            }
        }
    }

    fun setPaused(paused: Boolean) {
        val g = game ?: return
        if (g.finished || g.loading) return
        game = g.copy(paused = paused)
    }

    fun pauseIfPlaying() {
        if (screen == Screen.Game) setPaused(true)
    }

    fun restart() {
        val g = game ?: return
        beginSession(g.level, g.gridSize, g.image)
    }

    fun exitGame() {
        val wasCustom = game?.isCustom == true
        timerJob?.cancel()
        loadJob?.cancel()
        game = null
        engine = null
        screen = if (wasCustom) Screen.Custom else Screen.Levels
    }

    private fun finishSession() {
        val g = game ?: return
        if (!g.finished || g.result != null) return
        timerJob?.cancel()

        val stars = starsFor(g.gridSize, g.moves, g.seconds)
        val score = scoreFor(g.gridSize, g.moves, g.seconds, stars)
        var isNewBest = false
        val earned = mutableListOf<String>()

        if (!g.isCustom) {
            val old = progress.levels[g.level]
            val firstClear = old == null || old.stars == 0
            isNewBest = firstClear || score > (old?.bestScore ?: 0)
            val record = LevelRecord(
                stars = maxOf(old?.stars ?: 0, stars),
                bestMoves = if (firstClear) g.moves else minOf(old!!.bestMoves, g.moves),
                bestSeconds = if (firstClear) g.seconds else minOf(old!!.bestSeconds, g.seconds),
                bestScore = maxOf(old?.bestScore ?: 0, score),
            )
            var p = progress.copy(
                levels = progress.levels + (g.level to record),
                totalMoves = progress.totalMoves + g.moves,
                totalSeconds = progress.totalSeconds + g.seconds,
                unlocked = if (g.level >= progress.unlocked && g.level < TOTAL_LEVELS) g.level + 1 else progress.unlocked,
            )
            val have = p.achievements.toMutableSet()
            fun claim(id: String, label: String) {
                if (have.add(id)) earned.add(label)
            }
            if (p.solved == 1) claim("first_win", "First solve")
            if (stars == 3) claim("three_star_${g.level}", "Three-star clear")
            if (g.level == 25 || g.level == 50 || g.level == 75 || g.level == 100) {
                claim("world_${g.level}", "${worldForLevel(g.level).name} cleared")
            }
            p = p.copy(achievements = have)
            progress = p
            store.saveProgress(p)
        }

        sound.win()
        game = g.copy(result = SessionResult(g.moves, g.seconds, stars, score, isNewBest, earned))
    }

    // ---------------- settings & data ----------------

    fun updateSettings(s: Settings) {
        settings = s
        store.saveSettings(s)
        sound.apply(s)
    }

    fun resetProgress() {
        store.resetProgress()
        progress = Progress()
    }

    override fun onCleared() {
        sound.release()
        super.onCleared()
    }
}

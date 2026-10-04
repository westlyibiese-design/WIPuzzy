package com.westly.wipuzzle.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.westly.wipuzzle.data.TOTAL_LEVELS
import com.westly.wipuzzle.data.worldForLevel
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.vm.GameUi
import com.westly.wipuzzle.vm.GameViewModel
import com.westly.wipuzzle.vm.SessionResult
import kotlin.math.roundToInt

@Composable
fun VictoryDialog(
    vm: GameViewModel,
    g: GameUi,
    result: SessionResult,
    onViewImage: () -> Unit,
) {
    val c = Wip.c
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(c.bg)
                .systemBarsPadding(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                val img = g.image
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, c.line, RoundedCornerShape(8.dp))
                        .background(c.surface)
                        .clickable { onViewImage() },
                ) {
                    if (img != null) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawImage(
                                image = img,
                                dstSize = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt()),
                                filterQuality = FilterQuality.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                Eyebrow(if (g.isCustom) "Custom puzzle" else "${worldForLevel(g.level).name} · Level ${g.level}")
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Txt("Solved", Type.display, Modifier.weight(1f))
                    AnimatedStars(result.stars)
                }
                if (result.isNewBest) {
                    Spacer(Modifier.height(2.dp))
                    Txt("New best", Type.body, color = c.accent)
                }

                Spacer(Modifier.height(20.dp))
                Hairline()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    StatBlock("Time", formatTime(result.seconds), Modifier.weight(1f))
                    StatBlock("Moves", result.moves.toString(), Modifier.weight(1f), Alignment.CenterHorizontally)
                    StatBlock("Score", result.score.toString(), Modifier.weight(1f), Alignment.End)
                }
                Hairline()

                if (result.achievements.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Eyebrow("Unlocked")
                    result.achievements.forEach {
                        Spacer(Modifier.height(4.dp))
                        Txt(it, Type.body)
                    }
                }

                Spacer(Modifier.height(24.dp))
                val hasNext = !g.isCustom && g.level < TOTAL_LEVELS
                if (hasNext) {
                    PrimaryButton("Next level", { vm.nextLevel() })
                } else {
                    PrimaryButton(
                        if (g.isCustom) "New photo" else "Back to levels",
                        { vm.exitGame() },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton("Play again", { vm.restart() }, Modifier.weight(1f))
                    if (hasNext) {
                        SecondaryButton("Levels", { vm.exitGame() }, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedStars(earned: Int) {
    val c = Wip.c
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0 until 3) {
            val scale by animateFloatAsState(
                targetValue = if (shown) 1f else 0f,
                animationSpec = tween(durationMillis = 320, delayMillis = 250 + i * 170),
                label = "star$i",
            )
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (i < earned) c.accent else c.line,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            )
        }
    }
}

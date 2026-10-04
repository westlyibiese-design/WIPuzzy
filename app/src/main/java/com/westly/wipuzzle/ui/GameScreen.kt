package com.westly.wipuzzle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.westly.wipuzzle.data.parForSize
import com.westly.wipuzzle.data.worldForLevel
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.vm.GameViewModel
import kotlin.math.roundToInt

@Composable
fun GameScreen(vm: GameViewModel) {
    val g = vm.game ?: return
    val c = Wip.c
    var showViewer by remember { mutableStateOf(false) }
    val par = parForSize(g.gridSize)

    Column(Modifier.fillMaxSize()) {
        // top bar
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackIcon { vm.exitGame() }
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Eyebrow(if (g.isCustom) "Custom puzzle" else worldForLevel(g.level).name)
                Txt(
                    if (g.isCustom) "${g.gridSize} × ${g.gridSize}" else "Level ${g.level}",
                    Type.heading,
                    maxLines = 1,
                )
            }
            // target image thumbnail
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, c.line, RoundedCornerShape(4.dp))
                    .background(c.surface)
                    .clickable(enabled = g.image != null) { showViewer = true },
            ) {
                val img = g.image
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
            Spacer(Modifier.width(4.dp))
            PauseIcon { vm.setPaused(true) }
            Spacer(Modifier.width(4.dp))
        }

        Hairline()

        // stats
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatBlock("Time", formatTime(g.seconds), Modifier.weight(1f))
            StatBlock("Moves", g.moves.toString(), Modifier.weight(1f), Alignment.CenterHorizontally)
            StatBlock("Par", par.moves.toString(), Modifier.weight(1f), Alignment.End)
        }

        // board
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (g.loading) {
                Eyebrow("Loading")
            } else {
                PuzzleBoard(
                    image = g.image,
                    gridSize = g.gridSize,
                    placement = g.placement,
                    onTap = { vm.tapTile(it) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // actions
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SecondaryButton("Restart", { vm.restart() }, Modifier.weight(1f))
            SecondaryButton("View image", { if (g.image != null) showViewer = true }, Modifier.weight(1f))
        }
    }

    if (g.paused && !g.finished) {
        PauseDialog(
            onResume = { vm.setPaused(false) },
            onRestart = { vm.restart() },
            onExit = { vm.exitGame() },
        )
    }

    val result = g.result
    if (result != null) {
        VictoryDialog(vm, g, result) { showViewer = true }
    }

    if (showViewer && g.image != null) {
        ImageViewerDialog(g.image) { showViewer = false }
    }
}

@Composable
private fun PauseDialog(onResume: () -> Unit, onRestart: () -> Unit, onExit: () -> Unit) {
    val c = Wip.c
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = c.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, c.line),
        ) {
            Column(Modifier.padding(24.dp)) {
                Eyebrow("Paused")
                Spacer(Modifier.height(4.dp))
                Txt("Timer stopped", Type.title)
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Resume", onResume)
                Spacer(Modifier.height(10.dp))
                SecondaryButton("Restart level", onRestart, Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextAction("Leave level", onExit)
                }
            }
        }
    }
}

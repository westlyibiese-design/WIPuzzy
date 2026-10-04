package com.westly.wipuzzle.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.westly.wipuzzle.game.SlidingEngine
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import kotlin.math.roundToInt

/**
 * The puzzle plate: a holding slot sitting above a gridSize x gridSize board.
 * Each tile is drawn as a window into the single source image and slides
 * between positions with a short eased animation.
 */
@Composable
fun PuzzleBoard(
    image: ImageBitmap?,
    gridSize: Int,
    placement: List<Int>,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Wip.c
    val gap = 3.dp
    val slotGap = 16.dp
    val pad = 10.dp
    val plateShape = RoundedCornerShape(10.dp)

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val availW = (maxWidth - pad * 2).coerceAtLeast(0.dp)
        val availH = (maxHeight - pad * 2 - slotGap).coerceAtLeast(0.dp)
        val sideByHeight = availH / (1f + 1f / gridSize)
        val side = minOf(availW, sideByHeight)
        val cell = (side - gap * (gridSize - 1)) / gridSize
        val boardTop = cell + slotGap
        val totalH = boardTop + side

        Box(
            Modifier
                .size(side + pad * 2, totalH + pad * 2)
                .clip(plateShape)
                .background(c.surface)
                .border(1.dp, c.line, plateShape)
                .padding(pad),
        ) {
            // holding slot
            Box(
                Modifier
                    .size(cell)
                    .border(1.dp, c.line, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Eyebrow("Hold", color = c.line)
            }
            // connector between the slot and the anchor cell
            Box(
                Modifier
                    .offset(x = cell / 2, y = cell)
                    .size(1.dp, slotGap)
                    .background(c.line),
            )
            // board well
            Box(
                Modifier
                    .offset(y = boardTop)
                    .size(side)
                    .clip(RoundedCornerShape(4.dp))
                    .background(c.bg),
            )

            for (tile in 0 until gridSize * gridSize) {
                val pos = placement.getOrNull(tile) ?: continue
                Tile(
                    tile = tile,
                    gridSize = gridSize,
                    image = image,
                    pos = pos,
                    cell = cell,
                    gap = gap,
                    boardTop = boardTop,
                    onTap = { onTap(tile) },
                )
            }
        }
    }
}

@Composable
private fun Tile(
    tile: Int,
    gridSize: Int,
    image: ImageBitmap?,
    pos: Int,
    cell: Dp,
    gap: Dp,
    boardTop: Dp,
    onTap: () -> Unit,
) {
    val density = LocalDensity.current
    val cellPx = with(density) { cell.toPx() }
    val gapPx = with(density) { gap.toPx() }
    val topPx = with(density) { boardTop.toPx() }

    val target = if (pos == SlidingEngine.HOLD) {
        Offset(0f, 0f)
    } else {
        Offset(
            (pos % gridSize) * (cellPx + gapPx),
            topPx + (pos / gridSize) * (cellPx + gapPx),
        )
    }
    val animated by animateOffsetAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing),
        label = "tile$tile",
    )

    val srcRow = tile / gridSize
    val srcCol = tile % gridSize

    Box(
        Modifier
            .size(cell)
            .graphicsLayer {
                translationX = animated.x
                translationY = animated.y
            }
            .clip(RoundedCornerShape(3.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Canvas(Modifier.fillMaxSize()) {
                val srcW = image.width / gridSize
                val srcH = image.height / gridSize
                drawImage(
                    image = image,
                    srcOffset = IntOffset(srcCol * srcW, srcRow * srcH),
                    srcSize = IntSize(srcW, srcH),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(this.size.width.roundToInt(), this.size.height.roundToInt()),
                    filterQuality = FilterQuality.Medium,
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        } else {
            Box(Modifier.fillMaxSize().background(Wip.c.raised), contentAlignment = Alignment.Center) {
                Txt("${tile + 1}", Type.title)
            }
        }
    }
}

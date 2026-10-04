package com.westly.wipuzzle.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.westly.wipuzzle.data.LevelRecord
import com.westly.wipuzzle.data.TOTAL_LEVELS
import com.westly.wipuzzle.data.WORLDS
import com.westly.wipuzzle.data.imageAssetUri
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.vm.GameViewModel

@Composable
fun LevelsScreen(vm: GameViewModel) {
    val c = Wip.c
    val ctx = LocalContext.current
    val p = vm.progress
    val gridState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        val level = p.current.coerceIn(1, TOTAL_LEVELS)
        val worldIndex = WORLDS.indexOfFirst { level in it.min..it.max }.coerceAtLeast(0)
        val index = (worldIndex + 1) + (level - 1)
        gridState.scrollToItem((index - 4).coerceAtLeast(0))
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Levels",
            onBack = { vm.back() },
            trailing = { Txt("${p.solved} / $TOTAL_LEVELS", Type.small, color = c.dim) },
        )
        Hairline()
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (world in WORLDS) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "world_${world.min}") {
                    val solvedInWorld = (world.min..world.max).count { (p.levels[it]?.stars ?: 0) > 0 }
                    Column(Modifier.padding(top = 24.dp, bottom = 6.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Txt(world.name, Type.heading, Modifier.weight(1f))
                            Txt("$solvedInWorld / ${world.count}", Type.small, color = c.dim)
                        }
                        Spacer(Modifier.height(2.dp))
                        Eyebrow("Levels ${world.min}–${world.max} · ${world.size} × ${world.size}")
                    }
                }
                items(
                    count = world.count,
                    key = { world.min + it },
                ) { i ->
                    val level = world.min + i
                    LevelCell(
                        level = level,
                        record = p.levels[level],
                        unlocked = level <= p.unlocked,
                        current = level == p.current.coerceIn(1, p.unlocked),
                        onClick = {
                            if (level <= p.unlocked) {
                                vm.openLevel(level)
                            } else {
                                Toast.makeText(ctx, "Solve level ${p.unlocked} to unlock this one", Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCell(
    level: Int,
    record: LevelRecord?,
    unlocked: Boolean,
    current: Boolean,
    onClick: () -> Unit,
) {
    val c = Wip.c
    val shape = RoundedCornerShape(6.dp)
    val stars = record?.stars ?: 0

    Box(
        Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(c.surface)
            .border(if (current) 2.dp else 1.dp, if (current) c.accent else c.line, shape)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = imageAssetUri(level),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = if (unlocked) null else ColorFilter.colorMatrix(GrayMatrix),
            alpha = if (unlocked) 1f else 0.45f,
            modifier = Modifier.fillMaxSize(),
        )
        if (unlocked) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color(0xB3000000),
                        ),
                    ),
            )
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Txt(level.toString(), Type.small, Modifier.weight(1f), color = Color.White)
                if (stars > 0) StarRow(stars, 9.dp, spacing = 0.dp)
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xCCFFFFFF),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private val GrayMatrix: ColorMatrix = ColorMatrix().apply { setToSaturation(0f) }

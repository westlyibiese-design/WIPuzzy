package com.westly.wipuzzle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.westly.wipuzzle.data.TOTAL_LEVELS
import com.westly.wipuzzle.data.imageAssetUri
import com.westly.wipuzzle.data.worldForLevel
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.vm.GameViewModel

@Composable
fun HomeScreen(vm: GameViewModel) {
    val c = Wip.c
    val p = vm.progress
    val level = p.current.coerceIn(1, p.unlocked)
    val world = worldForLevel(level)
    val heroShape = RoundedCornerShape(8.dp)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Eyebrow("Sliding picture puzzle")
        Spacer(Modifier.height(4.dp))
        Txt("WIPuzzle", Type.display)
        Spacer(Modifier.height(24.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clip(heroShape)
                .border(1.dp, c.line, heroShape)
                .background(c.surface)
                .clickable { vm.openLevel(level) },
        ) {
            AsyncImage(
                model = imageAssetUri(level),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.40f to Color.Transparent,
                            1f to Color(0xE6000000),
                        ),
                    ),
            )
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    Eyebrow(if (p.solved == 0) "Begin" else "Continue", color = Color(0xB3FFFFFF))
                    Txt("Level $level", Type.title, color = Color.White)
                    Txt(world.name, Type.body, color = Color(0xB3FFFFFF))
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Play",
                    tint = Color(0xFFE9C985),
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Txt("${p.solved} of $TOTAL_LEVELS solved", Type.small, color = c.dim)
            Txt("${p.totalStars} stars", Type.small, color = c.dim)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(2.dp).background(c.line)) {
            Box(
                Modifier
                    .fillMaxWidth(p.solved / TOTAL_LEVELS.toFloat())
                    .fillMaxHeight()
                    .background(c.accent),
            )
        }

        Spacer(Modifier.height(28.dp))
        Hairline()
        MenuRow("Levels", "100 levels across four worlds") { vm.goLevels() }
        Hairline()
        MenuRow("Custom puzzle", "Turn a photo from your phone into a puzzle") { vm.goCustom() }
        Hairline()
        MenuRow("Settings", "Sound, appearance and progress") { vm.goSettings() }
        Hairline()
        Spacer(Modifier.height(28.dp))
        Txt(
            "© NERIBO GROUP",
            Type.label,
            Modifier.fillMaxWidth(),
            color = Wip.c.dim,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    val c = Wip.c
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Txt(title, Type.heading)
            Spacer(Modifier.height(2.dp))
            Txt(subtitle, Type.small, color = c.dim)
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = c.dim,
        )
    }
}

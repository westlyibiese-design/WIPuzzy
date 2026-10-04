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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip

@Composable
fun Txt(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Wip.c.text,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Wip.c.dim) {
    Txt(text.uppercase(), Type.label, modifier, color)
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Wip.c.line))
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Wip.c
    Box(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(c.accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, Type.button, color = c.onAccent)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Wip.c
    Box(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, c.line, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, Type.button)
    }
}

@Composable
fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Wip.c.dim) {
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Txt(text, Type.button, color = color)
    }
}

@Composable
fun IconTap(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
fun BackIcon(onClick: () -> Unit) {
    IconTap(onClick) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Wip.c.text)
    }
}

@Composable
fun PauseIcon(onClick: () -> Unit) {
    val c = Wip.c
    IconTap(onClick) {
        Canvas(Modifier.size(18.dp)) {
            val w = this.size.width * 0.28f
            val r = CornerRadius(1.5.dp.toPx())
            drawRoundRect(c.text, Offset(this.size.width * 0.12f, 0f), Size(w, this.size.height), r)
            drawRoundRect(c.text, Offset(this.size.width * 0.60f, 0f), Size(w, this.size.height), r)
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackIcon(onBack)
        Spacer(Modifier.width(4.dp))
        Txt(title, Type.heading, Modifier.weight(1f), maxLines = 1)
        trailing()
        Spacer(Modifier.width(12.dp))
    }
}

@Composable
fun StarRow(earned: Int, iconSize: Dp, modifier: Modifier = Modifier, spacing: Dp = 2.dp) {
    val c = Wip.c
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        for (i in 0 until 3) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (i < earned) c.accent else c.line,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Wip.c
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .border(1.dp, c.line, shape),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(if (on) c.accent else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Txt(label, Type.button, color = if (on) c.onAccent else c.text)
            }
        }
    }
}

@Composable
fun StatBlock(label: String, value: String, modifier: Modifier = Modifier, align: Alignment.Horizontal = Alignment.Start) {
    Column(modifier, horizontalAlignment = align) {
        Eyebrow(label)
        Spacer(Modifier.height(2.dp))
        Txt(value, Type.numeral)
    }
}

fun formatTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

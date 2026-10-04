package com.westly.wipuzzle.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.vm.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun CustomScreen(vm: GameViewModel) {
    val c = Wip.c
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val source = vm.customSource

    var zoom by remember(source) { mutableFloatStateOf(1f) }
    var offset by remember(source) { mutableStateOf(Offset.Zero) }
    var boxPx by remember { mutableFloatStateOf(0f) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) { decodeSampled(ctx, uri, 2048) }
                if (bmp != null) {
                    vm.setCustomSource(bmp.asImageBitmap())
                } else {
                    Toast.makeText(ctx, "Couldn't open that photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun pickPhoto() {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun baseScale(): Float {
        val img = source ?: return 1f
        return if (boxPx <= 0f) 1f else boxPx / minOf(img.width, img.height).toFloat()
    }

    fun clampOffset(o: Offset, z: Float): Offset {
        val img = source ?: return o
        val sc = baseScale() * z
        val minX = minOf(boxPx - img.width * sc, 0f)
        val minY = minOf(boxPx - img.height * sc, 0f)
        return Offset(o.x.coerceIn(minX, 0f), o.y.coerceIn(minY, 0f))
    }

    LaunchedEffect(source, boxPx) {
        val img = source
        if (img != null && boxPx > 0f) {
            val sc = baseScale()
            offset = Offset((boxPx - img.width * sc) / 2f, (boxPx - img.height * sc) / 2f)
            zoom = 1f
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Custom puzzle", { vm.back() })
        Hairline()

        if (source == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Eyebrow("Step 1")
                Spacer(Modifier.height(4.dp))
                Txt("Choose a photo", Type.title)
                Spacer(Modifier.height(10.dp))
                Txt(
                    "Pick any picture from your phone and play it as a puzzle. The photo stays on your device and is never uploaded.",
                    Type.body,
                    color = c.dim,
                )
                Spacer(Modifier.height(28.dp))
                PrimaryButton("Choose photo", { pickPhoto() })
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(20.dp))
                Eyebrow("Step 2")
                Spacer(Modifier.height(4.dp))
                Txt("Frame your puzzle", Type.title)
                Spacer(Modifier.height(4.dp))
                Txt("Drag to move, pinch to zoom.", Type.small, color = c.dim)
                Spacer(Modifier.height(16.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clipToBounds()
                        .background(c.surface)
                        .border(1.dp, c.line)
                        .onSizeChanged { boxPx = it.width.toFloat() }
                        .pointerInput(source, boxPx) {
                            detectTransformGestures { centroid, pan, gestureZoom, _ ->
                                val newZoom = (zoom * gestureZoom).coerceIn(1f, 6f)
                                val ratio = newZoom / zoom
                                val moved = (offset - centroid) * ratio + centroid + pan
                                zoom = newZoom
                                offset = clampOffset(moved, newZoom)
                            }
                        },
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val sc = baseScale() * zoom
                        drawImage(
                            image = source,
                            dstOffset = IntOffset(offset.x.roundToInt(), offset.y.roundToInt()),
                            dstSize = IntSize((source.width * sc).roundToInt(), (source.height * sc).roundToInt()),
                            filterQuality = FilterQuality.Medium,
                        )
                        val w = this.size.width
                        val h = this.size.height
                        val line = Color.White.copy(alpha = 0.35f)
                        val stroke = 1.dp.toPx()
                        for (i in 1..2) {
                            drawLine(line, Offset(w * i / 3f, 0f), Offset(w * i / 3f, h), stroke)
                            drawLine(line, Offset(0f, h * i / 3f), Offset(w, h * i / 3f), stroke)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Eyebrow("Grid")
                Spacer(Modifier.height(8.dp))
                Segmented(
                    options = listOf("3 × 3", "4 × 4"),
                    selected = vm.customSize - 3,
                    onSelect = { vm.setCustomSize(it + 3) },
                )

                Spacer(Modifier.height(24.dp))
                PrimaryButton("Start puzzle", {
                    val sc = baseScale() * zoom
                    if (sc > 0f) {
                        val x = (-offset.x / sc).roundToInt()
                        val y = (-offset.y / sc).roundToInt()
                        val side = (boxPx / sc).roundToInt()
                        val cropped = cropSquare(source.asAndroidBitmap(), x, y, side)
                        vm.startCustom(cropped.asImageBitmap(), vm.customSize)
                    }
                })
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextAction("Choose a different photo", { pickPhoto() })
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun cropSquare(src: Bitmap, x: Int, y: Int, side: Int): Bitmap {
    val cx = x.coerceIn(0, src.width - 1)
    val cy = y.coerceIn(0, src.height - 1)
    val s = side.coerceAtLeast(1).coerceAtMost(minOf(src.width - cx, src.height - cy))
    val cropped = Bitmap.createBitmap(src, cx, cy, s, s)
    val target = s.coerceIn(512, 1600)
    return if (target != s) Bitmap.createScaledBitmap(cropped, target, target, true) else cropped
}

private fun decodeSampled(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val longest = maxOf(info.size.width, info.size.height)
                if (longest > maxSide) {
                    val f = maxSide.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * f).roundToInt().coerceAtLeast(1),
                        (info.size.height * f).roundToInt().coerceAtLeast(1),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }
    } catch (e: Exception) {
        null
    }
}

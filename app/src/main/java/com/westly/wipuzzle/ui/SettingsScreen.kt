package com.westly.wipuzzle.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.westly.wipuzzle.data.ThemeMode
import com.westly.wipuzzle.ui.theme.Type
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.vm.GameViewModel

@Composable
fun SettingsScreen(vm: GameViewModel) {
    val c = Wip.c
    val s = vm.settings
    val p = vm.progress
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Settings", { vm.back() })
        Hairline()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Eyebrow("Audio")
            Spacer(Modifier.height(8.dp))
            ToggleRow("Sound effects", s.sound) { vm.updateSettings(s.copy(sound = it)) }
            VolumeSlider(s.soundVolume, s.sound) { vm.updateSettings(s.copy(soundVolume = it)) }
            Hairline(Modifier.padding(top = 4.dp))
            ToggleRow("Music", s.music) { vm.updateSettings(s.copy(music = it)) }
            VolumeSlider(s.musicVolume, s.music) { vm.updateSettings(s.copy(musicVolume = it)) }

            Spacer(Modifier.height(28.dp))
            Eyebrow("Appearance")
            Spacer(Modifier.height(12.dp))
            Segmented(
                options = listOf("System", "Light", "Dark"),
                selected = s.theme.ordinal,
                onSelect = { vm.updateSettings(s.copy(theme = ThemeMode.values()[it])) },
            )

            Spacer(Modifier.height(28.dp))
            Eyebrow("Progress")
            Spacer(Modifier.height(12.dp))
            FactRow("Levels solved", "${p.solved} of 100")
            Hairline()
            FactRow("Stars earned", "${p.totalStars} of 300")
            Hairline()
            FactRow("Total moves", p.totalMoves.toString())
            Hairline()
            FactRow("Time played", formatPlayTime(p.totalSeconds))
            Spacer(Modifier.height(16.dp))
            TextAction("Reset progress", { confirmReset = true }, color = c.danger)
            Spacer(Modifier.height(32.dp))
            Txt(
                "© NERIBO GROUP",
                Type.label,
                Modifier.fillMaxWidth(),
                color = Wip.c.dim,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmReset) {
        Dialog(onDismissRequest = { confirmReset = false }) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = c.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, c.line),
            ) {
                Column(Modifier.padding(24.dp)) {
                    Txt("Reset progress?", Type.title)
                    Spacer(Modifier.height(8.dp))
                    Txt(
                        "All stars, best scores and unlocked levels will be erased. This can't be undone.",
                        Type.body,
                        color = c.dim,
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton("Cancel", { confirmReset = false }, Modifier.weight(1f))
                        SecondaryButton(
                            "Reset",
                            {
                                vm.resetProgress()
                                confirmReset = false
                            },
                            Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val c = Wip.c
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Txt(label, Type.body, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = c.onAccent,
                checkedTrackColor = c.accent,
                checkedBorderColor = c.accent,
                uncheckedThumbColor = c.dim,
                uncheckedTrackColor = c.raised,
                uncheckedBorderColor = c.line,
            ),
        )
    }
}

@Composable
private fun VolumeSlider(value: Float, enabled: Boolean, onChange: (Float) -> Unit) {
    val c = Wip.c
    Slider(
        value = value,
        onValueChange = onChange,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = c.accent,
            activeTrackColor = c.accent,
            inactiveTrackColor = c.line,
            disabledThumbColor = c.line,
            disabledActiveTrackColor = c.line,
            disabledInactiveTrackColor = c.raised,
        ),
    )
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Txt(label, Type.body, Modifier.weight(1f), color = Wip.c.dim)
        Txt(value, Type.body)
    }
}

private fun formatPlayTime(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m ${totalSeconds % 60}s"
}

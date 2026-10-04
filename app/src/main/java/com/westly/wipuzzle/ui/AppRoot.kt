package com.westly.wipuzzle.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.westly.wipuzzle.ui.theme.Wip
import com.westly.wipuzzle.ui.theme.WipTheme
import com.westly.wipuzzle.vm.GameViewModel
import com.westly.wipuzzle.vm.Screen

@Composable
fun AppRoot(vm: GameViewModel) {
    WipTheme(vm.settings.theme) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Wip.c.bg)
                .systemBarsPadding(),
        ) {
            BackHandler(enabled = vm.screen != Screen.Home) { vm.back() }
            Crossfade(
                targetState = vm.screen,
                animationSpec = tween(160),
                label = "screen",
            ) { s ->
                when (s) {
                    Screen.Home -> HomeScreen(vm)
                    Screen.Levels -> LevelsScreen(vm)
                    Screen.Game -> GameScreen(vm)
                    Screen.Custom -> CustomScreen(vm)
                    Screen.Settings -> SettingsScreen(vm)
                }
            }
        }
    }
}

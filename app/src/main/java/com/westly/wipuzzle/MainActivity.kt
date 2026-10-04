package com.westly.wipuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.westly.wipuzzle.ui.AppRoot
import com.westly.wipuzzle.vm.GameViewModel

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot(vm) }
    }

    override fun onStart() {
        super.onStart()
        vm.sound.onForeground()
    }

    override fun onStop() {
        vm.pauseIfPlaying()
        vm.sound.onBackground()
        super.onStop()
    }
}

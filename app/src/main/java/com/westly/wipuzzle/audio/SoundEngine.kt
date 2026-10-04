package com.westly.wipuzzle.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.westly.wipuzzle.R
import com.westly.wipuzzle.data.Settings

class SoundEngine(private val context: Context) {
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val clickId = pool.load(context, R.raw.click, 1)
    private val winId = pool.load(context, R.raw.win, 1)
    private var music: MediaPlayer? = null

    private var sfxOn = true
    private var musicOn = true
    private var sfxVol = 0.8f
    private var musicVol = 0.5f
    private var inForeground = false

    fun apply(s: Settings) {
        sfxOn = s.sound
        musicOn = s.music
        sfxVol = s.soundVolume
        musicVol = s.musicVolume
        music?.setVolume(musicVol, musicVol)
        if (!musicOn) pauseMusic() else if (inForeground) startMusic()
    }

    fun click() {
        if (sfxOn) pool.play(clickId, sfxVol, sfxVol, 1, 0, 1f)
    }

    fun win() {
        if (sfxOn) pool.play(winId, sfxVol, sfxVol, 1, 0, 1f)
    }

    fun onForeground() {
        inForeground = true
        startMusic()
    }

    fun onBackground() {
        inForeground = false
        pauseMusic()
    }

    private fun startMusic() {
        if (!musicOn) return
        try {
            if (music == null) {
                music = MediaPlayer.create(context, R.raw.background)?.apply {
                    isLooping = true
                    setVolume(musicVol, musicVol)
                }
            }
            music?.setVolume(musicVol, musicVol)
            music?.start()
        } catch (e: Exception) {
            // sound is optional; the game continues silently
        }
    }

    private fun pauseMusic() {
        try {
            val m = music
            if (m != null && m.isPlaying) m.pause()
        } catch (e: Exception) {
        }
    }

    fun release() {
        try {
            music?.release()
        } catch (e: Exception) {
        }
        music = null
        pool.release()
    }
}

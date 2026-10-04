package com.westly.wipuzzle.data

import android.content.Context
import org.json.JSONObject

data class LevelRecord(
    val stars: Int = 0,
    val bestMoves: Int = 0,
    val bestSeconds: Int = 0,
    val bestScore: Int = 0,
)

data class Progress(
    val unlocked: Int = 1,
    val current: Int = 1,
    val levels: Map<Int, LevelRecord> = emptyMap(),
    val achievements: Set<String> = emptySet(),
    val totalMoves: Long = 0,
    val totalSeconds: Long = 0,
) {
    val solved: Int get() = levels.values.count { it.stars > 0 }
    val totalStars: Int get() = levels.values.sumOf { it.stars }
}

enum class ThemeMode { System, Light, Dark }

data class Settings(
    val sound: Boolean = true,
    val music: Boolean = true,
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.5f,
    val theme: ThemeMode = ThemeMode.System,
)

class SaveStore(context: Context) {
    private val prefs = context.getSharedPreferences("wipuzzle_save", Context.MODE_PRIVATE)

    fun loadSettings(): Settings = Settings(
        sound = prefs.getBoolean("sound", true),
        music = prefs.getBoolean("music", true),
        soundVolume = prefs.getFloat("soundVolume", 0.8f),
        musicVolume = prefs.getFloat("musicVolume", 0.5f),
        theme = runCatching { ThemeMode.valueOf(prefs.getString("theme", "System") ?: "System") }
            .getOrDefault(ThemeMode.System),
    )

    fun saveSettings(s: Settings) {
        prefs.edit()
            .putBoolean("sound", s.sound)
            .putBoolean("music", s.music)
            .putFloat("soundVolume", s.soundVolume)
            .putFloat("musicVolume", s.musicVolume)
            .putString("theme", s.theme.name)
            .apply()
    }

    fun loadProgress(): Progress {
        val raw = prefs.getString("progress", null) ?: return Progress()
        return try {
            val o = JSONObject(raw)
            val levels = HashMap<Int, LevelRecord>()
            val lo = o.optJSONObject("levels")
            if (lo != null) {
                val keys = lo.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val r = lo.getJSONObject(k)
                    levels[k.toInt()] = LevelRecord(
                        stars = r.optInt("stars"),
                        bestMoves = r.optInt("moves"),
                        bestSeconds = r.optInt("seconds"),
                        bestScore = r.optInt("score"),
                    )
                }
            }
            val ach = HashSet<String>()
            val arr = o.optJSONArray("achievements")
            if (arr != null) for (i in 0 until arr.length()) ach.add(arr.getString(i))
            Progress(
                unlocked = o.optInt("unlocked", 1).coerceIn(1, TOTAL_LEVELS),
                current = o.optInt("current", 1).coerceIn(1, TOTAL_LEVELS),
                levels = levels,
                achievements = ach,
                totalMoves = o.optLong("totalMoves"),
                totalSeconds = o.optLong("totalSeconds"),
            )
        } catch (e: Exception) {
            Progress()
        }
    }

    fun saveProgress(p: Progress) {
        val o = JSONObject()
        o.put("unlocked", p.unlocked)
        o.put("current", p.current)
        o.put("totalMoves", p.totalMoves)
        o.put("totalSeconds", p.totalSeconds)
        val lo = JSONObject()
        for ((level, r) in p.levels) {
            lo.put(
                level.toString(),
                JSONObject()
                    .put("stars", r.stars)
                    .put("moves", r.bestMoves)
                    .put("seconds", r.bestSeconds)
                    .put("score", r.bestScore),
            )
        }
        o.put("levels", lo)
        o.put("achievements", org.json.JSONArray(p.achievements.toList()))
        prefs.edit().putString("progress", o.toString()).apply()
    }

    fun resetProgress() {
        prefs.edit().remove("progress").apply()
    }
}

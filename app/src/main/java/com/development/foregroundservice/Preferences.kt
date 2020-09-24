package com.development.foregroundservice

import android.content.Context

/**
 * Created by jorgesys on 12/09/2016.
 */
object Preferences {
    private const val PREFS = "SoundPreferences"
    fun setMediaPosition(ctx: Context, position: Int) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt("position", position).apply()
    }

    fun getMediaPosition(ctx: Context): Int {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt("position", 0)
    }
}
package com.dragsystem.muteme.data

import android.content.Context

object ConfigManager {
    private const val PREFS_NAME = "muteme_config"

    fun salvarModoBloqueio(context: Context, modo: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("modo_bloqueio", modo)
            .apply()
    }

    fun obterModoBloqueio(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("modo_bloqueio", "todos")
    }
}

package com.dragsystem.muteme.data

import android.content.Context
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity

object ConfigManager {
    private const val PREFS_NAME = "muteme_config"

    fun salvarTipoBloqueio(context: Context, tipo: String?) {
        val db = AppDatabase.getInstance(context)
        val config = db.configuracoesDao().obterConfiguracoes() ?: ConfiguracoesEntity()
        config.tipoBloqueio = tipo ?: "nenhum"
        db.configuracoesDao().salvarConfiguracoes(config)
    }

    fun obterTipoBloqueio(context: Context): String? {
        val db = AppDatabase.getInstance(context)
        return db.configuracoesDao().obterConfiguracoes()?.tipoBloqueio
    }
}

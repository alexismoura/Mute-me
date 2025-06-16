package com.dragsystem.muteme.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracoes")
data class ConfiguracoesEntity(
    @PrimaryKey val id: Int = 1, // sempre 1 — será um singleton
    var notificacoesAtivas: Boolean = true,
    var tipoBloqueio: String = "nenhum" // "nenhum" ou "fora_contatos"
)

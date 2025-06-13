package com.dragsystem.muteme.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracoes")
data class ConfiguracoesEntity(
    @PrimaryKey val id: Int = 1, // sempre 1 — será um singleton
    val modoBloqueio: String = "lista",
    val notificacoes: Boolean = true
)

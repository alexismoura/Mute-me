package com.dragsystem.muteme.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "numeros_bloqueados")
data class NumeroBloqueadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val numero: String,
    val dataAdicao: String // formato: dd/MM/yyyy (opcional)
)
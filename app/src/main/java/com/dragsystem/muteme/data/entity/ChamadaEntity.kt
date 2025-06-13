package com.dragsystem.muteme.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "chamadas")
class ChamadaEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var tipo: String? = null // recebida, perdida, feita
    var numero: String? = null
    var dataHora: String? = null
}

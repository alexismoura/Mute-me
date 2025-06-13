package com.dragsystem.muteme.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms")
class SmsEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var tipo: String? = null // enviado, recebido
    var numero: String? = null
    var mensagem: String? = null
    var dataHora: String? = null
}

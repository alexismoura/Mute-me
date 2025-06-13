package com.dragsystem.muteme.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dragsystem.muteme.data.entity.ChamadaEntity


@Dao
interface ChamadaDao {
    @Insert
    fun inserir(chamada: ChamadaEntity)

    @Query("SELECT * FROM chamadas ORDER BY id DESC")
    fun listarChamadas(): List<ChamadaEntity?>?
}


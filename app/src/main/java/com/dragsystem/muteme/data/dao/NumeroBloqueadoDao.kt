package com.dragsystem.muteme.data.dao

import androidx.room.*
import com.dragsystem.muteme.data.entity.NumeroBloqueadoEntity

@Dao
interface NumeroBloqueadoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun adicionarNumero(numero: NumeroBloqueadoEntity)

    @Delete
    fun removerNumero(numero: NumeroBloqueadoEntity)

    @Query("SELECT * FROM numeros_bloqueados ORDER BY id DESC")
    fun listarNumeros(): List<NumeroBloqueadoEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM numeros_bloqueados WHERE numero = :numero)")
    fun estaNaLista(numero: String): Boolean
}

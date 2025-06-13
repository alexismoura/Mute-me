package com.dragsystem.muteme.data.dao

import androidx.room.*
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity

@Dao
interface ConfiguracoesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarConfiguracoes(config: ConfiguracoesEntity)

    @Query("SELECT * FROM configuracoes WHERE id = 1")
    fun obterConfiguracoes(): ConfiguracoesEntity?

    @Update
    fun atualizarConfiguracoes(config: ConfiguracoesEntity)
}

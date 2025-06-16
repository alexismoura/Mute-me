package com.dragsystem.muteme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dragsystem.muteme.data.dao.ChamadaDao
import com.dragsystem.muteme.data.dao.ConfiguracoesDao
import com.dragsystem.muteme.data.dao.NumeroBloqueadoDao
import com.dragsystem.muteme.data.dao.SmsDao
import com.dragsystem.muteme.data.entity.ChamadaEntity
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity
import com.dragsystem.muteme.data.entity.NumeroBloqueadoEntity
import com.dragsystem.muteme.data.entity.SmsEntity

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Criar tabela temporária com a nova estrutura
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS configuracoes_temp (
                id INTEGER PRIMARY KEY NOT NULL,
                notificacoesAtivas INTEGER NOT NULL DEFAULT 1,
                tipoBloqueio TEXT NOT NULL DEFAULT 'nenhum'
            )
        """)

        // Copiar dados da tabela antiga para a nova
        database.execSQL("""
            INSERT INTO configuracoes_temp (id, notificacoesAtivas, tipoBloqueio)
            SELECT id, 
                   CASE WHEN notificacoes = 1 THEN 1 ELSE 0 END,
                   CASE 
                       WHEN modoBloqueio = 'todos' THEN 'fora_contatos'
                       WHEN modoBloqueio = 'apenas_contatos' THEN 'fora_contatos'
                       WHEN modoBloqueio = 'lista' THEN 'nenhum'
                       ELSE 'nenhum'
                   END
            FROM configuracoes
        """)

        // Remover tabela antiga
        database.execSQL("DROP TABLE configuracoes")

        // Renomear tabela temporária
        database.execSQL("ALTER TABLE configuracoes_temp RENAME TO configuracoes")
    }
}

@Database(entities = [ChamadaEntity::class, SmsEntity::class, ConfiguracoesEntity::class, NumeroBloqueadoEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chamadaDao(): ChamadaDao?
    abstract fun smsDao(): SmsDao?
    abstract fun configuracoesDao(): ConfiguracoesDao
    abstract fun numeroBloqueadoDao(): NumeroBloqueadoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "muteme_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

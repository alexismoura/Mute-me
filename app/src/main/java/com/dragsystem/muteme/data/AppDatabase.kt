package com.dragsystem.muteme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dragsystem.muteme.data.dao.ChamadaDao
import com.dragsystem.muteme.data.dao.ConfiguracoesDao
import com.dragsystem.muteme.data.dao.NumeroBloqueadoDao
import com.dragsystem.muteme.data.dao.SmsDao
import com.dragsystem.muteme.data.entity.ChamadaEntity
import com.dragsystem.muteme.data.entity.ConfiguracoesEntity
import com.dragsystem.muteme.data.entity.NumeroBloqueadoEntity
import com.dragsystem.muteme.data.entity.SmsEntity


@Database(entities = [ChamadaEntity::class, SmsEntity::class, ConfiguracoesEntity::class, NumeroBloqueadoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chamadaDao(): ChamadaDao?
    abstract fun smsDao(): SmsDao?
    abstract fun configuracoesDao(): ConfiguracoesDao
    abstract fun numeroBloqueadoDao (): NumeroBloqueadoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "muteme_db"
                )
                    .fallbackToDestructiveMigration() // 👈 RESET automático durante dev
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

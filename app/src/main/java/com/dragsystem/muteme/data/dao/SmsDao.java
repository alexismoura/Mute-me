package com.dragsystem.muteme.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dragsystem.muteme.data.entity.SmsEntity;

import java.util.List;

@Dao
public interface SmsDao {
    @Insert
    void inserir(SmsEntity sms);

    @Query("SELECT * FROM sms ORDER BY id DESC")
    List<SmsEntity> listarSms();
}


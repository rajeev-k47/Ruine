package com.example.ruine

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MailDao {
    @Insert
    suspend fun insertMail(data:Maildata)
    @Update
    suspend fun UpdateMail(data:Maildata)
    @Delete
    suspend fun DeleteMail(data:Maildata)

    @Query("SELECT * FROM mail_data")
    fun getData():LiveData<List<Maildata>>

}
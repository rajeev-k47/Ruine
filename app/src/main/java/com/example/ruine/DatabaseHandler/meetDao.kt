package com.example.ruine.DatabaseHandler

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface meetDao {
    @Insert
    suspend fun insertMail(data: meetData)
    @Update
    suspend fun UpdateMail(data: meetData)

    @Query("DELETE FROM meetData WHERE meetCode = :meetCode")
    suspend fun deleteByMeetId(meetCode: String)

    @Query("SELECT * FROM meetData")
    fun getData(): LiveData<List<meetData>>

}
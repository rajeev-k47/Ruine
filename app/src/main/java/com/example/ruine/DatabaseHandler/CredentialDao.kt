package com.example.ruine.DatabaseHandler

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CredentialDao {
    @Insert
    suspend fun insertCred(data: CredData)
    @Update
    suspend fun UpdateCred(data: CredData)
    @Delete
    suspend fun DeleteCred(data: CredData)

    @Query("SELECT * FROM cred_list")
    fun getData(): LiveData<List<CredData>>

}
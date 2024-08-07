package com.example.ruine.DatabaseHandler

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CredData::class], version = 1)
abstract class CredDatabase: RoomDatabase(){
    abstract fun credDao(): CredentialDao
}
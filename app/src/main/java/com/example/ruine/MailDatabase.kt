package com.example.ruine

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Maildata::class], version = 1)
abstract class MailDatabase:RoomDatabase (){
    abstract fun mailDao():MailDao
}
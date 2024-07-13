package com.example.ruine.DatabaseHandler

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Maildata::class], version = 1)
@TypeConverters(Converters::class)
abstract class MailDatabase:RoomDatabase (){
    abstract fun mailDao(): MailDao
}
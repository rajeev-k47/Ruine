package com.example.ruine.DatabaseHandler

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [meetData::class], version = 1)

abstract class meetDatabase:RoomDatabase() {
    abstract fun meetDao(): meetDao
}
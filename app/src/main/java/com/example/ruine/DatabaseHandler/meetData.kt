package com.example.ruine.DatabaseHandler

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetData")

data class meetData(@PrimaryKey(autoGenerate=true) val Sr: Int=0, val Uid:String?="", val meetname:String?="", val meetUri: String?="", val meetCode:String?="", val meetSubject:String?="",
                    val meetTime:String?="")
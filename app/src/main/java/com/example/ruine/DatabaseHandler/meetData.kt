package com.example.ruine.DatabaseHandler

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetData")

data class meetData(@PrimaryKey(autoGenerate=true) val Sr: Int=0, val Uid:String?="", val meetDate:String?="", val meetUri: String?="", val meetGroup:String?="", val meetSubject:String?="",
                    val meetTime:String?="")
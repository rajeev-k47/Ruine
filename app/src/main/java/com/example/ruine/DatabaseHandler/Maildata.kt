package com.example.ruine.DatabaseHandler

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "Mail_Data")

data class Maildata(@PrimaryKey(autoGenerate=true)val Sr:Long, val Id:String?="",val messageId:String,val Title: String?="",val Date:String?="",val Subject:String?="",val labelIds:JSONArray)

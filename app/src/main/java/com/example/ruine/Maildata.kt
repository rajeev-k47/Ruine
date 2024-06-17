package com.example.ruine

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.KeyPairGenerator

@Entity(tableName = "Mail_Data")

data class Maildata(@PrimaryKey(autoGenerate=true)val Sr:Long, val Id:String?="",val Title: String?="",val Date:String?="",val Subject:String?="")

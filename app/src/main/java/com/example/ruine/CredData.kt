package com.example.ruine

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cred_List")
data class CredData(@PrimaryKey(autoGenerate=true)val Sr:Long, val Refresh_Token:String,val uid:String)
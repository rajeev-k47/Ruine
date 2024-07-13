package com.example.ruine.Rvmodels

import com.example.ruine.R
import org.json.JSONArray

data class Rv_mail_model(val mail_profile:Int?= R.drawable.person, var messageId:String, var mail_title:String?="", var mail_date:String?="", var mail_snippet:String?="",var labelIds:JSONArray, var NewTag:Boolean?=false){

}

package com.example.ruine

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.ruine.DatabaseHandler.CredDatabase
import com.example.ruine.Rvmodels.RvMembersModel
import com.example.ruine.Rvmodels.Rvmodel
import com.example.ruine.databinding.ActivityMailSendingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.Base64

class MailSending : AppCompatActivity() {
    val binding:ActivityMailSendingBinding by lazy {
        ActivityMailSendingBinding.inflate(layoutInflater)
    }
    lateinit var  Creddatabase: CredDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var groupValueEventListener: ValueEventListener
    private lateinit var memberEventListener: ValueEventListener
    private val suggestions = mutableListOf<String>()

    lateinit var auth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        Creddatabase= Room.databaseBuilder(this, CredDatabase::class.java,"Cred").build()
        databaseReference = FirebaseDatabase.getInstance().reference

        auth=FirebaseAuth.getInstance()
        val From = auth.currentUser?.email

        val Fromtext=binding.from
        Fromtext.setText(From)
        Fromtext.isFocusable = false
        Fromtext.isFocusableInTouchMode = false
        val To = binding.to
        val subject = binding.subject
        val context = binding.context
        binding.sendMail.setOnClickListener {
            if(From.toString().isNotEmpty()&&To.text.isNotEmpty()&&subject.text.isNotEmpty()){
                if(isGmailAddress(To.text.toString())){
                sendMail(To.text.toString(),subject.text.toString(),context.text.toString())}
            else{
                    if(verifyGroup(To.text.toString())){
                     sendMultipleMessages(To.text.toString(),subject.text.toString(),context.text.toString()) }

            }
            }
        }
        val currentuser = auth.currentUser
        currentuser?.let { user ->
            val groupref = databaseReference.child("users").child(user.uid).child("groupmail")
            groupValueEventListener=object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (groups in snapshot.children) {
                        val grps_from_database = groups.getValue(Rvmodel::class.java)
                        grps_from_database?.let {
                            suggestions.add(it.name!!)
                        }
                    }
                    binding.LoadSendEmail.visibility=View.GONE
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MailSending,"Data Fetching Failed!!" , Toast.LENGTH_SHORT).show()
                }

            }
            groupref.addValueEventListener(groupValueEventListener)
        }

        val adapter =ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line,suggestions)
        To.setAdapter(adapter)
        To.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            To.setText(selectedItem)
        }

    }
    private fun sendMail( To:String, Subject:String, Context:String){
        val emailContent = "To: $To\r\n" +
                "Subject: $Subject\r\n" +
                "\r\n" +
                Context
        val encodedEmail = Base64.getUrlEncoder().encodeToString(emailContent.toByteArray(Charsets.UTF_8))
        val emailPayloadJson = JSONObject().apply {
            put("raw", encodedEmail)
        }
        RequireToken(emailPayloadJson)

    }
    private fun RequireToken(payload:JSONObject){
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Creddatabase.credDao().getData().observe(this@MailSending, Observer {
                    var RefToken=""
                    for(item in it){
                        if(item.uid==auth.currentUser?.uid){
                            RefToken=item.Refresh_Token
                        }
                    }
                    if(RefToken!=""){
                        val accessUrl = "https://oauth2.googleapis.com/token"
                        val clientId = getString(R.string.client_id)
                        val clientSecret = getString(R.string.client_secret)

                        val formBody = FormBody.Builder()
                            .add("client_secret", clientSecret)
                            .add("refresh_token", RefToken)
                            .add("grant_type", "refresh_token")
                            .add("client_id", clientId)
                            .build()

                        val request = Request.Builder()
                            .url(accessUrl)
                            .post(formBody)
                            .build()

                        val client = OkHttpClient()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                println("Request failed: ${e.message}")
                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseBody = response.body?.string()
                                val json = JSONObject(responseBody)
                                val AccessToken = json.getString("access_token")
                                    sendMessaage(AccessToken,payload)
                                //implement no internet exception
                            }
                        })

                    }
                })
            }
        }
    }
    private fun verifyGroup(groupName:String):Boolean{
        for(groups in suggestions){
            if(groupName==groups){
                return true
            }
        }
        return false
    }
    private fun sendMessaage(accessToken:String,payload:JSONObject){
        val requestBody = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                println("Failed to send email: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    showFailureToast("Email Sent !!")
                    println("Email sent successfully")
                } else {
                    println("Failed to send email: ${response.code} - ${response.message}")
                }
            }
        })
    }
    private fun showFailureToast(message: String) {
        runOnUiThread {
            FancyToast.makeText(this, "Mail Sent", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, R.drawable.tick, false).show();

        }
    }
    fun isGmailAddress(email: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return regex.matches(email)
    }
    private fun sendMultipleMessages(Gname: String,Subject: String,Context: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val membRef = databaseReference.child("users").child(user.uid).child("groupmail")
            memberEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (groups in snapshot.children) {
                        val groupName = groups.child("name").getValue(String::class.java)
                        groupName?.let {
                            if (it == Gname) {
                                val membersSnapshot = groups.child("members")
                                for (member in membersSnapshot.children) {
                                    val memberName = member.getValue(RvMembersModel::class.java)
                                    memberName?.let { name ->
                                        Log.d("dies","$memberName")
                                        sendMail(name.MemberMail!!,Subject,Context)
                                    }
                                }
                                binding.LoadSendEmail.visibility = View.GONE
                                finish()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MailSending, "Data Fetching Failed!!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            membRef.addValueEventListener(memberEventListener)
        }
    }}

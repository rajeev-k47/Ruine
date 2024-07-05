package com.example.ruine

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ruine.databinding.ActivityViewMailBinding
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.ruine.DatabaseHandler.CredDatabase
import com.example.ruine.handlerClasses.getNewMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class ViewMail : AppCompatActivity() {
    val binding:ActivityViewMailBinding by lazy {
        ActivityViewMailBinding.inflate(layoutInflater)
    }
    lateinit var  Creddatabase: CredDatabase
    lateinit var auth: FirebaseAuth
    lateinit var webView: WebView
    val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val MessageId=intent.getStringExtra("messageID").toString()
        webView= binding.webview
        webView.settings.javaScriptEnabled=true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
            }
        }

        auth=FirebaseAuth.getInstance()
        Creddatabase= Room.databaseBuilder(this, CredDatabase::class.java,"Cred").build()

        binding.mailviewback.setOnClickListener {
            finish()
        }

        fetchNewMessage(MessageId)


    }
    private fun fetchNewMessage(MessageId: String){
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Creddatabase.credDao().getData().observe(this@ViewMail, Observer {
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
                                getNewMessages(AccessToken,MessageId)

                            }
                        })

                    }
                })
            }
        }
    }
    private fun getNewMessages(AccessToken:String,MessageId:String){
        val request = Request.Builder()
            .url(url+MessageId)
            .header("Authorization", "Bearer $AccessToken")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody!!)
                val payload = json.getJSONObject("payload")
                val header = payload.getJSONArray("headers")
                if(payload.getString("mimeType")=="text/html"){
                   val data =  payload.getJSONObject("body").getString("data")
                    decodeNUpdate(data)
                }
                else if(payload.getString("mimeType")=="text/plain"){
                    var data =  payload.getJSONObject("body").getString("data")
                    data =decodeNUpdate(data,true)
                    runOnUiThread {
                        binding.cardView.visibility=View.GONE
                        binding.scrollviewtext.visibility=View.VISIBLE
                        binding.message.text = data
                    }

                }
                else if(payload.getString("mimeType")=="multipart/alternative"){
                    val parts = payload.getJSONArray("parts")
                    for(item in 0..parts.length()){
                        if(parts.getJSONObject(item).getString("mimeType")=="text/html"){
                            val data=parts.getJSONObject(item).getJSONObject("body").getString("data")
                            decodeNUpdate(data)
                            break
                        }
                    }
                }
                Log.d("hallo","${payload.getString("mimeType")}")

                var subject=""
                var title=""
                for (i in 0 until header.length()) {

                    if (header.getJSONObject(i).getString("name") == "Subject") {
                        subject = header.getJSONObject(i).getString("value")
                        subject = subject.replace("\\u2019", "'")

                    }
                    if (header.getJSONObject(i).getString("name") == "From") {
                        title = header.getJSONObject(i).getString("value").toString()}
                    runOnUiThread {
                        binding.mailLoad.visibility=View.GONE
                        binding.maiilviewmailtitle.setText(subject)
                        binding.mailviewmailsender.setText(title)
                    }

                }


            }
        })
    }
    private fun decodeNUpdate(data:String,manualrun:Boolean=false):String{
        val base64Encoded = data.replace("-", "+").replace("_", "/")
        val bytes = Base64.decode(base64Encoded, Base64.DEFAULT)

        val decodedString = String(bytes, Charsets.UTF_8)
        if(manualrun){return decodedString}
        runOnUiThread {
            webView.loadDataWithBaseURL(null, decodedString, "text/html", "UTF-8", null)
        }
        return ""
    }
}
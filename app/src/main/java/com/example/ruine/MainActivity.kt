package com.example.ruine

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.ruine.DatabaseHandler.CredData
import com.example.ruine.DatabaseHandler.CredDatabase
import com.example.ruine.DatabaseHandler.MailDatabase
import com.example.ruine.DatabaseHandler.Maildata
import com.example.ruine.Rvmodels.Rv_mail_model
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.glide.transformations.CropCircleTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.ruine.viewModels.mailViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private val mailViewModel: mailViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    lateinit var  Creddatabase: CredDatabase
    lateinit var  database: MailDatabase
    private lateinit var ACCESS_TOKEN :String
    private lateinit var REFRESH_TOKEN :String
    val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/"
    private lateinit var MESSAGES: JSONArray
    private lateinit var MESSAGE_IDS:MutableList<String>
    private var datalist=ArrayList<Rv_mail_model>()
    val datePattern = Pattern.compile(
        "\\b(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun), (\\d{1,2} \\w{3}) \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}\\b"
    )
    val TitlePattern = Pattern.compile("([^<]+)")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        database= Room.databaseBuilder(this@MainActivity, MailDatabase::class.java,"MailDB").build()
        Creddatabase=Room.databaseBuilder(this@MainActivity, CredDatabase::class.java,"Cred").build()

        auth=FirebaseAuth.getInstance()


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(navController)

        var deeplink = intent.data

        val ImageUri = FirebaseAuth.getInstance().currentUser?.photoUrl
        val Email = FirebaseAuth.getInstance().currentUser?.email
        var Profile=findViewById<ImageView>(R.id.Profile_Btn)

        if(ImageUri!=null){
            ImageUri.let {
                Glide.with(this)
                    .load(it)
                    .transform(CropCircleTransformation())
                    .into(Profile)
            }
        }else{
            val profileLetter = getProfileLetter(Email)
            val profileBitmap = createProfileBitmap(profileLetter)
            Profile.setImageBitmap(profileBitmap)
        }

        Profile.setOnClickListener {
            val bottomSheetDialog:BottomSheetDialogFragment=Profile_Bottom()
            bottomSheetDialog.show(this.supportFragmentManager ,"Bottom")
            bottomSheetDialog.enterTransition
//            bottomSheetDialog.dismiss()
        }

        //========================================================================//

            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    database.mailDao().getData().observe(this@MainActivity, Observer {
                        val userdata = userdataexist(it)

                        if(deeplink!=null && userdata){

                        }
                        else if(deeplink==null && userdata){

                        }
                        else if(deeplink!=null && !userdata){
                            ACCESS_TOKEN= deeplink.getQueryParameter("access_token")!!
                            REFRESH_TOKEN=deeplink.getQueryParameter("refresh_token")!!
                            register_token(REFRESH_TOKEN)
                            fetchEmails()
                            intent.data=null
                        }

                    })
                }
            }





    }
    private  fun register_token(register: String) {
        lifecycleScope.launch {
            if (register != "undefined" &&register!="") {
                Creddatabase.credDao().insertCred(CredData(0, register, auth.currentUser?.uid!!))
            }
        }
    }
    private fun fetchEmails() {

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $ACCESS_TOKEN")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody)
                MESSAGES = json.getJSONArray("messages")
                MESSAGE_IDS = mutableListOf()
                for (i in 0 until MESSAGES.length()) {
                    val message = MESSAGES.getJSONObject(i)
                    MESSAGE_IDS.add(message.getString("id"))
                }
                fetchMessagesSequentially(0)
            }
        })
    }

    private fun fetchMessagesSequentially(index: Int) {

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                mailViewModel.updateProgress(index)
            }}

        if (index >= MESSAGE_IDS.size) {

            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    datalist.reverse()
                    for (item in datalist){
                        database.mailDao().insertMail(Maildata(0,auth.currentUser?.uid!!,item.messageId,item.mail_title!!, item.mail_date!!, item.mail_snippet!!))
                    }
                }}
            return
        }
        val client = OkHttpClient()
        val messagerequest = Request.Builder()
            .url(url + MESSAGE_IDS[index])
            .header("Authorization", "Bearer $ACCESS_TOKEN")
            .build()
        client.newCall(messagerequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                fetchMessagesSequentially(index + 1)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody)
                val payload = json.getJSONObject("payload")
                val header = payload.getJSONArray("headers")
                var date = ""
                var title = ""
                var subject = ""

                try {
                    for (i in 0 until header.length()) {
                        if (header.getJSONObject(i).getString("name") == "Date") {
                            date = header.getJSONObject(i).getString("value")
                            val matcher = datePattern.matcher(date)
                            if (matcher.find()) {
                                date = matcher.group(1)
                            }
                        }

                        if (header.getJSONObject(i).getString("name") == "Subject") {
                            subject = header.getJSONObject(i).getString("value")
                            subject = subject.replace("\\u2019", "'")
                        }
                        if (header.getJSONObject(i).getString("name") == "From") {
                            title = header.getJSONObject(i).getString("value")
                            val matcher = TitlePattern.matcher(title)
                            if (matcher.find()) {
                                title = matcher.group(1).trim()
                            }
                        }

                    }

                    datalist.add(Rv_mail_model(R.drawable.person,MESSAGE_IDS[index], title, date, subject))


                } catch (error: JSONException) {
                    Log.d("Catch", "$error")
                } finally {
                    //Proceed to fetch next message
                    fetchMessagesSequentially(index + 1)
                }
            }
        })
    }

    private fun userdataexist(list: List<Maildata>):Boolean{
        for(item in list){
            if (item.Id==auth.currentUser?.uid){
                return true;
            }
        }
        return false;
    }
    private fun getProfileLetter(email: String?): String {
        return if (!email.isNullOrEmpty()) {
            email.first().uppercaseChar().toString()
        } else {
            "U"
        }
    }
    @SuppressLint("ResourceAsColor")
    private fun createProfileBitmap(letter: String): Bitmap {
        val bitmap = Bitmap.createBitmap(200,200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintBackground = Paint()
        paintBackground.color = ContextCompat.getColor(this, R.color.gery)
        canvas.drawCircle(100F, 100F, 100F, paintBackground)

        val paintText = Paint()
        paintText.color = R.color.black
        paintText.textSize = 150f
        paintText.isAntiAlias = true
        paintText.textAlign = Paint.Align.CENTER

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2)
        canvas.drawText(letter, xPos.toFloat(), yPos, paintText)

        return bitmap
    }
}
package com.example.ruine.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ruine.DatabaseHandler.CredDatabase
import com.example.ruine.DatabaseHandler.MailDatabase
import com.example.ruine.MailSending
import com.example.ruine.DatabaseHandler.Maildata
import com.example.ruine.R
import com.example.ruine.Adapters.Rv_mail_Adapter
import com.example.ruine.Rvmodels.Rv_mail_model
import com.example.ruine.databinding.FragmentMailsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.ruine.viewModels.mailViewModel
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern


class mails : Fragment(){
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val binding:FragmentMailsBinding by lazy {
        FragmentMailsBinding.inflate(layoutInflater)
    }
    private val mailViewModel: mailViewModel by activityViewModels()
    val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/"
    private lateinit var MESSAGES: JSONArray
    private lateinit var auth: FirebaseAuth
    private var datalist=ArrayList<Rv_mail_model>()
    private var Newdatalist=ArrayList<Rv_mail_model>()
    private var CatergoryList=ArrayList<Rv_mail_model>()
    lateinit var  database: MailDatabase
    lateinit var  Creddatabase: CredDatabase
    var MessageUpdated=false
    var CurrentTab="PRIMARY"

    val datePattern = Pattern.compile(
//        "(\\d{1,2} \\w{3}) \\d{4}"
        "(\\d{1,2}) (\\w{3})"
    )
    val TitlePattern = Pattern.compile("([^<]+)")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth=FirebaseAuth.getInstance()

        swipeRefreshLayout=binding.swipe
        swipeRefreshLayout.setOnRefreshListener {
            if(MessageUpdated){
            fetchNewMessages()}else{swipeRefreshLayout.isRefreshing=false}
        }

        database= Room.databaseBuilder(requireContext(), MailDatabase::class.java,"MailDB").build()
        Creddatabase=Room.databaseBuilder(requireContext(), CredDatabase::class.java,"Cred").build()
        binding.progressBar.visibility=View.INVISIBLE
        binding.progressFetch.visibility=View.INVISIBLE

        binding.compose.setOnClickListener{
            activity?.startActivity(Intent(requireContext(),MailSending::class.java))
        }
        binding.LoadMails.visibility=View.VISIBLE

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val tabName = tab?.text.toString().uppercase().replace("\\s".toRegex(), "")
                CurrentTab=tabName
                binding.LoadMails.visibility=View.VISIBLE
                lifecycleScope.launch {
                    withContext(Dispatchers.IO){
                        filterLabel(tabName)
                    }
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
//                println(tab.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                println(tab.toString())
            }
        })

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {

                database.mailDao().getData().observe(this@mails, Observer {
                    val data = userdataexist(it)
                    if(data){
                        for (item in it.reversed()){
                            if(item.Id==auth.currentUser?.uid&&!MessageUpdated){
                                datalist.add(Rv_mail_model(R.drawable.person,item.messageId,item.Title,item.Date,item.Subject,item.labelIds))
                            }
                        }
//                        manageAdapter(datalist)
                        filterLabel(CurrentTab)//random shit
                        binding.LoadMails.visibility=View.GONE
                        if(!MessageUpdated){
                        fetchNewMessages();
                        }
                    }
                    else{
                        binding.LoadMails.visibility=View.GONE
                        mailViewModel.progress.observe(this@mails, Observer { progress ->
                            binding.progressBar.visibility=View.VISIBLE
                            binding.progressFetch.visibility=View.VISIBLE
                            binding.progressBar.progress = progress
                            Log.d("debugprocess","${progress}")

                        })

                    }

                })
            }
        }

    }
    private fun userdataexist(list: List<Maildata>):Boolean{
        for(item in list){
            if (item.Id==auth.currentUser?.uid){
                return true;
            }
        }
        return false;
    }


    private fun fetchNewMessages(){
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Creddatabase.credDao().getData().observe(this@mails, Observer {
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
                                getNewMessages(AccessToken)

                            }
                        })

                    }
                })
            }
        }
    }
    private fun getNewMessages(AccessToken:String){
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $AccessToken")
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
                val index = checkUpdateList(MESSAGES,datalist)
                Log.d("index","$index")
//                datalist.clear()
                if(index>0) {fetchMessagesSequentially(0,index,MESSAGES,AccessToken)}
                else{swipeRefreshLayout.isRefreshing = false;MessageUpdated=true}


            }
        })
    }

    private fun manageAdapter(List:ArrayList<Rv_mail_model>){
        binding.rvMail.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        val adapter = Rv_mail_Adapter(List,requireContext())
        binding.rvMail.adapter = adapter
    }
    private fun checkUpdateList(messages:JSONArray, itemList:ArrayList<Rv_mail_model>):Int{
        var currentLopper = 0
        for (i in 0 until messages.length()) {

            val index = itemList.indexOfFirst { it.messageId== messages.getJSONObject(i).getString("id") }
            if (index != -1 ) {
                return currentLopper
            }
            currentLopper+=1

        }
        return Int.MIN_VALUE
    }
    private fun fetchMessagesSequentially(index: Int,mIndex:Int,Messages:JSONArray,AccessToken: String) {


        if (index >= mIndex) {
            MessageUpdated=true
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    Newdatalist.reverse()
                    for (item in Newdatalist){
                        database.mailDao().insertMail(Maildata(0,auth.currentUser?.uid!!,item.messageId,item.mail_title!!, item.mail_date!!, item.mail_snippet!!,item.labelIds))
                        datalist.add(0,
                            Rv_mail_model(R.drawable.person,item.messageId,item.mail_title,item.mail_date,item.mail_snippet,item.labelIds,true)
                        )
                    }
                }}
            swipeRefreshLayout.isRefreshing = false
            return
        }
        val client = OkHttpClient()
        val messagerequest = Request.Builder()
            .url(url + Messages.getJSONObject(index).getString("id"))
            .header("Authorization", "Bearer $AccessToken")
            .build()
        client.newCall(messagerequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                fetchMessagesSequentially(index + 1,mIndex,Messages,AccessToken)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody)
                val payload = json.getJSONObject("payload")
                val labelIds = json.getJSONArray("labelIds")
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
                                val day = matcher.group(1).toInt().toString()
                                val month = matcher.group(2)
                                date = "$day $month"
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
                            }else{date=""}
                        }

                    }

                    Newdatalist.add(Rv_mail_model(R.drawable.person,Messages.getJSONObject(index).getString("id"), title, date, subject,labelIds))


                } catch (error: JSONException) {
                    Log.d("Catch", "$error")
                } finally {
                    fetchMessagesSequentially(index + 1,mIndex,Messages,AccessToken)
                }
            }
        })
    }

    private fun filterLabel(Category:String){
        CatergoryList.clear()
        for(item in datalist){
            for(items in 0..<item.labelIds.length()){
                val label = item.labelIds[items]
               if(label==Category){
                   CatergoryList.add(Rv_mail_model(item.mail_profile,item.messageId,item.mail_title,item.mail_date,item.mail_snippet,item.labelIds))
               }else if(Category=="PRIMARY"&&label=="CATEGORY_PROMOTIONS"){
                   CatergoryList.add(Rv_mail_model(item.mail_profile,item.messageId,item.mail_title,item.mail_date,item.mail_snippet,item.labelIds))
               }
            }
        }
        if(Category=="PRIMARY"){
            var filteredList = ArrayList<Rv_mail_model>()
             filteredList = datalist.filterNot { it in CatergoryList } as ArrayList<Rv_mail_model>
            activity?.runOnUiThread {
                binding.LoadMails.visibility=View.GONE
                manageAdapter(filteredList)
            }
        }
        else if(Category=="ALLMAIL"){
            activity?.runOnUiThread {
                manageAdapter(datalist)
                binding.LoadMails.visibility=View.GONE

            }
        }
        else{
        activity?.runOnUiThread {
            binding.LoadMails.visibility=View.GONE
            manageAdapter(CatergoryList)
            }
        }
    }


}
package com.example.ruine.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ruine.Auth_Redirection
import com.example.ruine.R
import com.example.ruine.Rv_mail_Adapter
import com.example.ruine.Rv_mail_model
import com.example.ruine.databinding.FragmentMailsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Date
import java.util.regex.Pattern


class mails : Fragment() {
    private val binding:FragmentMailsBinding by lazy {
        FragmentMailsBinding.inflate(layoutInflater)
    }
    val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/"
    private lateinit var ACCESS_TOKEN :String
    private lateinit var REFRESH_TOKEN :String
    private lateinit var MESSAGES: JSONArray
    private lateinit var MESSAGE_IDS:MutableList<String>
    private var datalist=ArrayList<Rv_mail_model>()

    val datePattern = Pattern.compile(
        "\\b(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun), (\\d{1,2} \\w{3}) \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}\\b"
    )
    val TitlePattern = Pattern.compile("([^<]+)")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deeplink : Uri? =arguments?.getString("arg")?.toUri()
        Log.d("deep","${deeplink}")
        binding.progressBar.visibility=View.INVISIBLE
        binding.progressFetch.visibility=View.INVISIBLE
        binding.searhcbar.visibility=View.INVISIBLE

        if(deeplink !=null&&activity?.intent?.data!=null){
            binding.authGoogle.visibility=View.INVISIBLE
            binding.searhcbar.visibility=View.VISIBLE
            binding.progressBar.visibility=View.VISIBLE
            binding.progressFetch.visibility=View.VISIBLE
            ACCESS_TOKEN= deeplink.getQueryParameter("access_token")!!
            REFRESH_TOKEN=deeplink.getQueryParameter("refresh_token")!!
            activity?.intent?.data=null
            fetchEmails()
        }
        else{
            binding.rvMail.layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
            val adapter = Rv_mail_Adapter(datalist)
            binding.rvMail.adapter = adapter
        }

        binding.authGoogle.setOnClickListener {
            val intent = Intent(requireActivity(), Auth_Redirection::class.java)
            startActivity(intent)
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
                Log.d("opopop", "${url + MESSAGE_IDS[1]}")
                fetchMessagesSequentially(0) // Start fetching messages sequentially
            }
        })
    }

    private fun fetchMessagesSequentially(index: Int) {
        Log.d("yes","${index},${MESSAGE_IDS.size}")
        binding.progressBar.progress=index
        if (index >= MESSAGE_IDS.size) {
            // All messages fetched, update UI
            activity?.runOnUiThread {
                binding.rvMail.layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                val adapter = Rv_mail_Adapter(datalist)
                binding.rvMail.adapter = adapter
                binding.progressBar.visibility=View.INVISIBLE
                binding.progressFetch.visibility=View.INVISIBLE
            }
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
                    datalist.add(Rv_mail_model(R.drawable.person, title, date, subject))
                } catch (error: JSONException) {
                    Log.d("Catch", "$error")
                } finally {
                     //Proceed to fetch next message
                    fetchMessagesSequentially(index + 1)
                }
            }
        })
    }






}
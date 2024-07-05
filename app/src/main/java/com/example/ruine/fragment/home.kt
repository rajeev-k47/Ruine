package com.example.ruine.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.ruine.Adapters.RvmeetAdapter
import com.example.ruine.DatabaseHandler.CredDatabase
import com.example.ruine.DatabaseHandler.Maildata
import com.example.ruine.DatabaseHandler.meetData
import com.example.ruine.DatabaseHandler.meetDatabase
import com.example.ruine.R
import com.example.ruine.Rvmodels.Rvmeets
import com.example.ruine.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class home : Fragment() {
    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    val meetlist=ArrayList<Rvmeets>()
    lateinit var Creddatabase:CredDatabase
    lateinit var meetdatabase: meetDatabase
    lateinit var auth:FirebaseAuth
    var AccessToken=""
    val newMeetLink="https://meet.googleapis.com/v2/spaces"
    var Updating=false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchNewAccessToken()
        Creddatabase= Room.databaseBuilder(requireContext(), CredDatabase::class.java,"Cred").build()
        meetdatabase= Room.databaseBuilder(requireContext(),meetDatabase::class.java,"MeetList").build()
        auth=FirebaseAuth.getInstance()


        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                binding.meetLoad.visibility=View.VISIBLE

                meetdatabase.meetDao().getData().observe(this@home, Observer {
                        for (item in it.reversed()){
                            if(item.Uid==auth.currentUser?.uid&&!Updating){
                                Log.d("hallo","hallo")
                                meetlist.add(Rvmeets(item.meetSubject!!,item.meetTime!!,item.meetname!!,item.meetUri!!,item.meetCode!!))
                            }
                        }
                    binding.meetLoad.visibility=View.GONE
                    if(!Updating){
                    manageAdapter()}
                })
            }
        }


        binding.extendedFab.setOnClickListener {
//                    makeNewMeet(AccessToken)
            val add_view = layoutInflater.inflate(R.layout.add_new_meet, null)

            val DailogCreateGroup= MaterialAlertDialogBuilder(requireContext())
                .setView(add_view)
                .create()
            add_view.findViewById<Button>(R.id.meetcreatecancel).setOnClickListener{
                DailogCreateGroup.dismiss()
            }
            var time=""
            add_view.findViewById<Button>(R.id.choosetime).setOnClickListener {
                val picker =
                    MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select Appointment time")
                        .build()
                picker.show(childFragmentManager, "tag");

                picker.addOnPositiveButtonClickListener {
                    time="${picker.hour}:${picker.minute}"
                }
            }
            add_view.findViewById<Button>(R.id.meetcreate).setOnClickListener {
                val meetingsubject = add_view.findViewById<EditText>(R.id.meetSubject).text.toString()
                if(time.isNotEmpty()&&meetingsubject.isNotEmpty()){
                    makeNewMeet(AccessToken,meetingsubject,time)
                    Updating=true
                    DailogCreateGroup.dismiss()
                    binding.meetLoad.visibility=View.VISIBLE
                }
                else{
                    Toast.makeText(requireContext(),"All the Fields are Required",Toast.LENGTH_SHORT).show()
                }
            }
            DailogCreateGroup.show()



        }


    }
    private fun makeNewMeet(AccessToken:String,meetingSubject:String,time:String){
        if(AccessToken.isEmpty()){return}
        Log.d("hallo", AccessToken)
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            "{}"
        )

        val request = Request.Builder()
            .url(newMeetLink)
            .header("Authorization", "Bearer $AccessToken")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody!!)
                val name = json.getString("name")
                val meetUri = json.getString("meetingUri")
                val meetCode = json.getString("meetingCode")

                Updating=true
                activity?.runOnUiThread {
                    meetlist.add(0,Rvmeets(meetingSubject,time,name,meetUri,meetCode))
                    binding.rvmeet.adapter?.notifyDataSetChanged()
                    lifecycleScope.launch {
                        meetdatabase.meetDao().insertMail(
                            meetData(0, auth.currentUser?.uid, name, meetUri, meetCode, meetingSubject, time)
                        )
                    }
                    binding.meetLoad.visibility = View.INVISIBLE
                }

            }
        })
    }
    private fun fetchNewAccessToken(){
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Creddatabase.credDao().getData().observe(this@home, Observer {
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
                                 AccessToken = json.getString("access_token")
                            }
                        })

                    }
                })
            }
        }
    }
    private fun manageAdapter(){
        binding.rvmeet.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        val adapter = RvmeetAdapter(meetlist,requireContext(),object :RvmeetAdapter.meetDataBridge{
            override fun DataBridgeCarrier(meetCode: String) {
                Updating=true
                lifecycleScope.launch {
                    meetdatabase.meetDao().deleteByMeetId(meetCode)
                }
            }
        })
        binding.rvmeet.adapter = adapter
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }




}
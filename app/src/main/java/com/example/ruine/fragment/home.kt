package com.example.ruine.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.ruine.DatabaseHandler.meetData
import com.example.ruine.DatabaseHandler.meetDatabase
import com.example.ruine.R
import com.example.ruine.Rvmodels.RvMembersModel
import com.example.ruine.Rvmodels.Rvmeets
import com.example.ruine.Rvmodels.Rvmodel
import com.example.ruine.databinding.FragmentHomeBinding
import com.example.ruine.savedInstances
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class home : Fragment() {
    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    val meetlist=ArrayList<Rvmeets>()
    lateinit var Creddatabase:CredDatabase
    lateinit var meetdatabase: meetDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var groupValueEventListener: ValueEventListener
    private lateinit var memberEventListener: ValueEventListener
    lateinit var auth:FirebaseAuth
    private val suggestions = mutableListOf<String>()
    var AccessToken=""
    val newMeetLink="https://meet.googleapis.com/v2/spaces"
    var Updating=false

    var savedInstance = savedInstances(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchNewAccessToken()
        Creddatabase= Room.databaseBuilder(requireContext(), CredDatabase::class.java,"Cred").build()
        meetdatabase= Room.databaseBuilder(requireContext(),meetDatabase::class.java,"MeetListv1").build()
        auth=FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference


        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                binding.meetLoad.visibility=View.VISIBLE

                meetdatabase.meetDao().getData().observe(this@home, Observer {
                        for (item in it.reversed()){
                            if(item.Uid==auth.currentUser?.uid&&!Updating){
                                meetlist.add(Rvmeets(item.meetSubject!!,item.meetTime!!,item.meetDate!!,item.meetUri!!,item.meetGroup!!))
                            }
                        }
                    binding.meetLoad.visibility=View.GONE
                    if(!Updating){
                    manageAdapter()}
                })
            }
        }


        binding.extendedFab.setOnClickListener {
            savedInstance.newMeetPopUp=true
            ShowPopUp()
            if(suggestions.isEmpty()){
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
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
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        }
                        groupref.addValueEventListener(groupValueEventListener)
                    }
                }
            }
            }
        }


    }

    fun attendeesListToJson(attendees: MutableList<String>): String {
        val jsonArray = JSONArray()
        for (email in attendees) {
            val jsonObject = JSONObject()
            jsonObject.put("email", email)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
    private fun makeNewMeet(AccessToken:String,meetingSubject:String,time:String,date:String,Gname:String){
        if(AccessToken.isEmpty()){return}
        var dateTime=date+"T"+time
        val localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        val startDateTime = zonedDateTime.format(formatter)
        val endDateTime = zonedDateTime.plusHours(1).format(formatter)

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
                                val Gmembers= mutableListOf<String>()
                                for (member in membersSnapshot.children) {
                                    val memberName = member.getValue(RvMembersModel::class.java)
                                    memberName?.let { name ->
                                        Gmembers.add(name.MemberMail.toString())
                                    }
                                }
                                createMeet(Gmembers,startDateTime,endDateTime,meetingSubject,date,time,Gname)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
            membRef.addValueEventListener(memberEventListener)
        }


    }
    private fun createMeet(attendees: MutableList<String>,startDateTime:String,endDateTime:String,meetingSubject: String,date: String,time: String,meetGroup: String){

        val client = OkHttpClient()
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val eventJson = """
        {
            "summary": "$meetingSubject",
            "location": "Virtual",
            "description": "Group meet",
            "start": {
                "dateTime": "$startDateTime",
                "timeZone": "${ZoneId.systemDefault()}"
            },
            "end": {
                "dateTime": "$endDateTime",
                "timeZone": "${ZoneId.systemDefault()}"
            },
            "attendees": ${attendeesListToJson(attendees)},
            "conferenceData": {
                "createRequest": {
                    "conferenceSolutionKey": {
                        "type": "hangoutsMeet"
                    },
                    "requestId": "ViaRuine"
                }
            }
        }
    """.trimIndent()
        val requestBody = eventJson.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1&sendUpdates=all")
            .addHeader("Authorization", "Bearer $AccessToken")
            .post(requestBody)
            .build()
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val json = JSONObject(responseBody!!)
                        val meetUri= json.getString("hangoutLink")
                        saveMeetLocal(meetingSubject,time,date,meetUri,meetGroup)
                    } else {
                       Toast.makeText(requireContext(),"Something Went Wrong!!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun saveMeetLocal(meetingSubject: String,time: String,date: String,meetUri: String,meetGroup:String){
        println("entered")
        activity?.runOnUiThread {
            meetlist.add(0, Rvmeets(meetingSubject,time,date,meetUri,meetGroup))
            binding.rvmeet.adapter?.notifyDataSetChanged()
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                meetdatabase.meetDao().insertMail(
                    meetData(0,auth.currentUser?.uid,date,meetUri,meetGroup,meetingSubject,time)
                )
            }
        }
        binding.meetLoad.visibility=View.GONE
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
            override fun DataBridgeCarrier(meetUri: String) {
                Updating=true
                lifecycleScope.launch {
                    meetdatabase.meetDao().deleteByMeetId(meetUri)
                }
            }
        })
        binding.rvmeet.adapter = adapter
    }

    @SuppressLint("MissingInflatedId")
    private fun ShowPopUp(){
        val add_view = layoutInflater.inflate(R.layout.add_new_meet, null)

        val DailogCreateGroup= MaterialAlertDialogBuilder(requireContext())
            .setView(add_view)
            .create()
        add_view.findViewById<Button>(R.id.meetcreatecancel).setOnClickListener {
            DailogCreateGroup.dismiss()
        }
        var time=""
        var date=""
        add_view.findViewById<Button>(R.id.choosetime).setOnClickListener {
            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select Meeting time")
                    .build()
            picker.show(childFragmentManager, "tag");

            picker.addOnPositiveButtonClickListener {
                time="${picker.hour}:${picker.minute}"
            }
        }
        val autoCompleteTextView = add_view.findViewById<MaterialAutoCompleteTextView>(R.id.meetGroup)
        autoCompleteTextView.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        )

        add_view.findViewById<Button>(R.id.chooseDate).setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()
            datePicker.show(childFragmentManager, "tag");
            datePicker.addOnPositiveButtonClickListener {selection->
                val rawdate = Date(selection)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                date = format.format(rawdate)
            }

        }
        add_view.findViewById<Button>(R.id.meetcreate).setOnClickListener {
            val meetingsubject = add_view.findViewById<EditText>(R.id.meetSubject).text.toString()
            val meetingGroup = autoCompleteTextView.text
            if(time.isNotEmpty()&&meetingsubject.isNotEmpty()&&date.isNotEmpty()&&verifyGroup(meetingGroup.toString())){
                makeNewMeet(AccessToken,meetingsubject,time,date,meetingGroup.toString())
                Updating=true
                DailogCreateGroup.dismiss()
                binding.meetLoad.visibility=View.VISIBLE
            }
            else{
                Toast.makeText(requireContext(),"All the Fields are Required",Toast.LENGTH_SHORT).show()
            }
        }
        DailogCreateGroup.show()

        DailogCreateGroup.setOnDismissListener {
            savedInstance.newMeetPopUp=false
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




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("meetPopUp",savedInstance.newMeetPopUp)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState!=null){
            if (savedInstanceState.getBoolean("meetPopUp")==true){
                ShowPopUp()
            }
        }

    }




}
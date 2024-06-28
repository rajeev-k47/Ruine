package com.example.ruine

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ruine.databinding.ActivityViewGroupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewGroup : AppCompatActivity() {
    val binding:ActivityViewGroupBinding by lazy {
        ActivityViewGroupBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private var MemberList=ArrayList<RvMembersModel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var memberEventListener: ValueEventListener
    private lateinit var grp_Key:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Finish the activity when the back button is pressed
                finish()
            }
        })
        binding.back.setOnClickListener { finish() }
        val grp_Name = intent.getStringExtra("GrpName")
        grp_Key = intent.getStringExtra("GrpKey")!!

        binding.GrpNameTitle.setText(grp_Name?:"None")

        binding.AddPerson.setOnClickListener {
            MemberList.add(0,RvMembersModel(R.drawable.person,"","",true))
            manageAdapter()
        }
        auth.currentUser?.let { user->
            val memberReference=databaseReference.child("users").child(user.uid).child("groupmail").child(grp_Key).child("members")
            memberEventListener=object :ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    MemberList.clear()

                    for(members in snapshot.children){
                        val member = members.getValue(RvMembersModel::class.java)
                        member?.let {
                            MemberList.add(it)
                        }
                    }
                    MemberList.reverse()
                    binding.LoadMembers.visibility=View.GONE
                    manageAdapter()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }
            memberReference.addValueEventListener(memberEventListener)
        }



    }
    private fun manageAdapter(){
        binding.Rvmembers.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        val Adapter = RvMembersAdapter(MemberList,object :RvMembersAdapter.DataBridge{
            override fun DataBridgeCarrier(MemberName: String, MemberMail: String) {
                if(MemberMail.isNotEmpty()&&MemberName.isNotEmpty()){
                    if(isGmailAddress(MemberMail)){
                    AddMemberToDataBase(MemberName,MemberMail)}
                    else{
                        Toast.makeText(this@ViewGroup,"Please Enter Valid Email Address",Toast.LENGTH_SHORT).show()

                    }
                }
                else{
                    Toast.makeText(this@ViewGroup,"Please Enter All the Fields Correctly",Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.Rvmembers.adapter=Adapter
    }
    private fun AddMemberToDataBase(memberName:String,memberMail:String){
                auth.currentUser?.let { user->
            val memberKey = databaseReference.child("users").child(user.uid).child("groupmail").child(grp_Key).child("members").push().key!!
            val data =RvMembersModel(R.drawable.person,memberName,memberMail,false)
            databaseReference.child("users").child(user.uid).child("groupmail")
                .child(grp_Key).child("members").child(memberKey).setValue(data)
                .addOnCompleteListener { task->
                    if(task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Updated Successfully!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        Toast.makeText(
                        this,
                        "Something Wrong Occured!!",
                        Toast.LENGTH_SHORT
                    ).show()}
                }

        }
    }
    fun isGmailAddress(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$")
        return regex.matches(email)
    }





}
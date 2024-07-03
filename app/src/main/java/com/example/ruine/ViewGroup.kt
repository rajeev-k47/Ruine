package com.example.ruine

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ruine.Adapters.RvMembersAdapter
import com.example.ruine.Rvmodels.RvMembersModel
import com.example.ruine.databinding.ActivityViewGroupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

class ViewGroup : AppCompatActivity() {
    val binding:ActivityViewGroupBinding by lazy {
        ActivityViewGroupBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private var MemberList=ArrayList<RvMembersModel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var memberEventListener: ValueEventListener
    private lateinit var grp_Key:String

    private val PICK_FILE_REQUEST_CODE = 1


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
        binding.importMembers.setOnClickListener{
            importMembersFromExcel()
        }
        val grp_Name = intent.getStringExtra("GrpName")
        grp_Key = intent.getStringExtra("GrpKey")!!

        binding.GrpNameTitle.setText(grp_Name?:"None")

        binding.AddPerson.setOnClickListener {
            MemberList.add(0, RvMembersModel(R.drawable.person,"","",true))
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
        val Adapter = RvMembersAdapter(MemberList,object : RvMembersAdapter.DataBridge{
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
            val data = RvMembersModel(R.drawable.person,memberName,memberMail,false)
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
        val regex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return regex.matches(email)
    }
    private fun importMembersFromExcel(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // for .xlsx files
        }
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)

}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.LoadMembers.visibility=View.VISIBLE

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                readExcelFile(uri)
            }
        }
    }

    private fun readExcelFile(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                parseExcelFile(inputStream)
            }
        }
    }
    private suspend fun parseExcelFile(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            try {
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)
                for (row in sheet) {
                    if(isGmailAddress(row.getCell(1).toString())){
                        MemberList.add(RvMembersModel(R.drawable.person,row.getCell(0).toString(),row.getCell(1).toString()))
                        AddMemberToDataBase(row.getCell(0).toString(),row.getCell(1).toString())
                    }
                }
                workbook.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        withContext(Dispatchers.Main) {
            binding.LoadMembers.visibility=View.GONE
            manageAdapter()
        }
    }




}
package com.example.ruine.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ruine.R
import com.example.ruine.Adapters.Rvadapter
import com.example.ruine.Rvmodels.Rvmodel
import com.example.ruine.databinding.FragmentGroupsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Groups : Fragment() {
    private val binding: FragmentGroupsBinding by lazy {
        FragmentGroupsBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private var datalist = ArrayList<Rvmodel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var groupValueEventListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference

        auth = FirebaseAuth.getInstance()


//========================================================================================//

        binding.add.setOnClickListener {
            val add_view = layoutInflater.inflate(R.layout.add_mail_reference, null)
            val add_submit = add_view.findViewById<Button>(R.id.create)
            val add_cancel = add_view.findViewById<Button>(R.id.add_cancel)

            add_view.findViewById<EditText>(R.id.grp_name).setText("")
            add_view.findViewById<EditText>(R.id.grp_tag_mail).setText("")
            val DailogCreateGroup= MaterialAlertDialogBuilder(requireContext())
                .setView(add_view)
                .create()
            add_cancel.setOnClickListener {
                DailogCreateGroup.dismiss()
            }
            add_submit.setOnClickListener {addGroup(add_view,DailogCreateGroup)}

            DailogCreateGroup.show()

        }




        //=================================================================================//
        val currentuser = auth.currentUser
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                currentuser?.let { user ->
                    val groupref = databaseReference.child("users").child(user.uid).child("groupmail")
                    groupValueEventListener=object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
//                        val grp_list_from_database = mutableListOf<Rvmodel>()
                            datalist.clear()
                            for (groups in snapshot.children) {
                                val grps_from_database = groups.getValue(Rvmodel::class.java)
                                grps_from_database?.let {
                                    datalist.add(it)
                                }
                            }
                            binding.rv.layoutManager =
                                LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                            datalist.reverse()
                            val adapter =
                                Rvadapter(requireContext(),datalist, object : Rvadapter.OptionsMenuClickListener {
                                    override fun onOptionsMenuClicked(
                                        position: Int,
                                        grp_key: String,
                                        GRP_NAME: String,
                                        GRP_MAILTAG: String
                                    ) {
                                        Menuclick(position, grp_key, GRP_NAME, GRP_MAILTAG)
                                    }
                                })
                            binding.rv.adapter = adapter
                            binding.LoadGroups.visibility=View.GONE
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Snackbar.make(requireView(), "Data Fetching Failed!!", Snackbar.LENGTH_SHORT)
                                .setAction("Ok"){}
                                .setBackgroundTint(resources.getColor(R.color.brown))
                                .setTextColor(Color.WHITE)
                                .show()
//                    Toast.makeText(requireContext(),"Data Fetching Failed!!" ,Toast.LENGTH_SHORT).show()
                        }

                    }
                    groupref.addValueEventListener(groupValueEventListener)
                }


            }
        }

    }

    private fun Menuclick(position: Int, grp_key: String, GRP_NAME: String, GRP_MAILTAG: String) {
        val PopupMenu = PopupMenu(requireContext(), binding.rv[position].findViewById(R.id.menu))
        PopupMenu.inflate(R.menu.edit_menu)
        PopupMenu.setOnMenuItemClickListener(object : OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                val currentuser = auth.currentUser
                when (item?.itemId) {
                    R.id.Delete -> {
                        currentuser?.let { user ->
                            databaseReference.child("users").child(user.uid).child("groupmail")
                                .child(grp_key).removeValue()
                        }
                    }

                    R.id.Edit -> {
                        val update_add_view = layoutInflater.inflate(R.layout.update_grp, null)
                        val DailogEditGroup= MaterialAlertDialogBuilder(requireContext())
                            .setView(update_add_view)
                            .create()

                        DailogEditGroup.show()

                        if(DailogEditGroup.isShowing){
                            val update_name = update_add_view.findViewById<EditText>(R.id.update_grp_name)
                            val update_mail_tag =update_add_view.findViewById<EditText>(R.id.update_grp_tag_mail)
                            val update_cancel =update_add_view.findViewById<Button>(R.id.update_cancel)
                            val update_update =update_add_view.findViewById<Button>(R.id.update)
                            update_name.setText(GRP_NAME)
                            update_mail_tag.setText(GRP_MAILTAG)

                            update_update.setOnClickListener {
                                currentuser?.let { user ->
                                    val NewDataList = Rvmodel(R.drawable.group,update_name.text.toString(),update_mail_tag.text.toString(),grp_key
                                    )
                                    //It will upload a empty string again
                                    databaseReference.child("users").child(user.uid).child("groupmail")
                                        .child(grp_key).setValue(NewDataList)
                                        .addOnCompleteListener { task->
                                            if(task.isSuccessful) {
                                                Snackbar.make(requireView(), "Updated Successfully!!", Snackbar.LENGTH_SHORT)
                                                    .setAction("Ok"){}
                                                    .setBackgroundTint(resources.getColor(R.color.SnackBar))
                                                    .setTextColor(Color.WHITE)
                                                    .show()
//                                                Toast.makeText(
//                                                    requireContext(),
//                                                    "Updated Successfully!!",
//                                                    Toast.LENGTH_SHORT
//                                                ).show()
                                                DailogEditGroup.dismiss()
                                            }
                                            else{
                                                Snackbar.make(requireView(), "Something Wrong Occured!!", Snackbar.LENGTH_SHORT)
                                                    .setAction("Ok"){}
                                                    .setBackgroundTint(resources.getColor(R.color.SnackBar))
                                                    .setTextColor(Color.WHITE)
                                                    .show()

//                                                Toast.makeText(
//                                                requireContext(),
//                                                "Something Wrong Occured!!",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
                                                ;DailogEditGroup.dismiss()}
                                        }
                                }
                            }
                            update_cancel.setOnClickListener {
                                DailogEditGroup.dismiss()
                            }



                        }


                    }
                }
                return false
            }
        })

        PopupMenu.show()


    }
    private fun addGroup(add_view: View,Dialog:AlertDialog){
        val add_group_name = add_view.findViewById<EditText>(R.id.grp_name).text.toString()
        val add_group_tag_mail = add_view.findViewById<EditText>(R.id.grp_tag_mail).text.toString()

        if (add_group_name.isEmpty() || add_group_tag_mail.isEmpty()) {
            Toast.makeText(requireContext(), "All the Fields are Required ! ", Toast.LENGTH_SHORT).show()
        } else {
            val currentuser = auth.currentUser
            currentuser?.let { user ->
                //Generate a unique key for the note
                val GroupKey = databaseReference.child("users").child(user.uid).child("groupmail").push().key

                val grp_Item = Rvmodel(R.drawable.group, add_group_name, add_group_tag_mail, GroupKey)

                if (GroupKey != null) {
                    databaseReference.child("users").child(user.uid).child("groupmail")
                        .child(GroupKey).setValue(grp_Item)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Snackbar.make(requireView(), "Group Created Successfully !", Snackbar.LENGTH_SHORT)
                                    .setAction("Ok"){}
                                    .setBackgroundTint(resources.getColor(R.color.SnackBar))
                                    .setTextColor(Color.WHITE)
                                    .show()
//                                Toast.makeText(requireContext(), "Group Created Successfully !", Toast.LENGTH_SHORT).show()
                                if (Dialog.isShowing) {
                                    Dialog.dismiss()
                                }
                            } else {
//                                Toast.makeText(requireContext(), "Something Went Wrong !", Toast.LENGTH_SHORT).show()
                                Snackbar.make(requireView(), "Something Went Wrong !", Snackbar.LENGTH_SHORT)
                                    .setAction("Ok"){}
                                    .setBackgroundTint(resources.getColor(R.color.SnackBar))
                                    .setTextColor(Color.WHITE)
                                    .show()
                            }

                        }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val currentuser = auth.currentUser
        currentuser?.let { user ->
            val groupref = databaseReference.child("users").child(user.uid).child("groupmail")
            groupref.removeEventListener(groupValueEventListener)
        }
    }


}

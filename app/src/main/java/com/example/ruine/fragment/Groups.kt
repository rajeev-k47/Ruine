package com.example.ruine.fragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ruine.R
import com.example.ruine.Rvadapter
import com.example.ruine.Rvmodel
import com.example.ruine.databinding.FragmentGroupsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Groups : Fragment() {
    var group_num = 1
    private val binding: FragmentGroupsBinding by lazy {
        FragmentGroupsBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var rvAdapter: Rvadapter
    private var datalist = ArrayList<Rvmodel>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var add_popupwindow: PopupWindow
    private lateinit var update_add_popupwindow: PopupWindow


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseReference = FirebaseDatabase.getInstance().reference

        auth = FirebaseAuth.getInstance()

        val add_view = layoutInflater.inflate(R.layout.add_mail_reference, null)
        val add_submit = add_view.findViewById<Button>(R.id.create)
        val add_cancel = add_view.findViewById<Button>(R.id.add_cancel)
//========================================================================================//
        add_popupwindow = PopupWindow(add_view, 700, 700)

        binding.add.setOnClickListener {
            add_popupwindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
            add_popupwindow.isFocusable = true
            add_popupwindow.update()

        }

        add_submit.setOnClickListener {
            val add_group_name = add_view.findViewById<EditText>(R.id.grp_name).text.toString()
            val add_group_tag_mail =
                add_view.findViewById<EditText>(R.id.grp_tag_mail).text.toString()

            if (add_group_name.isEmpty() || add_group_tag_mail.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "All the Fields are Required ! ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val currentuser = auth.currentUser
                currentuser?.let { user ->
                    //Generate a unique key for the note
                    val GroupKey =
                        databaseReference.child("users").child(user.uid).child("groupmail")
                            .push().key

                    val grp_Item =
                        Rvmodel(R.drawable.group, add_group_name, add_group_tag_mail, GroupKey)
                    if (GroupKey != null) {
                        databaseReference.child("users").child(user.uid).child("groupmail")
                            .child(GroupKey).setValue(grp_Item)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Group Created Successfully !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (add_popupwindow.isShowing) {
                                        add_popupwindow.dismiss()
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Something Went Wrong !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }
                    }
                }
            }
        }
        add_cancel.setOnClickListener {
            if (add_popupwindow.isShowing) {
                add_popupwindow.dismiss()
            }
        }
        //=================================================================================//
        val currentuser = auth.currentUser
        currentuser?.let { user ->
            val groupref = databaseReference.child("users").child(user.uid).child("groupmail")
            groupref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                        val grp_list_from_database = mutableListOf<Rvmodel>()
                    datalist.clear()
                    for (groups in snapshot.children) {
                        val grps_from_database = groups.getValue(Rvmodel::class.java)
                        grps_from_database?.let {
                            datalist.add(it)
                        }
                    }
//                        Toast.makeText(requireContext(),"${datalist}",Toast.LENGTH_SHORT).show()
                    binding.rv.layoutManager =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    datalist.reverse()
                    var adapter =
                        Rvadapter(datalist, object : Rvadapter.OptionsMenuClickListener {
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
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
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
                        update_add_popupwindow = PopupWindow(update_add_view, 700, 700)

                        update_add_popupwindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
                        update_add_popupwindow.isFocusable = true
                        update_add_popupwindow.update()

                        if(update_add_popupwindow.isShowing){
                            val update_name = update_add_view.findViewById<EditText>(R.id.update_grp_name)
                            val update_mail_tag =update_add_view.findViewById<EditText>(R.id.update_grp_tag_mail)
                            val update_cancel =update_add_view.findViewById<Button>(R.id.update_cancel)
                            val update_update =update_add_view.findViewById<Button>(R.id.update)
                            update_name.setText(GRP_NAME)
                            update_mail_tag.setText(GRP_MAILTAG)

                            update_update.setOnClickListener {
                                currentuser?.let { user ->

                                    val NewDataList = Rvmodel(R.drawable.group,update_name.text.toString(),update_mail_tag.text.toString(),grp_key)
                                    databaseReference.child("users").child(user.uid).child("groupmail")
                                        .child(grp_key).setValue(NewDataList)
                                        .addOnCompleteListener { task->
                                            if(task.isSuccessful) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Updated Successfully!!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                update_add_popupwindow.dismiss()
                                            }
                                            else{Toast.makeText(
                                                requireContext(),
                                                "Something Wrong Occured!!",
                                                Toast.LENGTH_SHORT
                                            ).show();update_add_popupwindow.dismiss()}
                                        }
                                }
                            }
                            update_cancel.setOnClickListener {
                                update_add_popupwindow.dismiss()
                            }



                        }


                    }
                }
                return false
            }
        })

        PopupMenu.show()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (add_popupwindow.isShowing) {
            add_popupwindow.dismiss()
        }
    }


}

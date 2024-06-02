package com.example.ruine.fragment

import android.annotation.SuppressLint
import android.icu.text.Transliterator.Position
import android.os.Bundle
import android.util.Log
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
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.view.get
import androidx.lifecycle.findViewTreeViewModelStoreOwner
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

                    val grp_Item = Rvmodel(R.drawable.group, add_group_name, add_group_tag_mail)
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

                            Log.d("abcd","${groups}")
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
                        var adapter =
                            Rvadapter(datalist, object : Rvadapter.OptionsMenuClickListener {
                                override fun onOptionsMenuClicked(position: Int) {
                                    Menuclick(position)
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

    private fun Menuclick(position: Int) {
        val PopupMenu = PopupMenu(requireContext(), binding.rv[position].findViewById(R.id.menu))
        PopupMenu.inflate(R.menu.edit_menu)
        PopupMenu.setOnMenuItemClickListener(object : OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.Delete -> {

                    }

                    R.id.Edit -> {

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

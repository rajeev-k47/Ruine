package com.example.ruine.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.Rvmodels.RvMembersModel
import com.example.ruine.databinding.DemoMembersLayoutBinding

class RvMembersAdapter(var Memberlist: ArrayList<RvMembersModel>, val DataCarrier: DataBridge):RecyclerView.Adapter<RvMembersAdapter.MyviewHolder>() {
    interface DataBridge {
        fun DataBridgeCarrier(MemberName:String,MemberMail:String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
       val binding = DemoMembersLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyviewHolder(binding)
    }

    override fun getItemCount(): Int {
      return Memberlist.size
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        holder.binding.MemberName.setText(Memberlist[position].MemberName)
        holder.binding.MemberMail.setText(Memberlist[position].MemberMail)
        if(!Memberlist[position].Editable!!){
            holder.binding.MemberName.isFocusable=false
            holder.binding.MemberMail.isFocusable=false
            holder.binding.addMember.visibility=View.GONE
            holder.binding.addMemberCancel.visibility=View.GONE
        }else{
            holder.binding.addMember.setOnClickListener {
            DataCarrier.DataBridgeCarrier(holder.binding.MemberName.text.toString(),holder.binding.MemberMail.text.toString())
            }
            holder.binding.addMemberCancel.setOnClickListener {
                removeMemberAt(position)
            }
        }
    }
    inner class MyviewHolder(var binding: DemoMembersLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun removeMemberAt(position: Int) {
        if (position >= 0 && position < Memberlist.size) {
            Memberlist.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, Memberlist.size)
        }
    }
}
package com.example.ruine.Adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.Rvmodels.Rvmeets
import com.example.ruine.databinding.DemoMailLayoutBinding
import com.example.ruine.databinding.DemoMeetLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RvmeetAdapter(var meetlist:ArrayList<Rvmeets>,var context: Context,val DataCarrier: meetDataBridge): RecyclerView.Adapter<RvmeetAdapter.MyviewHolder>() {
    interface meetDataBridge {
        fun DataBridgeCarrier(meetCode:String)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RvmeetAdapter.MyviewHolder {
        val binding=DemoMeetLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyviewHolder(binding)

    }

    override fun getItemCount(): Int {
        return meetlist.size
    }

    override fun onBindViewHolder(holder: RvmeetAdapter.MyviewHolder, position: Int) {
        holder.binding.meetsubject.text=meetlist[position].Subject
        holder.binding.meettime.text=meetlist[position].Time
        holder.binding.joinmeet.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meetlist[position].meetUri))
            context.startActivity(intent)
        }
        holder.binding.sharemeet.setOnClickListener {
            val intent = Intent().apply {action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, meetlist[position].meetUri)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, "Share link using")
            context.startActivity(shareIntent)


        }
        holder.binding.meetDelete.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle("Confirm")
                .setMessage("Do you want to Delete \"${ meetlist[position].Subject}\" Meet ?")
                .setNegativeButton("Cancel") { dialog, which ->

                }
                .setPositiveButton("Confirm") { dialog, which ->
                    DataCarrier.DataBridgeCarrier(meetlist[position].meetingCode)
                    removeMemberAt(position)
                }
                .show()

        }
    }

    inner class MyviewHolder(var binding: DemoMeetLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun removeMemberAt(position: Int) {
        if (position >= 0 && position < meetlist.size) {
            meetlist.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, meetlist.size)
        }
    }
}
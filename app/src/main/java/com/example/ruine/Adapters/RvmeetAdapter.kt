package com.example.ruine.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.Rvmodels.Rvmeets
import com.example.ruine.databinding.DemoMeetLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RvmeetAdapter.MyviewHolder, position: Int) {
        holder.binding.meetsubject.text=meetlist[position].Subject
        holder.binding.meettime.text=meetlist[position].meetDate +" at "+meetlist[position].Time
        holder.binding.meetGroup.text="Group : ${meetlist[position].meetGroup}"
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
                    DataCarrier.DataBridgeCarrier(meetlist[position].meetUri)
                    removeMemberAt(position)
                }
                .show()

        }
        holder.binding.Remind.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val localTime = LocalTime.parse(meetlist[position].Time, formatter)
            val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, localTime.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, localTime.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, meetlist[position].Subject)
            }
            context.startActivity(alarmIntent)
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
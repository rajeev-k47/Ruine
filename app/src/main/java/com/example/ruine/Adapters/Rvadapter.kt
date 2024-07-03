package com.example.ruine.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.R
import com.example.ruine.Rvmodels.Rvmodel
import com.example.ruine.databinding.DemoGroupsLayoutBinding
import com.shashank.sony.fancytoastlib.FancyToast

class Rvadapter(
    var context: Context,
    var datalist: ArrayList<Rvmodel>,
    var optionsMenuClickListener: OptionsMenuClickListener
) : RecyclerView.Adapter<Rvadapter.MyviewHolder>() {

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(position: Int,grp_key:String,GRP_NAME:String,GRP_MAILTAG:String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
        var binding =
            DemoGroupsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyviewHolder(binding)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        anim(holder.itemView)
//        holder.binding.profile.setImageResource(datalist.get(position).profile)
        holder.binding.name.text = datalist[position].name
        holder.binding.gTag.text = datalist[position].mail_tag
        val grp_key = datalist[position].grp_id
        holder.binding.menu.setOnClickListener {
            if (grp_key != null) {
                optionsMenuClickListener.onOptionsMenuClicked(position,grp_key,
                    datalist[position].name.toString(), datalist[position].mail_tag.toString())
            }
        }
        holder.binding.group.setOnClickListener{
            val intent =Intent(context, com.example.ruine.ViewGroup::class.java)
            intent.putExtra("GrpName","${holder.binding.name.text}")
            intent.putExtra("GrpKey","${grp_key}")
            context.startActivity(intent)
            FancyToast.makeText(context, "$position", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS,
                R.drawable.tick, false).show();
        }
    }

    inner class MyviewHolder(var binding: DemoGroupsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun anim(view: View) {
        val animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 100
        view.startAnimation(animation)
    }
}
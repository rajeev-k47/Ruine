package com.example.ruine

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.databinding.DemoGroupsLayoutBinding

class Rvadapter(
    var datalist: ArrayList<Rvmodel>,
    var optionsMenuClickListener: OptionsMenuClickListener
) : RecyclerView.Adapter<Rvadapter.MyviewHolder>() {

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(position: Int)
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
        holder.binding.name.text = datalist.get(position).name
        holder.binding.gTag.text = datalist.get(position).mail_tag
        holder.binding.menu.setOnClickListener {
            optionsMenuClickListener.onOptionsMenuClicked(position)
        }
    }

    inner class MyviewHolder(var binding: DemoGroupsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun anim(View: View) {
        var animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 100
        View.startAnimation(animation)
    }
}
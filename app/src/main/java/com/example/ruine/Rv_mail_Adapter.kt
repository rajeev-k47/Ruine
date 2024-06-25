package com.example.ruine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.databinding.DemoMailLayoutBinding

class Rv_mail_Adapter(var dataList: ArrayList<Rv_mail_model>) :RecyclerView.Adapter<Rv_mail_Adapter.MyviewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Rv_mail_Adapter.MyviewHolder {
        var binding =
            DemoMailLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyviewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
//        anim(holder.itemView)

        holder.binding.mailProfile.setImageResource(R.drawable.person)
        holder.binding.mailTitle.text= dataList[position].mail_title
        holder.binding.mailDate.text=dataList[position].mail_date
        holder.binding.mailSnippet.text=dataList[position].mail_snippet
        if(dataList[position].NewTag==true){
            holder.binding.New.visibility=View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }



    inner class MyviewHolder(var binding: DemoMailLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun anim(View: View) {
        var animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 100
        View.startAnimation(animation)
    }
}
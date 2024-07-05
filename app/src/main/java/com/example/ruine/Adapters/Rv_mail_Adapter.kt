package com.example.ruine.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ruine.R
import com.example.ruine.Rvmodels.Rv_mail_model
import com.example.ruine.ViewMail
import com.example.ruine.databinding.DemoMailLayoutBinding

class Rv_mail_Adapter(var dataList: ArrayList<Rv_mail_model>,val context: Context) :RecyclerView.Adapter<Rv_mail_Adapter.MyviewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
        var binding =
            DemoMailLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyviewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
//        anim(holder.itemView)
        holder.binding.mailTitle.text= dataList[position].mail_title
        holder.binding.mailDate.text=dataList[position].mail_date
        holder.binding.mailSnippet.text=dataList[position].mail_snippet
        if(dataList[position].NewTag==true){
            holder.binding.New.visibility=View.VISIBLE
        }else{holder.binding.New.visibility=View.INVISIBLE}
        holder.binding.mailLayout.setOnClickListener {
            val intent=Intent(context,ViewMail::class.java)
            intent.putExtra("messageID", dataList[position].messageId)
            context.startActivity(intent)
        }
        val profileLetter = getProfileLetter(dataList[position].mail_title)
        val profileBitmap = createProfileBitmap(profileLetter)
        holder.binding.mailProfile.setImageBitmap(profileBitmap)

    }
    private fun getProfileLetter(email: String?): String {
        return if (!email.isNullOrEmpty()) {
            email.first().uppercaseChar().toString()
        } else {
            "U"
        }
    }
    @SuppressLint("ResourceAsColor")
    private fun createProfileBitmap(letter: String): Bitmap {
        val bitmap = Bitmap.createBitmap(150,150, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintBackground = Paint()
        paintBackground.color = ContextCompat.getColor(context, R.color.gery)
        canvas.drawCircle(75F, 75F, 80F, paintBackground)

        val paintText = Paint()
        paintText.color = R.color.black
        paintText.textSize = 100f
        paintText.isAntiAlias = true
        paintText.textAlign = Paint.Align.CENTER

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2)
        canvas.drawText(letter, xPos.toFloat(), yPos, paintText)

        return bitmap
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
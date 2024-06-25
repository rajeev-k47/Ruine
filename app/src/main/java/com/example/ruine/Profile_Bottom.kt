package com.example.ruine

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.ruine.databinding.FragmentMailsBinding
import com.example.ruine.databinding.FragmentProfileBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.glide.transformations.CropCircleTransformation

class Profile_Bottom : BottomSheetDialogFragment (){
    private val binding: FragmentProfileBottomBinding by lazy {
        FragmentProfileBottomBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.Logout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), Login::class.java))
                activity?.finish()
        }

        val ImageUri = FirebaseAuth.getInstance().currentUser?.photoUrl
        val Email = FirebaseAuth.getInstance().currentUser?.email
        val Uname = FirebaseAuth.getInstance().currentUser?.displayName

        val Profile=binding.bottomProfile
        val Name = binding.bottomName
        val Mail = binding.bottomEmail

        Name.text = Uname?:"User"
        Mail.text = Email?:"User Email"

        if(ImageUri!=null){
            ImageUri.let {
                Glide.with(this)
                    .load(it)
                    .transform(CropCircleTransformation())
                    .into(Profile)
            }
        }else{
            val profileLetter = getProfileLetter(Email)
            val profileBitmap = createProfileBitmap(profileLetter)
            Profile.setImageBitmap(profileBitmap)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
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
        val bitmap = Bitmap.createBitmap(200,200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintBackground = Paint()
        paintBackground.color = ContextCompat.getColor(requireContext(), R.color.gery)
        canvas.drawCircle(100F, 100F, 100F, paintBackground)

        val paintText = Paint()
        paintText.color = R.color.black
        paintText.textSize = 150f
        paintText.isAntiAlias = true
        paintText.textAlign = Paint.Align.CENTER

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2)
        canvas.drawText(letter, xPos.toFloat(), yPos, paintText)

        return bitmap
    }




}
package com.example.ruine.AuthnSetup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.room.Room
import com.example.ruine.DatabaseHandler.CredDatabase
import com.example.ruine.MainActivity
import com.example.ruine.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

class Splash_screen : AppCompatActivity() {
  private val binding: ActivitySplashScreenBinding by lazy {
    ActivitySplashScreenBinding.inflate(layoutInflater)
  }

  private lateinit var auth: FirebaseAuth
  lateinit var Creddatabase: CredDatabase
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)

    auth = FirebaseAuth.getInstance()
    Creddatabase =
            Room.databaseBuilder(this@Splash_screen, CredDatabase::class.java, "Cred").build()
    Creddatabase.credDao()
            .getData()
            .observe(
                    this@Splash_screen,
                    Observer {
                      for (item in it) {

                        if (item.uid == auth.currentUser?.uid) {
                          startActivity(Intent(this@Splash_screen, MainActivity::class.java))
                          return@Observer
                        }
                      }

                      val intent = Intent(this@Splash_screen, Login::class.java)
                      startActivity(intent)
                      finish()
                      return@Observer
                    }
            )

    // Handler(Looper.getMainLooper()).postDelayed({
    //  startActivity(Intent(this, Login::class.java))
    // finish()
    //  }, 200)
  }
}

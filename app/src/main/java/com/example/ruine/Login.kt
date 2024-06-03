package com.example.ruine

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ruine.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSigninClient: GoogleSignInClient

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Successful !!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed!!", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onStart() {
        super.onStart()

        val currentuser: FirebaseUser? = auth.currentUser
        if (currentuser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor=ContextCompat.getColor(this,R.color.black)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("460948780073-nicjd08qrsldo1a4n4is9btlepsv3ak0.apps.googleusercontent.com")
            .requestEmail().build()
        googleSigninClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()
        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val loginemail = binding.username.text.toString()
            val loginpassword = binding.password.text.toString()

            if (loginemail.isEmpty() || loginpassword.isEmpty()) {
                Toast.makeText(this, "Something is missing !!", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(loginemail, loginpassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful !!", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Wrong Credentials !!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        binding.google.setOnClickListener {
            val signinclient = googleSigninClient.signInIntent
            launcher.launch(signinclient)
        }


    }
}
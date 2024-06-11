package com.example.ruine

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.ruine.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.core.Tag
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.*


class Login : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth


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
            val credentialManager=CredentialManager.create(this)

            val rawNonce = UUID.randomUUID().toString()
            val bytes=rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest=md.digest(bytes)
            val hashedNonce = digest.fold(""){str,it->str+"%02x".format(it)}

//            Log.d("ew", "${bytes},${md},${digest},${hashedNonce}")

            val googleIdOption: GetGoogleIdOption =GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("448403106695-qmmbjaj8f5ns5r3kg45asne0cg3g0ngt.apps.googleusercontent.com")
                .setNonce(hashedNonce)
                .build()
            val request:GetCredentialRequest=GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch{
                try {
                    val result =
                        credentialManager.getCredential(request = request, context = this@Login)
                    val credential = result.credential
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken
                    Log.d("TAG", googleIdToken)

                    when {
                        googleIdToken!=null->{
                            val fireBaseCredential=GoogleAuthProvider.getCredential(googleIdToken,null)
//                            auth=Firebase.auth
                            auth.signInWithCredential(fireBaseCredential)
                                .addOnCompleteListener(this@Login) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@Login, "Login Successful !!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@Login, MainActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this@Login, "Login Failed !!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }else -> {
                        Toast.makeText(this@Login, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }catch (e:GetCredentialException){
                    Toast.makeText(this@Login, "Credential Failure!!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}
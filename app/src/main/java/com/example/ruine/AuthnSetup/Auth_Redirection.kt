package com.example.ruine.AuthnSetup

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.example.ruine.R
import com.example.ruine.databinding.ActivityAuthRedirectionBinding

class Auth_Redirection : AppCompatActivity() {

    private val binding: ActivityAuthRedirectionBinding by lazy {
        ActivityAuthRedirectionBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor= ContextCompat.getColor(this, R.color.black)

        val scopes = listOf(
            "https://www.googleapis.com/auth/gmail.addons.current.message.readonly",
            "https://www.googleapis.com/auth/gmail.addons.current.message.action",
            "https://mail.google.com/",
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.send",
            "https://www.googleapis.com/auth/gmail.addons.current.action.compose",
            "https://www.googleapis.com/auth/gmail.addons.current.message.metadata",
            "https://www.googleapis.com/auth/meetings.space.created",
            "https://www.googleapis.com/auth/meetings.space.readonly",
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/calendar.events",
            "https://www.googleapis.com/auth/calendar.events.readonly",
            "https://www.googleapis.com/auth/calendar.readonly",
            "https://www.googleapis.com/auth/calendar.settings.readonly"
        )
        val encodedScopes = scopes.joinToString(separator = "%20") { Uri.encode(it) }


        val url = "https://accounts.google.com/o/oauth2/v2/auth?scope=$encodedScopes&access_type=offline&include_granted_scopes=true&prompt=consent&response_type=code&state=state_parameter_passthrough_value&redirect_uri=https://ruine-credentials-chu6.onrender.com/callback&client_id=${getString(
            R.string.client_id
        )}&flowName=GeneralOAuthFlow"

        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, Uri.parse(url))

    }
}
package com.edu.tlucontact.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edu.tlucontact.MainActivity
import com.edu.tlucontact.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

import com.edu.tlucontact.data.sercurity.SecretManager

class OutlookLoginActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // Thay thế bằng giá trị thực tế của bạn từ Azure AD
    private val CLIENT_ID = "3387c31c-01f5-4164-bdcf-2d4af9bc75b3"
    private val TENANT_ID = "bbf9aad6-5f58-4387-927e-02f0b07a72fa"
    private val REDIRECT_URI = "msauth://com.edu.tlucontact/A+n3P/nJZeiK/M6y63oz08wiq/o="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outlook_login)

        webView = findViewById(R.id.webView)

        signInWithOutlook() // Gọi hàm đăng nhập ngay khi Activity được tạo
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun signInWithOutlook() {
        val authUrl = "https://login.microsoftonline.com/$TENANT_ID/oauth2/v2.0/authorize?" +
                "client_id=$CLIENT_ID" +
                "&response_type=code" +
                "&redirect_uri=$REDIRECT_URI" +
                "&scope=openid profile email offline_access" // Yêu cầu quyền email và offline_access

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.startsWith(REDIRECT_URI) == true) {
                    webView.visibility = android.view.View.GONE
                    handleRedirectUrl(url)
                    return true
                }
                return false
            }
        }
        webView.loadUrl(authUrl)
        webView.visibility = android.view.View.VISIBLE
    }

    private fun handleRedirectUrl(url: String) {
        val uri = Uri.parse(url)
        val code = uri.getQueryParameter("code")

        if (code != null) {
            getAccessToken(code)
        } else {
            Toast.makeText(this, "Failed to get authorization code.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAccessToken(code: String) {
        val clientSecret = SecretManager.getClientSecret(this)

        if (clientSecret == null) {
            runOnUiThread {
                Toast.makeText(this, "Client secret not found.", Toast.LENGTH_LONG).show()
            }
            return
        }

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT_URI)
            .add("client_id", CLIENT_ID)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url("https://login.microsoftonline.com/$TENANT_ID/oauth2/v2.0/token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OutlookLoginActivity, "Failed to get access token: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = JSONObject(response.body?.string() ?: "")
                    val accessToken = json.getString("access_token")
                    val refreshToken = json.getString("refresh_token") // Lấy refresh_token
                    runOnUiThread {
                        getUserProfile(accessToken)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@OutlookLoginActivity, "Failed to parse access token: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getUserProfile(accessToken: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://graph.microsoft.com/v1.0/me")
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OutlookLoginActivity, "Failed to get user profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = JSONObject(response.body?.string() ?: "")
                    val email = json.getString("mail") ?: json.getString("userPrincipalName")
                    runOnUiThread {
                        Toast.makeText(this@OutlookLoginActivity, "Logged in with email: $email", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@OutlookLoginActivity, MainActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@OutlookLoginActivity, "Failed to parse user profile: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
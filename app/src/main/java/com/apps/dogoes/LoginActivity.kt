package com.apps.dogoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.LoginRequest
import com.apps.dogoes.api.UserResponse
import com.apps.dogoes.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isUserLoggedIn()) {
            Log.d("LoginActivity", "User already logged in, redirecting to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are Required!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        ApiClient.instance.loginUser(loginRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse != null) {
                        val userJson = userResponse.user

                        val userId = userJson.get("_id")?.asString ?: ""
                        val userName = userJson.get("name")?.asString ?: ""
                        val userEmail = userJson.get("email")?.asString ?: ""
                        val userRole = userJson.get("role")?.asString ?: ""
                        val userStatus = userJson.get("status")?.asInt ?: 0
                        val userGeotag = userJson.get("geotag")?.asString
                        val userNotes = userJson.get("notes")?.asString

                        if (userRole == "user") {
                            saveUserData(userId, userName, userEmail, userRole, userStatus, userGeotag, userNotes)
                            Log.d("LoginActivity", "Login successful, user_id: $userId")

                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Only Users can Login!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Wrong Email or Password!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("LoginActivity", "API call failed: ${t.message}")
                Toast.makeText(this@LoginActivity, "Network Error!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserData(
        userId: String, userName: String, userEmail: String, userRole: String,
        userStatus: Int, userGeotag: String?, userNotes: String?
    ) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("user_id", userId)
        editor.putString("user_name", userName)
        editor.putInt("user_status", userStatus)
        editor.putString("user_email", userEmail)
        editor.putString("user_role", userRole)
        editor.putString("user_geotag", userGeotag)
        editor.putString("user_note", userNotes)

        if (editor.commit()) {
            Log.d("LoginActivity", "User data saved successfully")
        } else {
            Log.e("LoginActivity", "Failed to save user data")
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_id", null) != null
    }
}
package com.apps.dogoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.LoginRequest
import com.apps.dogoes.api.UserResponse
import com.apps.dogoes.api.ResetPasswordRequest
import com.apps.dogoes.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isForgotPasswordMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isForgotPasswordMode) {
                forgotPassword(email)
            } else {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Email and Password are Required!", Toast.LENGTH_SHORT).show()
                } else {
                    loginUser(email, password)
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            switchToForgotPasswordMode()
        }
    }

    private fun switchToForgotPasswordMode() {
        isForgotPasswordMode = true
        binding.etPassword.visibility = View.GONE
        binding.tvForgotPassword.visibility = View.GONE
        binding.btnLogin.text = "Reset"
        binding.txtLogin.text = "Forgot"
    }

    private fun forgotPassword(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = ResetPasswordRequest(email)

        ApiClient.instance.sendResetPasswordEmail(requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Failed to send password reset email!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loginUser(email: String, password: String) {
        val apiService = ApiClient.instance
        val loginRequest = LoginRequest(email, password)

        apiService.loginUser(loginRequest).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user?.role != null && user.role == "user") {
                        saveUserData(user)
                        Log.d("LoginActivity", "Login successful, user_id: ${user._id}")

                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Only User can Login!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Wrong Email or Password!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun saveUserData(user: UserResponse) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("user_id", user._id)
        editor.putString("user_name", user.name)
        editor.putInt("user_status", user.status)
        editor.putString("user_email", user.email)
        editor.putString("user_role", user.role)
        editor.putString("user_geotag", user.geotag)
        editor.putString("user_notes", user.notes)

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
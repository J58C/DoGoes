package com.apps.dogoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.LoginRequest
import com.apps.dogoes.api.UserResponse
import com.apps.dogoes.api.ResetPasswordRequest
import com.apps.dogoes.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatDelegate

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isForgotPasswordMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isForgotPasswordMode) {
                    switchToLoginMode()
                } else {
                    finish()
                }
            }
        })

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (isForgotPasswordMode) {
                forgotPassword(email)
            } else {
                if (email.isEmpty() || password.isEmpty()) {
                    if (email.isEmpty() && password.isEmpty()) {
                        showErrorAnimation(binding.etEmail)
                        showErrorAnimation(binding.etPassword)
                        Toast.makeText(this, "Email & Password required!", Toast.LENGTH_SHORT).show()
                    } else if (email.isEmpty()) {
                        showErrorAnimation(binding.etEmail)
                        Toast.makeText(this, "Email required!", Toast.LENGTH_SHORT).show()
                    } else if (password.isEmpty()) {
                        showErrorAnimation(binding.etPassword)
                        Toast.makeText(this, "Password required!", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    loginUser(email, password)
                }
            }
        }

        binding.txtForgotPassword.setOnClickListener {
            switchToForgotPasswordMode()
        }

        binding.txtLogin.setOnClickListener {
            if (isForgotPasswordMode) switchToLoginMode()
        }

        addTextWatchers()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isForgotPasswordMode) {
                    switchToLoginMode()
                } else {
                    finish()
                }
            }
        })
    }

    private fun showErrorAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 10f, -10f, 10f, -10f, 0f)
        animator.duration = 500
        animator.start()

        view.setBackgroundResource(R.drawable.editxt_error)
    }

    private fun addTextWatchers() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etEmail.setBackgroundResource(R.drawable.editxt_normal)
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.etPassword.setBackgroundResource(R.drawable.editxt_normal)
            }
        })
    }

    private fun switchToForgotPasswordMode() {
        isForgotPasswordMode = true

        val fadeOutPassword = ObjectAnimator.ofFloat(binding.etPassword, "alpha", 1f, 0f)
        val fadeOutForgotPassword = ObjectAnimator.ofFloat(binding.txtForgotPassword, "alpha", 1f, 0f)
        val fadeOutSignIn = ObjectAnimator.ofFloat(binding.txtLogin, "alpha", 1f, 0f)
        val fadeOutDescription = ObjectAnimator.ofFloat(binding.txtDescription, "alpha", 1f, 0f)

        fadeOutPassword.duration = 300
        fadeOutForgotPassword.duration = 300
        fadeOutSignIn.duration = 300
        fadeOutDescription.duration = 300

        val fadeOutSet = AnimatorSet()
        fadeOutSet.playTogether(fadeOutPassword, fadeOutForgotPassword, fadeOutSignIn, fadeOutDescription)
        fadeOutSet.start()

        fadeOutSet.doOnEnd {
            binding.etPassword.visibility = View.INVISIBLE
            binding.txtForgotPassword.visibility = View.INVISIBLE

            binding.btnLogin.text = getString(R.string.reset)
            binding.txtLogin.text = getString(R.string.forgot)
            binding.txtDescription.text = getString(R.string.description_f)

            binding.txtLogin.visibility = View.VISIBLE
            binding.txtDescription.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE

            binding.txtLogin.alpha = 0f
            binding.txtDescription.alpha = 0f
            binding.btnLogin.alpha = 0f

            val fadeInLoginText = ObjectAnimator.ofFloat(binding.txtLogin, "alpha", 0f, 1f)
            val fadeInDescription = ObjectAnimator.ofFloat(binding.txtDescription, "alpha", 0f, 1f)
            val fadeInButton = ObjectAnimator.ofFloat(binding.btnLogin, "alpha", 0f, 1f)

            fadeInLoginText.duration = 500
            fadeInDescription.duration = 500
            fadeInButton.duration = 500

            AnimatorSet().apply {
                playTogether(fadeInLoginText, fadeInDescription, fadeInButton)
                start()
            }
        }
    }

    private fun switchToLoginMode() {
        isForgotPasswordMode = false

        val fadeOutDescription = ObjectAnimator.ofFloat(binding.txtDescription, "alpha", 1f, 0f)
        val fadeOutLoginText = ObjectAnimator.ofFloat(binding.txtLogin, "alpha", 1f, 0f)
        val fadeOutButton = ObjectAnimator.ofFloat(binding.btnLogin, "alpha", 1f, 0f)

        fadeOutDescription.duration = 300
        fadeOutLoginText.duration = 300
        fadeOutButton.duration = 300

        val fadeOutSet = AnimatorSet()
        fadeOutSet.playTogether(fadeOutDescription, fadeOutLoginText, fadeOutButton)
        fadeOutSet.start()

        fadeOutSet.doOnEnd {
            binding.txtDescription.visibility = View.INVISIBLE
            binding.txtLogin.visibility = View.INVISIBLE

            binding.etPassword.visibility = View.VISIBLE
            binding.txtForgotPassword.visibility = View.VISIBLE
            binding.btnLogin.text = getString(R.string.login)
            binding.txtLogin.text = getString(R.string.sign_in)
            binding.txtDescription.text = getString(R.string.description_i)

            binding.txtLogin.visibility = View.VISIBLE
            binding.txtDescription.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE

            binding.txtLogin.alpha = 0f
            binding.txtDescription.alpha = 0f
            binding.btnLogin.alpha = 0f

            val fadeInPassword = ObjectAnimator.ofFloat(binding.etPassword, "alpha", 0f, 1f)
            val fadeInForgotPassword = ObjectAnimator.ofFloat(binding.txtForgotPassword, "alpha", 0f, 1f)
            val fadeInLoginText = ObjectAnimator.ofFloat(binding.txtLogin, "alpha", 0f, 1f)
            val fadeInDescription = ObjectAnimator.ofFloat(binding.txtDescription, "alpha", 0f, 1f)
            val fadeInButton = ObjectAnimator.ofFloat(binding.btnLogin, "alpha", 0f, 1f)

            fadeInPassword.duration = 500
            fadeInForgotPassword.duration = 500
            fadeInLoginText.duration = 500
            fadeInDescription.duration = 500
            fadeInButton.duration = 500

            AnimatorSet().apply {
                playTogether(fadeInPassword, fadeInForgotPassword, fadeInLoginText, fadeInDescription, fadeInButton)
                start()
            }
        }
    }

    private fun forgotPassword(email: String) {
        if (email.isEmpty()) {
            showErrorAnimation(binding.etEmail)
            Toast.makeText(this, "Email is required!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = ResetPasswordRequest(email)

        ApiClient.instance.sendResetPasswordEmail(requestBody).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Reset email sent!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Failed to send reset!", Toast.LENGTH_SHORT).show()
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
                    if (user?.role != null && user.role in listOf("user", "admin", "superadmin")) {
                        saveUserData(user)
                        Log.d("LoginActivity", "Login successful, user_id: ${user._id}")

                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "UNNES access only!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserData(user: UserResponse) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("user_id", user._id)
        editor.putString("user_name", user.name)
        editor.putInt("user_status", user.status)
        editor.putString("user_email", user.email)
        editor.putString("user_role", user.role)
        editor.putString("user_geotag", user.geotag)
        editor.putString("user_notes", user.notes)
        editor.putString("user_key", user.token)

        if (editor.commit()) {
            Log.d("LoginActivity", "User data saved successfully")
        } else {
            Log.e("LoginActivity", "Failed to save user data")
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_id", null) != null
    }
}
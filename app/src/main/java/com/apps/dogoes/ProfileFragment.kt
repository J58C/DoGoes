package com.apps.dogoes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.ChangePasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit

class ProfileFragment : Fragment() {
    private lateinit var tvUserName: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private var isChangingPassword = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvUserName = view.findViewById(R.id.tvUserName)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
        etOldPassword = view.findViewById(R.id.etOldPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")
        val userName = sharedPreferences.getString("user_name", "Guest")

        tvUserName.text = userName

        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 500 }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 500 }

        btnChangePassword.setOnClickListener {
            if (!isChangingPassword) {
                isChangingPassword = true
                btnLogout.startAnimation(fadeOut)
                btnLogout.visibility = View.GONE

                etOldPassword.visibility = View.VISIBLE
                etNewPassword.visibility = View.VISIBLE

                etOldPassword.startAnimation(fadeIn)
                etNewPassword.startAnimation(fadeIn)

                btnChangePassword.text = getString(R.string.submit)
                val params = btnChangePassword.layoutParams
                params.width = dpToPx(220)
                btnChangePassword.layoutParams = params
            } else {
                val oldPW = etOldPassword.text.toString()
                val newPW = etNewPassword.text.toString()

                if (oldPW.isEmpty() || newPW.isEmpty()) {
                    Toast.makeText(requireContext(), "Fill in all the fields!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                userId?.let {
                    changePassword(it, oldPW, newPW)
                } ?: run {
                    Toast.makeText(requireContext(), "User ID not found!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnLogout.setOnClickListener {
            sharedPreferences.edit { clear().apply() }
            Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isChangingPassword) {
                    resetChangePasswordUI()
                } else {
                    val viewPager = requireActivity().findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
                    viewPager?.currentItem = 1
                }
            }
        })

        return view
    }

    private fun changePassword(userId: String, oldPW: String, newPW: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userKey = sharedPreferences.getString("user_key", null) ?: return

        val apiService = ApiClient.instance
        val request = ChangePasswordRequest(oldPW, newPW, userKey)

        apiService.changePassword(userId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()
                    resetChangePasswordUI()
                } else {
                    Toast.makeText(requireContext(), "Failed to change password!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(requireContext(), userId, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetChangePasswordUI() {
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 500 }
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 500 }

        etOldPassword.startAnimation(fadeOut)
        etNewPassword.startAnimation(fadeOut)

        etOldPassword.visibility = View.GONE
        etNewPassword.visibility = View.GONE

        btnLogout.visibility = View.VISIBLE
        btnLogout.startAnimation(fadeIn)

        btnChangePassword.text = getString(R.string.change)
        val params = btnChangePassword.layoutParams
        params.width = dpToPx(300)
        btnChangePassword.layoutParams = params

        isChangingPassword = false
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}
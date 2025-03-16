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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.ChangePasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var btnSubmitPassword: Button
    private lateinit var btnCancel: Button
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnSubmitPassword = view.findViewById(R.id.btnSubmitPassword)
        btnCancel = view.findViewById(R.id.btnCancel)
        etOldPassword = view.findViewById(R.id.etOldPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "")

        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 500 }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 500 }

        btnChangePassword.setOnClickListener {
            btnLogout.startAnimation(fadeOut)
            btnLogout.visibility = View.GONE
            btnChangePassword.startAnimation(fadeOut)
            btnChangePassword.visibility = View.GONE

            etOldPassword.visibility = View.VISIBLE
            etNewPassword.visibility = View.VISIBLE
            btnSubmitPassword.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE

            etOldPassword.startAnimation(fadeIn)
            etNewPassword.startAnimation(fadeIn)
            btnSubmitPassword.startAnimation(fadeIn)
            btnCancel.startAnimation(fadeIn)
        }

        btnCancel.setOnClickListener {
            etOldPassword.startAnimation(fadeOut)
            etNewPassword.startAnimation(fadeOut)
            btnSubmitPassword.startAnimation(fadeOut)
            btnCancel.startAnimation(fadeOut)

            etOldPassword.visibility = View.GONE
            etNewPassword.visibility = View.GONE
            btnSubmitPassword.visibility = View.GONE
            btnCancel.visibility = View.GONE

            btnLogout.visibility = View.VISIBLE
            btnChangePassword.visibility = View.VISIBLE
            btnLogout.startAnimation(fadeIn)
            btnChangePassword.startAnimation(fadeIn)
        }

        btnSubmitPassword.setOnClickListener {
            val oldPW = etOldPassword.text.toString()
            val newPW = etNewPassword.text.toString()

            if (oldPW.isEmpty() || newPW.isEmpty()) {
                Toast.makeText(requireContext(), "Isi semua kolom!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userId?.let {
                changePassword(it, oldPW, newPW)
            } ?: run {
                Toast.makeText(requireContext(), "User ID tidak ditemukan!", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogout.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }

    private fun changePassword(userId: String, oldPW: String, newPW: String) {
        val apiService = ApiClient.instance
        val request = ChangePasswordRequest(oldPW, newPW)

        apiService.changePassword(userId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                    btnCancel.performClick()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengubah password!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(requireContext(), userId, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
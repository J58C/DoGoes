package com.apps.dogoes

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.AddAnnouncementRequest
import com.apps.dogoes.api.AddAnnouncementResponse
import com.apps.dogoes.api.DeleteAnnouncementRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher

class AnnouncementsFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnSend: Button
    private lateinit var btnViewAnnouncements: Button

    private var lastId: String? = null
    private var lastTitle: String? = null
    private var lastContent: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_announcements, container, false)

        etTitle = view.findViewById(R.id.etTitle)
        etContent = view.findViewById(R.id.etContent)
        btnSend = view.findViewById(R.id.btnSend)
        btnViewAnnouncements = view.findViewById(R.id.btnViewAnnouncements)

        btnSend.setOnClickListener {
            uploadAnnouncement()
        }

        btnViewAnnouncements.setOnClickListener {
            val intent = Intent(requireContext(), AnnouncementsListActivity::class.java)
            startActivity(intent)
        }

        addTextWatchers()

        return view
    }

    private fun uploadAnnouncement() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val userKey = sharedPreferences.getString("user_key", null)

        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty()) {
            showErrorAnimation(etTitle)
        }

        if (content.isEmpty()) {
            showErrorAnimation(etContent)
        }

        if (title.isEmpty() || content.isEmpty()) {
            Snackbar.make(requireView(), "Title and Content cannot be empty", Snackbar.LENGTH_SHORT).show()
            return
        }

        val request = AddAnnouncementRequest(content, userId.toString(), title, userKey.toString())

        ApiClient.instance.uploadAnnouncement(request).enqueue(object : Callback<AddAnnouncementResponse> {
            override fun onResponse(call: Call<AddAnnouncementResponse>, response: Response<AddAnnouncementResponse>) {
                if (response.isSuccessful) {
                    val uploadedData = response.body()
                    if (uploadedData != null) {
                        lastId = uploadedData._id
                        lastTitle = title
                        lastContent = content

                        etTitle.text.clear()
                        etContent.text.clear()
                        etTitle.clearFocus()
                        etContent.clearFocus()
                        hideKeyboard()

                        Snackbar.make(requireView(), "Upload Successful!", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                undoUpload()
                            }
                            .show()
                    }
                } else {
                    Log.e("UploadError", "Failed: ${response.errorBody()?.string()}")
                    Snackbar.make(requireView(), "Failed to Upload", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddAnnouncementResponse>, t: Throwable) {
                Log.e("UploadError", "Network Error: ${t.message}")
                Snackbar.make(requireView(), "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun undoUpload() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userKey = sharedPreferences.getString("user_key", null)

        if (lastId != null && userKey != null) {
            val request = DeleteAnnouncementRequest(secret_key = userKey)

            ApiClient.instance.deleteAnnouncement(lastId!!, request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        etTitle.setText(lastTitle)
                        etContent.setText(lastContent)
                        Snackbar.make(requireView(), "Upload Undone!", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(requireView(), "Failed to Undo", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Snackbar.make(requireView(), "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
                }
            })
        } else {
            Snackbar.make(requireView(), "No recent upload to undo or user_key missing", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun showErrorAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 10f, -10f, 10f, -10f, 0f)
        animator.duration = 500
        animator.start()

        view.setBackgroundResource(R.drawable.editxt_error)
    }

    private fun addTextWatchers() {
        etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                etTitle.setBackgroundResource(R.drawable.editxt_normal)
            }
        })

        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                etContent.setBackgroundResource(R.drawable.editxt_normal)
            }
        })
    }
}
package com.apps.dogoes

import android.animation.ObjectAnimator
import android.content.Context
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
import com.apps.dogoes.api.AnnouncementRequest
import com.apps.dogoes.api.AnnouncementResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher

class AnnouncementsFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnSend: Button

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

        btnSend.setOnClickListener {
            uploadAnnouncement()
        }

        addTextWatchers()

        return view
    }

    private fun uploadAnnouncement() {
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

        val request = AnnouncementRequest(title, content)

        ApiClient.instance.uploadAnnouncement(request).enqueue(object : Callback<AnnouncementResponse> {
            override fun onResponse(call: Call<AnnouncementResponse>, response: Response<AnnouncementResponse>) {
                if (response.isSuccessful) {
                    val uploadedData = response.body()
                    if (uploadedData != null) {
                        lastId = uploadedData._id
                        lastTitle = uploadedData.title
                        lastContent = uploadedData.content

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

            override fun onFailure(call: Call<AnnouncementResponse>, t: Throwable) {
                Log.e("UploadError", "Network Error: ${t.message}")
                Snackbar.make(requireView(), "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun undoUpload() {
        if (lastId != null) {
            ApiClient.instance.deleteAnnouncement(lastId!!).enqueue(object : Callback<Void> {
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
            Snackbar.make(requireView(), "No recent upload to undo", Snackbar.LENGTH_SHORT).show()
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
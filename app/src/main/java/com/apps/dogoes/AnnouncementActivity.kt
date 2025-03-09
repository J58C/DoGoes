package com.apps.dogoes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.AnnouncementRequest
import com.apps.dogoes.api.AnnouncementResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnnouncementActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnUpload: Button

    private var lastId: String? = null
    private var lastTitle: String? = null
    private var lastContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnUpload = findViewById(R.id.btnUpload)

        btnUpload.setOnClickListener {
            uploadAnnouncement()
        }
    }

    private fun uploadAnnouncement() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Snackbar.make(btnUpload, "Title and Content cannot be empty", Snackbar.LENGTH_SHORT).show()
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

                        Snackbar.make(btnUpload, "Upload Successful!", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                undoUpload()
                            }
                            .show()
                    }
                } else {
                    Log.e("UploadError", "Failed: ${response.errorBody()?.string()}")
                    Snackbar.make(btnUpload, "Failed to Upload", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AnnouncementResponse>, t: Throwable) {
                Log.e("UploadError", "Network Error: ${t.message}")
                Snackbar.make(btnUpload, "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
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

                        Snackbar.make(btnUpload, "Upload Undone!", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(btnUpload, "Failed to Undo", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Snackbar.make(btnUpload, "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
                }
            })
        } else {
            Snackbar.make(btnUpload, "No recent upload to undo", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}
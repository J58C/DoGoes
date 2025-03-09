package com.apps.dogoes

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            Toast.makeText(this, "Title and Content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AnnouncementRequest(title, content)

        ApiClient.instance.uploadAnnouncement(request).enqueue(object : Callback<AnnouncementResponse> {
            override fun onResponse(call: Call<AnnouncementResponse>, response: Response<AnnouncementResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AnnouncementActivity, "Upload Successful!", Toast.LENGTH_SHORT).show()

                    etTitle.text.clear()
                    etContent.text.clear()

                    etTitle.clearFocus()
                    etContent.clearFocus()

                    hideKeyboard()
                } else {
                    Log.e("UploadError", "Failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@AnnouncementActivity, "Failed to Upload", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AnnouncementResponse>, t: Throwable) {
                Log.e("UploadError", "Network Error: ${t.message}")
                Toast.makeText(this@AnnouncementActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}
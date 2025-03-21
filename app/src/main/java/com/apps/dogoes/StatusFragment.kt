package com.apps.dogoes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.apps.dogoes.api.UpdateStatusRequest
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatusFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var webView: WebView
    private lateinit var btnLocation: Button
    private lateinit var btnSend: Button
    private lateinit var etNote: EditText
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null
    private var isSlidDown = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        webView = view.findViewById(R.id.webViewMap)
        btnLocation = view.findViewById(R.id.btnLocation)
        btnSend = view.findViewById(R.id.btnSend)
        etNote = view.findViewById(R.id.etNote)
        autoCompleteTextView = view.findViewById(R.id.tvavailable)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }
        WebView.setWebContentsDebuggingEnabled(false)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
        }
        webView.loadUrl("file:///android_asset/leaflet_map.html")

        val listofStatus = arrayOf("Unavailable", "Available")
        autoCompleteTextView.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listofStatus)
        )
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            autoCompleteTextView.setText(listofStatus[position], false)
        }

        val userStatus = sharedPreferences.getInt("user_status", 0)
        autoCompleteTextView.setText(if (userStatus == 1) "Available" else "Unavailable", false)

        btnLocation.setOnClickListener { getLocation() }
        btnSend.setOnClickListener { sendUserStatusUpdate() }
    }

    private fun slideIn(view: View, onEnd: (() -> Unit)? = null) {
        view.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 100f, 0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        )
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()

        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                onEnd?.invoke()
            }
        })

        animator.start()
    }

    private fun slideOut(view: View) {
        val animator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, 100f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
        )
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.start()

        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                view.visibility = View.GONE
            }
        })
    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_LONG).show()
            }
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            false
        } else {
            true
        }
    }

    private fun getLocation() {
        if (!checkLocationPermission()) return

        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    if (location.latitude != lastLatitude || location.longitude != lastLongitude) {
                        lastLatitude = location.latitude
                        lastLongitude = location.longitude
                        webView.evaluateJavascript("updateLocation(${location.latitude}, ${location.longitude});", null)
                    }
                    if (!isSlidDown) {
                        slideIn(webView)
                        slideIn(etNote) {
                            etNote.post { etNote.invalidate(); etNote.requestLayout() }
                        }
                        slideIn(btnSend)
                        isSlidDown = true
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to Get Location", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Location request failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendUserStatusUpdate() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null) ?: return
        val status = if (autoCompleteTextView.text.toString() == "Available") 1 else 0
        val notes = etNote.text.toString()
        val userKey = sharedPreferences.getString("user_key", null) ?: return

        ApiClient.instance.updateUserStatus(userId, UpdateStatusRequest(status, "${lastLatitude},${lastLongitude}", notes, userKey))
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        Snackbar.make(btnSend, "Status Updated!", Snackbar.LENGTH_SHORT).show()
                        etNote.text.clear()
                        slideOut(webView)
                        slideOut(etNote)
                        slideOut(btnSend)
                        isSlidDown = false
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Snackbar.make(btnSend, "Error: ${t.message}", Snackbar.LENGTH_LONG).show()
                }
            })
    }
}
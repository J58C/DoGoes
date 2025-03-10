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
import android.view.animation.DecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.apps.dogoes.api.UpdateStatusRequest
import com.apps.dogoes.api.ApiClient
import com.apps.dogoes.api.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var webView: WebView
    private lateinit var btnLocation: Button
    private lateinit var btnSend: Button
    private lateinit var etNote: EditText
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var tvUserName: TextView
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null
    private var lastLocation: String? = null
    private var isSlidDown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            requireActivity().startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        webView = view.findViewById(R.id.webViewMap)
        btnLocation = view.findViewById(R.id.btnLocation)
        btnSend = view.findViewById(R.id.btnSend)
        etNote = view.findViewById(R.id.etNote)
        tvUserName = view.findViewById(R.id.tvUserName)
        autoCompleteTextView = view.findViewById(R.id.tvavailable)

        webView.settings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(false)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/leaflet_map.html")

        val listofStatus = arrayOf("Unavailable", "Available")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listofStatus)
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setOnClickListener { autoCompleteTextView.showDropDown() }
        autoCompleteTextView.keyListener = null

        val userName = sharedPreferences.getString("user_name", "Guest")
        val userStatus = sharedPreferences.getInt("user_status", 0)
        autoCompleteTextView.setText(if (userStatus == 1) "Available" else "Unavailable", false)
        tvUserName.text = userName

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            Toast.makeText(requireContext(), "Logged out!", Toast.LENGTH_SHORT).show()
            requireActivity().startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }

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
        return if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_LONG).show()
            }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            false
        } else {
            true
        }
    }

    private fun getLocation() {
        if (!checkLocationPermission()) return

        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            if (location != null) {
                if (location.latitude != lastLatitude || location.longitude != lastLongitude) {
                    lastLatitude = location.latitude
                    lastLongitude = location.longitude
                    webView.evaluateJavascript("updateLocation(${location.latitude}, ${location.longitude});", null)
                }
                if (!isSlidDown){
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
        }
    }

    private fun sendUserStatusUpdate() {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null) ?: return
        val status = if (autoCompleteTextView.text.toString() == "Available") 1 else 0
        val notes = etNote.text.toString()

        ApiClient.instance.updateUserStatus(userId, UpdateStatusRequest(status, "${lastLatitude},${lastLongitude}", notes))
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
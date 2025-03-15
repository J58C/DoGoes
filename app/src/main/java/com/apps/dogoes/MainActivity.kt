package com.apps.dogoes

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.forEach
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        viewPager = findViewById(R.id.viewPager)
        tvUserName = findViewById(R.id.tvUserName)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.currentItem = 1
        bottomNavigationView.selectedItemId = R.id.navigation_status

        bottomNavigationView.setOnItemSelectedListener { item ->
            animateNavItem(item.itemId)
            val selectedTab = when (item.itemId) {
                R.id.navigation_announcement -> 0
                R.id.navigation_status -> 1
                R.id.navigation_profile -> 2
                else -> return@setOnItemSelectedListener false
            }

            if (viewPager.currentItem != selectedTab) {
                viewPager.setCurrentItem(selectedTab, true)
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = bottomNavigationView.menu[position].itemId
                animateNavItem(itemId)
                bottomNavigationView.menu[position].isChecked = true

                val logoUnnes = findViewById<View>(R.id.logoUnnes)
                val txtHello = findViewById<View>(R.id.txtHello)

                if (position == 2) {
                    logoUnnes.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
                        logoUnnes.visibility = View.INVISIBLE
                    }?.start()

                    txtHello.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
                        txtHello.visibility = View.INVISIBLE
                    }?.start()

                    tvUserName.animate()?.alpha(0f)?.setDuration(300)?.withEndAction {
                        tvUserName.visibility = View.INVISIBLE
                    }?.start()

                } else {
                    logoUnnes.visibility = View.VISIBLE
                    txtHello.visibility = View.VISIBLE
                    tvUserName.visibility = View.VISIBLE

                    logoUnnes.animate()?.alpha(1f)?.setDuration(300)?.start()
                    txtHello.animate()?.alpha(1f)?.setDuration(300)?.start()
                    tvUserName.animate()?.alpha(1f)?.setDuration(300)?.start()
                }
            }
        })

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Guest")
        tvUserName.text = userName
    }

    private fun animateNavItem(itemId: Int) {
        bottomNavigationView.menu.forEach { menuItem ->
            val view = findViewById<View>(menuItem.itemId)
            if (view != null) {
                if (menuItem.itemId == itemId) {
                    view.animate().scaleX(1.1f).scaleY(1.1f).alpha(1f)
                        .setDuration(300).setInterpolator(DecelerateInterpolator()).start()
                } else {
                    view.animate().scaleX(1f).scaleY(1f).alpha(0.7f)
                        .setDuration(300).setInterpolator(AccelerateInterpolator()).start()
                }
            }
        }
    }
}
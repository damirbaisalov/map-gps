package com.example.map_gps

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var locale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("AAA", "Activity created")

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        if (getLanguageSharedPreferences()==0)
            setLocaleWithoutRefresh("en-us")
        else
            setLocaleWithoutRefresh("kk")
    }

    private fun setLocaleWithoutRefresh(languageName: String) {
        locale = Locale(languageName)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = locale
        res.updateConfiguration(conf, dm)
    }

    private fun getLanguageSharedPreferences(): Int {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getInt(USER_LANGUAGE, 0)
    }
}
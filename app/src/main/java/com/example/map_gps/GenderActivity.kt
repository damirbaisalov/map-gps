package com.example.map_gps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable

const val MY_APP_USER_ACTIVITY = "MY_APP_USER_ACTIVITY"
const val USER_GENDER = "USER_WOMAN_GENDER"
class GenderActivity : AppCompatActivity() {

    private lateinit var womanImageView: ImageView
    private lateinit var manImageView: ImageView
    private lateinit var womanDoneImageView: ImageView
    private lateinit var manDoneImageView: ImageView

    private var gender: Int = 3

    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gender)

        if (getGenderSharedPreferences()!=3){
            val intent = Intent(this@GenderActivity, ProfileDataActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        init()
    }

    private fun init() {
        womanImageView = findViewById(R.id.activity_gender_woman_image_view)
        manImageView = findViewById(R.id.activity_gender_man_image_view)
        womanDoneImageView = findViewById(R.id.activity_gender_woman_done_image_view)
        manDoneImageView = findViewById(R.id.activity_gender_man_done_image_view)
        continueButton = findViewById(R.id.activity_gender_continue_button)

        womanImageView.setOnClickListener {
            gender = 1

            womanDoneImageView.visibility = View.VISIBLE
            manDoneImageView.visibility = View.INVISIBLE

            womanImageView.setBackgroundResource(R.drawable.circle_image_view_ripple)
            manImageView.background = null


            continueButton.isEnabled = true
        }

        manImageView.setOnClickListener {
            gender = 0

            manDoneImageView.visibility = View.VISIBLE
            womanDoneImageView.visibility = View.INVISIBLE

            manImageView.setBackgroundResource(R.drawable.circle_image_view_ripple)
            womanImageView.background = null


            continueButton.isEnabled = true
        }

        continueButton.setOnClickListener {
            saveGenderSharedPreferences(gender)
            successReg()
        }

    }

    private fun successReg() {
        val intent = Intent(this, ProfileDataActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveGenderSharedPreferences(gender: Int) {
        val sharedPref = this.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(USER_GENDER, gender)
        editor.apply()
    }

    private fun getGenderSharedPreferences(): Int {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getInt(USER_GENDER, 3)
    }
}
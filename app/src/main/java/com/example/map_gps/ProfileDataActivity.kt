package com.example.map_gps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible


const val USER_HEIGHT = "USER_HEIGHT"
const val USER_WEIGHT = "USER_WEIGHT"
class ProfileDataActivity : AppCompatActivity() {

    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var warningHeightTextView: TextView
    private lateinit var warningWeightTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_data)

        if (getSavedHeight()!="0" && getSavedWeight()!="0"){
            val intent = Intent(this@ProfileDataActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        init()
    }

    private fun init() {
        heightEditText= findViewById(R.id.set_height_activity_profile_data_edit_text)
        weightEditText= findViewById(R.id.set_weight_activity_profile_data_edit_text)
        continueButton= findViewById(R.id.activity_profile_data_continue_button)

        warningHeightTextView= findViewById(R.id.activity_profile_data_height_warning_tv)
        warningWeightTextView= findViewById(R.id.activity_profile_data_weight_warning_tv)

        continueButton.setOnClickListener {
           if (handleHeightAndWeight(height = heightEditText.text.toString(), weight = weightEditText.text.toString())) {
               successReg()
           }
        }

        checkEditTextForTextWatcher(heightEditText)
        checkEditTextForTextWatcher(weightEditText)
    }

    private fun checkEditTextForTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val heightInput = heightEditText.text.toString().trim()
                val weightInput = weightEditText.text.toString().trim()

                continueButton.isEnabled = (heightInput.isNotEmpty() && weightInput.isNotEmpty())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun handleHeightAndWeight(height: String?, weight: String?): Boolean {
        if (height != null && weight !=null) {

            if (height.toInt()<20 || height.toInt()>240) {
                warningHeightTextView.isVisible = true
                return false
            } else {
                warningHeightTextView.isVisible = false
            }

            if (weight.toInt()<20 || weight.toInt()>300) {
                warningWeightTextView.isVisible = true
                return false
            } else {
                warningWeightTextView.isVisible = false
            }
        }
        
        warningWeightTextView.isVisible = false
        warningWeightTextView.isVisible = false
        return true
    }

    private fun successReg() {
        saveUserData(heightEditText.text.toString(), weightEditText.text.toString())
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun saveUserData(height: String?, weight: String?) {
        val sharedPref = this.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(USER_HEIGHT, height)
        editor.putString(USER_WEIGHT, weight)
        editor.apply()
    }

    private fun getSavedHeight(): String {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_HEIGHT, "0") ?: "0"
    }

    private fun getSavedWeight(): String {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_WEIGHT, "0") ?: "0"
    }
}
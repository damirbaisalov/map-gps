package com.example.map_gps

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import java.util.*
import kotlin.concurrent.fixedRateTimer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ThirdFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
const val USER_LANGUAGE = "USER_LANGUAGE"
class ThirdFragment : Fragment(), DialogFragmentHeight.OnInputSelected ,DialogFragmentWeight.OnInputSelected, AdapterView.OnItemSelectedListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rootView: View

    private lateinit var heightLinearLayout: LinearLayout
    private lateinit var weightLinearLayout: LinearLayout
    private lateinit var heightTextView: TextView
    private lateinit var weightTextView: TextView

    private lateinit var genderImageView: ImageView

    private lateinit var spinner: Spinner
    private lateinit var genderSpinner: Spinner
    private lateinit var locale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_third, container, false)

        init()

        return rootView
    }

    private fun init() {
        genderImageView = rootView.findViewById(R.id.fragment_third_gender_image_view)

        heightLinearLayout = rootView.findViewById(R.id.height_linear_layout)
        heightLinearLayout.setOnClickListener {
            val dialog = DialogFragmentHeight()
            dialog.show(childFragmentManager, "dialog_fragment_height")
        }
        weightLinearLayout = rootView.findViewById(R.id.weight_linear_layout)
        weightLinearLayout.setOnClickListener {
            val dialog = DialogFragmentWeight()
            dialog.show(childFragmentManager, "dialog_fragment_weight")
        }
        heightTextView = rootView.findViewById(R.id.third_fragment_height_tv)
        weightTextView = rootView.findViewById(R.id.third_fragment_weight_tv)
        heightTextView.text = (getSavedHeight()+" см")
        weightTextView.text = (getSavedWeight()+" кг")


        genderSpinner = rootView.findViewById(R.id.third_fragment_gender_spinner)
        ArrayAdapter.createFromResource(
            rootView.context,
            R.array.Genders,
            R.layout.spinner_item_selected
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.my_drop_down_item)
            // Apply the adapter to the spinner
            genderSpinner.adapter = adapter
        }
        if (getGenderSharedPreferences()==0){
            genderImageView.setImageResource(R.drawable.man)
            genderSpinner.setSelection(0, true)
        }
        else
        {
            genderImageView.setImageResource(R.drawable.woman)
            genderSpinner.setSelection(1, true)
        }
        genderSpinner.onItemSelectedListener = this

        spinner = rootView.findViewById(R.id.third_fragment_spinner)
        ArrayAdapter.createFromResource(
            rootView.context,
            R.array.Languages,
            R.layout.spinner_item_selected
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.my_drop_down_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.setSelection(getLanguageSharedPreferences(), true)
        spinner.onItemSelectedListener = this

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (parent != null) {
            when(parent.id) {
                R.id.third_fragment_gender_spinner -> {
                    saveGenderSharedPreferences(position)
                    when(position) {
                        0 -> genderImageView.setImageResource(R.drawable.man)
                        1 -> genderImageView.setImageResource(R.drawable.woman)
                    }
                }
                R.id.third_fragment_spinner -> {
                    saveUserLanguage(position)
                    when(position) {
                        0 -> setLocale("en-us")
                        1 -> setLocale("kk")
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


    private fun setLocale(languageName: String) {
        locale = Locale(languageName)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = locale
        res.updateConfiguration(conf, dm)

//        val refresh = Intent(rootView.context, MainActivity::class.java)
//        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        startActivity(refresh)
//        requireActivity().finish()

        activity?.recreate()
    }

    override fun sendInputHeight(input: String) {
        heightTextView.text = ("$input см")
        saveUserHeight(input)
    }

    override fun sendInputWeight(input: String) {
        weightTextView.text = ("$input кг")
        saveUserWeight(input)
    }

    private fun saveUserLanguage(language: Int) {
        val sharedPref = rootView.context.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(USER_LANGUAGE, language)
        editor.apply()
    }

    private fun getLanguageSharedPreferences(): Int {
        val sharedPreferences: SharedPreferences = rootView.context.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getInt(USER_LANGUAGE, 0)
    }

    private fun saveUserHeight(height: String?) {
        val sharedPref = rootView.context.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(USER_HEIGHT, height)
        editor.apply()
    }

    private fun saveUserWeight(weight: String?) {
        val sharedPref = rootView.context.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(USER_WEIGHT, weight)
        editor.apply()
    }

    private fun getSavedHeight(): String {
        val sharedPreferences: SharedPreferences = rootView.context.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_HEIGHT, "0") ?: "0"
    }

    private fun getSavedWeight(): String {
        val sharedPreferences: SharedPreferences = rootView.context.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_WEIGHT, "0") ?: "0"
    }

    private fun saveGenderSharedPreferences(gender: Int) {
        val sharedPref = rootView.context.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(USER_GENDER, gender)
        editor.apply()
    }

    private fun getGenderSharedPreferences(): Int {
        val sharedPreferences: SharedPreferences = rootView.context.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getInt(USER_GENDER, 3)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ThirdFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ThirdFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
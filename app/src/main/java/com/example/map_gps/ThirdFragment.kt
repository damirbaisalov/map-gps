package com.example.map_gps

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.map_gps.DialogFragmentHeight
import com.example.map_gps.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ThirdFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ThirdFragment : Fragment(), DialogFragmentHeight.OnInputSelected ,DialogFragmentWeight.OnInputSelected{

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rootView: View

    private lateinit var heightLinearLayout: LinearLayout
    private lateinit var weightLinearLayout: LinearLayout
    private lateinit var heightTextView: TextView
    private lateinit var weightTextView: TextView


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

    override fun sendInputHeight(input: String) {
        heightTextView.text = ("$input см")
        saveUserHeight(input)
    }

    override fun sendInputWeight(input: String) {
        weightTextView.text = ("$input кг")
        saveUserWeight(input)
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
}
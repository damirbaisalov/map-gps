package com.example.map_gps

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException

class DialogFragmentHeight: DialogFragment() {

    interface OnInputSelected {
        fun sendInputHeight(input : String)
    }

    lateinit var onInputSelected: OnInputSelected

    private lateinit var heightEditText: EditText
    private lateinit var heightWarningTv: TextView
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.dialog_fragment_height, container, false)

        heightEditText = rootView.findViewById(R.id.set_height_dialog_fragment_edit_text)
        heightWarningTv = rootView.findViewById(R.id.height_warning_tv)
        saveButton = rootView.findViewById(R.id.dialog_fragment_height_save_button)

        saveButton.setOnClickListener {
            val input = heightEditText.text.toString()
            var inputNum = 0

            if (input=="") {
                dialog?.dismiss()
            } else {
                inputNum = input.toInt()
            }

            if (input != "") {
                if (inputNum>240 || inputNum<20) {

                    heightWarningTv.visibility = View.VISIBLE

                } else {
                    onInputSelected.sendInputHeight(input)
                    dialog?.dismiss()
                }
            }
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onInputSelected = parentFragment as OnInputSelected

        } catch (e: ClassCastException) {

        }
    }
}
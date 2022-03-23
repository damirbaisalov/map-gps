package com.example.map_gps

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException

class DialogFragmentSteps: DialogFragment() {

    interface OnInputSelected {
        fun sendInput(input : String)
    }

    lateinit var onInputSelected: OnInputSelected

    private lateinit var stepsEditText: EditText
    private lateinit var okButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.dialog_fragment_steps, container, false)

        stepsEditText = rootView.findViewById(R.id.set_steps_edit_text)
        okButton = rootView.findViewById(R.id.dialog_fragment_ok_button)


        okButton.setOnClickListener {
            val input = stepsEditText.text.toString()

            if (input != "") {
                onInputSelected.sendInput(input)
            }
            dialog?.dismiss()
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
package com.example.map_gps

import android.Manifest.permission.*
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.map_gps.services.TimerService
import com.example.map_gps.viewmodel.FirstViewModel
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
const val USER_MAX_STEPS = "USER_MAX_STEPS"
class FirstFragment : Fragment(), LocListenerInterface, DialogFragmentSteps.OnInputSelected {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rootView: View
    private lateinit var pLauncher: ActivityResultLauncher<String>

    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? =null
    private lateinit var myLocListener: MyLocListener
    private var distance: Float = 0f

    private lateinit var tvStepsTaken: TextView
    private lateinit var progressBar: CircularProgressBar

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resumeButton: Button
    private lateinit var resetButton: Button

    private lateinit var kmTextView: TextView
    private lateinit var kkalTextView: TextView
    private lateinit var timeTextView: TextView

    private lateinit var stepsToChangeLinearLayout: LinearLayout
    private lateinit var totalMaxStepsTv: TextView

    private lateinit var vm: FirstViewModel

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
        rootView = inflater.inflate(R.layout.fragment_first, container, false)

        Log.e("AAA", "Fragment created")

        init()

        vm = ViewModelProvider(requireActivity()).get(FirstViewModel::class.java)

        vm.getResultUserStepsLive().observe(requireActivity(), {
            tvStepsTaken.text = it
        })

        vm.getResultProgressStateLive().observe(requireActivity(), {
            progressBar.progress = it
        })

        vm.getResultStartButtonStateLive().observe(requireActivity(), {
            startButton.isVisible = it
        })

        vm.getResultStopButtonStateLive().observe(requireActivity(), {
            stopButton.isVisible = it
        })

        vm.getResultResumeButtonStateLive().observe(requireActivity(), {
            resumeButton.isVisible = it
        })

        vm.getResultResetButtonStateLive().observe(requireActivity(), {
            resetButton.isVisible = it
        })

        vm.getResultUserKmLive().observe(requireActivity(), {
            kmTextView.text = it
        })

        vm.getResultTimeLive().observe(requireActivity(), {
            timeTextView.text = it
        })

        vm.getResultUserKkalLive().observe(requireActivity(), {
            kkalTextView.text = it
        })

        return rootView
    }

    private fun init() {
        totalMaxStepsTv = rootView.findViewById(R.id.total_max_steps_tv)
        totalMaxStepsTv.text = ("/"+getUserMaxSteps())

        stepsToChangeLinearLayout = rootView.findViewById(R.id.steps_to_change_linear_layout)
        stepsToChangeLinearLayout.setOnClickListener {
            val dialog = DialogFragmentSteps()
            dialog.show(childFragmentManager, "dialog_fragment_steps")
        }

        startButton = rootView.findViewById(R.id.fragment_first_start_button)
        stopButton = rootView.findViewById(R.id.fragment_first_stop_button)
        resumeButton = rootView.findViewById(R.id.fragment_first_resume_button)
        resetButton = rootView.findViewById(R.id.fragment_first_reset_button)

        kmTextView =rootView.findViewById(R.id.road_km_num_text_view)
        kkalTextView =rootView.findViewById(R.id.road_burnt_kal_num_text_view)
        timeTextView =rootView.findViewById(R.id.road_time_num_text_view)

        tvStepsTaken = rootView.findViewById(R.id.tv_steps_taken)

        progressBar = rootView.findViewById(R.id.circular_progressbar)
        progressBar.progressMax = getUserMaxSteps().toFloat()


        locationManager = rootView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        myLocListener = MyLocListener(this)
        registerPermissionListener()
        checkPermission()

        startButton.setOnClickListener {
            startButtonClicked()
            vm.registerSensorManager()
            vm.startStopTimer()
        }


        stopButton.setOnClickListener {
            stopButtonClicked()
            vm.unRegisterSensorManager()
            vm.startStopTimer()
        }

        resumeButton.setOnClickListener {
            resumeButtonClicked()
            vm.registerSensorManager()
            vm.startStopTimer()
        }

        resetButton.setOnClickListener {
            resetButtonClicked()
            vm.unRegisterSensorManager()

            vm.resetUserSteps()
            vm.resetLastSteps()
            vm.resetProgressState()
            vm.resetUserKm()
            vm.resetResultUserKkalLive()

            vm.resetTimer()
        }
    }

    private fun checkPermission() {
        when {
            checkSelfPermission(rootView.context, ACCESS_FINE_LOCATION)
                    == PERMISSION_GRANTED ->{
//                        Toast.makeText(rootView.context, "ACCESS_FINE_LOCATION run", Toast.LENGTH_LONG).show()
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5,3F,myLocListener)
                    }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
//                Toast.makeText(rootView.context, "we need your permission", Toast.LENGTH_LONG).show()
                pLauncher.launch(ACCESS_FINE_LOCATION)
            }

            else -> {
                pLauncher.launch(ACCESS_FINE_LOCATION)

            }
        }
    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            if (it){
//                Toast.makeText(rootView.context, "ACCESS_FINE_LOCATION run", Toast.LENGTH_LONG).show()
                checkPermission()
            } else {
                Toast.makeText(rootView.context, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onLocationChanged(loc: Location) {
//        if (loc.hasSpeed() && lastLocation != null) {
//
//            distance += lastLocation!!.distanceTo(loc).toInt()
//
//        }
//
//        lastLocation = loc
//        tvStepsTaken.text = distance.toInt().toString()
//        progressBar.apply {
//            setProgressWithAnimation(distance)
//        }
    }

    private fun startButtonClicked() {
       vm.resetResultStartButtonStateLive()
    }

    private fun stopButtonClicked() {
        vm.resetResultStopButtonStateLive()
    }

    private fun resumeButtonClicked() {
        vm.resetResultResumeButtonStateLive()
    }

    private fun resetButtonClicked() {
        vm.resetResultResetButtonStateLive()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun sendInput(input: String) {
        progressBar.progressMax = input.toFloat()
        totalMaxStepsTv.text = ("/$input")
        saveUserMaxSteps(input)
    }

    private fun saveUserMaxSteps(maxSteps: String?) {
        val sharedPref = rootView.context.getSharedPreferences(MY_APP_USER_ACTIVITY, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(USER_MAX_STEPS, maxSteps)
        editor.apply()
    }

    private fun getUserMaxSteps(): String {
        val sharedPreferences: SharedPreferences = rootView.context.getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_MAX_STEPS, "2500") ?: "2500"
    }
}
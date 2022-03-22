package com.example.map_gps

import android.Manifest.permission.*
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.mikhaellopez.circularprogressbar.CircularProgressBar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstFragment : Fragment(), SensorEventListener, LocListenerInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rootView: View
    private lateinit var pLauncher: ActivityResultLauncher<String>


    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null

    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? =null
    private lateinit var myLocListener: MyLocListener
    private var distance: Float = 0f

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    private lateinit var tvStepsTaken: TextView
    private lateinit var progressBar: CircularProgressBar

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resumeButton: Button
    private lateinit var resetButton: Button

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

        init()


//        loadData()
//        resetSteps()
//        sensorManager = rootView.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return rootView
    }

    private fun init() {
        startButton = rootView.findViewById(R.id.fragment_first_start_button)
        stopButton = rootView.findViewById(R.id.fragment_first_stop_button)
        resumeButton = rootView.findViewById(R.id.fragment_first_resume_button)
        resetButton = rootView.findViewById(R.id.fragment_first_reset_button)

        tvStepsTaken = rootView.findViewById(R.id.tv_steps_taken)
        progressBar = rootView.findViewById(R.id.circular_progressbar)

        locationManager = rootView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = rootView.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        myLocListener = MyLocListener(this)
        registerPermissionListener()
        checkPermission()

        startButton.setOnClickListener {
            startButtonClicked()
        }

        stopButton.setOnClickListener {
            stopButtonClicked()
        }

        resumeButton.setOnClickListener {
            resumeButtonClicked()
        }

        resetButton.setOnClickListener {
            resetButtonClicked()
        }

    }

    private fun checkPermission() {
        when {
            checkSelfPermission(rootView.context, ACCESS_FINE_LOCATION)
                    == PERMISSION_GRANTED ->{
                        Toast.makeText(rootView.context, "ACCESS_FINE_LOCATION run", Toast.LENGTH_LONG).show()
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,1F,myLocListener)
                    }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                Toast.makeText(rootView.context, "we need your permission", Toast.LENGTH_LONG).show()
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
        if (loc.hasSpeed() && lastLocation != null) {

            distance += lastLocation!!.distanceTo(loc).toInt()

        }

        lastLocation = loc
        tvStepsTaken.text = distance.toInt().toString()
        progressBar.apply {
            setProgressWithAnimation(distance)
        }

    }

    private fun startButtonClicked() {
        resumeButton.visibility = View.GONE
        resetButton.visibility = View.GONE
        startButton.visibility = View.GONE
        stopButton.visibility = View.VISIBLE
    }

    private fun stopButtonClicked() {
        resumeButton.visibility = View.VISIBLE
        resetButton.visibility = View.VISIBLE
        startButton.visibility = View.GONE
        stopButton.visibility = View.GONE
    }

    private fun resumeButtonClicked() {
        resumeButton.visibility = View.GONE
        resetButton.visibility = View.GONE
        startButton.visibility = View.GONE
        stopButton.visibility = View.VISIBLE
    }

    private fun resetButtonClicked() {
        resumeButton.visibility = View.GONE
        resetButton.visibility = View.GONE
        startButton.visibility = View.VISIBLE
        stopButton.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
//        running = true
////        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//
//        if (stepSensor == null) {
//            Toast.makeText(rootView.context, "no sensor detected on this device", Toast.LENGTH_SHORT).show()
//        } else {
//            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
//        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
//            val sensor = event?.sensor
//            val values: FloatArray = event!!.values
//            var value = -1
//            if (values.size>0) {
//                value = values[0].toInt()
//            }
//
//            if (sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
//                steps++
//            }


//            totalSteps = event!!.values[0]
//            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
//            tvStepsTaken.text = ("$currentSteps")
//
//            progressBar.apply {
//                setProgressWithAnimation(currentSteps.toFloat())
//            }
        }
    }


//    private fun resetSteps() {
//        tvStepsTaken.setOnClickListener {
//            Toast.makeText(rootView.context, "Long tap to clear", Toast.LENGTH_SHORT).show()
//        }
//
//        tvStepsTaken.setOnLongClickListener {
//            previousTotalSteps = totalSteps
//            tvStepsTaken.text = 0.toString()
//            saveData()
//
//            true
//        }
//    }

//    private fun saveData() {
//        val sf = rootView.context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
//        val editor: SharedPreferences.Editor = sf.edit()
//
//
//        editor.putFloat("key1", previousTotalSteps)
//        editor.apply()
//    }
//
//    private fun loadData() {
//        val sf = rootView.context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
//        val savedNumber = sf.getFloat("key1", 0f)
//        Log.d("firstFragment","$savedNumber")
//        previousTotalSteps = savedNumber
//
//    }
}
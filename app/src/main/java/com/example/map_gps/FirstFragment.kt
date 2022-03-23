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
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentManager
import com.example.map_gps.listener.StepListener
import com.example.map_gps.utils.StepDetector
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.lang.Math.ceil
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstFragment : Fragment(), SensorEventListener, LocListenerInterface, StepListener, DialogFragmentSteps.OnInputSelected {
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
//    private lateinit var timeWalkTextView: TextView
    private lateinit var chronometer: Chronometer
    private var running = false
    private var pauseOffSet: Long = 0

    //PEDOMETER
    private var simpleStepDetector: StepDetector? = null
    private var sensorManager: SensorManager? = null
    private var numSteps: Int = 0
    private var previousSteps: Int = 0

    private lateinit var stepsToChangeLinearLayout: LinearLayout
    private lateinit var totalMaxStepsTv: TextView

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector!!.updateAccelerometer(event.timestamp, event.values[0], event.values[1], event.values[2])
        }
    }

    override fun step(timeNs: Long) {
        numSteps++
        previousSteps++

        if (previousSteps==10) {

            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING

            val kmTextViewValue = kmTextView.text.toString().toFloat()

            val calculatedKm = kmTextViewValue + 0.01
            var formattedCalculatedKm =  df.format(calculatedKm)
            formattedCalculatedKm = formattedCalculatedKm.replace(",", ".")

            kmTextView.text = formattedCalculatedKm

            Log.d("km_v", kmTextView.text.toString())

            previousSteps = 0
        }

        tvStepsTaken.text = numSteps.toString()
        progressBar.apply {
            setProgressWithAnimation(numSteps.toFloat())
        }
    }

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


        return rootView
    }

    private fun init() {
        totalMaxStepsTv = rootView.findViewById(R.id.total_max_steps_tv)
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
//        timeWalkTextView =rootView.findViewById(R.id.road_time_num_text_view)
        chronometer = rootView.findViewById(R.id.road_time_chronometer)
        chronometer.format = "%s"
        chronometer.base = SystemClock.elapsedRealtime()

        tvStepsTaken = rootView.findViewById(R.id.tv_steps_taken)
        progressBar = rootView.findViewById(R.id.circular_progressbar)

        // Get an instance of the SensorManager
        sensorManager = rootView.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        simpleStepDetector = StepDetector()
        simpleStepDetector!!.registerListener(this)

        locationManager = rootView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        myLocListener = MyLocListener(this)
        registerPermissionListener()
        checkPermission()

        startButton.setOnClickListener {
            startButtonClicked()
            numSteps = 0
            sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)

            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffSet
                chronometer.start()
                running = true
            }
        }

        stopButton.setOnClickListener {
            stopButtonClicked()
            sensorManager!!.unregisterListener(this)

            if (running) {
                chronometer.stop()
                pauseOffSet = SystemClock.elapsedRealtime() - chronometer.base
                running = false
            }
        }

        resumeButton.setOnClickListener {
            resumeButtonClicked()
            sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST)

            if (!running) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffSet
                chronometer.start()
                running = true
            }
        }

        resetButton.setOnClickListener {
            resetButtonClicked()
            numSteps = 0
            tvStepsTaken.text = "0"
            progressBar.apply {
                setProgressWithAnimation(0f)
            }
            kmTextView.text = ("0.00")
            sensorManager!!.unregisterListener(this)

            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffSet = 0
        }

    }

    private fun checkPermission() {
        when {
            checkSelfPermission(rootView.context, ACCESS_FINE_LOCATION)
                    == PERMISSION_GRANTED ->{
                        Toast.makeText(rootView.context, "ACCESS_FINE_LOCATION run", Toast.LENGTH_LONG).show()
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5,3F,myLocListener)
                    }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                Toast.makeText(rootView.context, "we need your permission", Toast.LENGTH_LONG).show()
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

    override fun sendInput(input: String) {
        progressBar.progressMax = input.toFloat()
        totalMaxStepsTv.text = ("/$input")
    }
}
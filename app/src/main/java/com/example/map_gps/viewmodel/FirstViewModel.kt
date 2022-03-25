package com.example.map_gps.viewmodel

import android.app.Application
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.map_gps.MY_APP_USER_ACTIVITY
import com.example.map_gps.USER_HEIGHT
import com.example.map_gps.USER_WEIGHT
import com.example.map_gps.listener.StepListener
import com.example.map_gps.services.TimerService
import com.example.map_gps.utils.StepDetector
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class FirstViewModel(application: Application): AndroidViewModel(application), StepListener, SensorEventListener {

    private val resultUserStepsLive = MutableLiveData<String>()
    private val resultLastStepsLive = MutableLiveData<Int>()
    private val resultProgressStateLive = MutableLiveData<Float>()

    private val resultUserKmLive = MutableLiveData<String>()
    private val resultUserKkalLive = MutableLiveData<String>()
    private val resultTimeLive = MutableLiveData<String>()

    private val resultStartButtonStateLive = MutableLiveData<Boolean>()
    private val resultStopButtonStateLive = MutableLiveData<Boolean>()
    private val resultResumeButtonStateLive = MutableLiveData<Boolean>()
    private val resultResetButtonStateLive = MutableLiveData<Boolean>()

    private var simpleStepDetector: StepDetector? = null
    private var sensorManager: SensorManager? = null
    private var serviceIntent: Intent
    private var timeStarted = false
    private var time = 0.0

    private var numSteps = 0
    private var previousSteps = 0
    private var constVar1 = 0.035F
    private var constVar2 = 0.029F
    private var minRes = 0
    private var tempResKal = 0F

    init {
        Log.e("AAA", "VM created")

        val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
                resultTimeLive.value = getTimeStringFromDouble(time)
            }
        }

        serviceIntent = Intent(application, TimerService::class.java)
        application.registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        simpleStepDetector = StepDetector()
        simpleStepDetector!!.registerListener(this)
    }

    override fun onCleared() {
        Log.e("AAA", "VM cleared")
        super.onCleared()
    }

    //TIME-LIVE
    fun getResultTimeLive(): LiveData<String> {
        return resultTimeLive
    }

    //USER-KKAL-LIVE
    fun getResultUserKkalLive(): LiveData<String> {
        return resultUserKkalLive
    }

    private fun calculateKkalLive(steps: Int, timeF: Double) {
        val resultInt = timeF.toInt()

        Log.d("kkal_step",timeF.toString())
        Log.d("kkal_step",getSavedWeight())
        Log.d("kkal_step",getSavedHeight())

        val minutes = resultInt % 86400 % 3600 / 60

        if (minutes==0) {

        } else {
            if (minutes <= minRes || steps==0) {

            } else {

                minRes++

                Log.d("kkal_step", minRes.toString())
                Log.d("kkal_step", steps.toString())

                val v = ((steps / minRes) / 1000 * 60).toFloat()
                Log.d("kkal_step", v.toString())

                val res = (constVar1 * getSavedWeight().toFloat() + (v * v / (getSavedHeight().toFloat() / 100)) * constVar2 * getSavedWeight().toFloat())
                tempResKal+=res
                Log.d("kkal_step", res.toString())

                val df = DecimalFormat("#.#")

                resultUserKkalLive.value = df.format(tempResKal).replace(",", ".")
                Log.d("kkal_step", resultUserKkalLive.value.toString())

            }
        }
    }

    fun resetResultUserKkalLive() {
        resultUserKkalLive.value = "0.0"
        minRes = 0
        tempResKal = 0F
    }

    //USER-STEPS-LIVE
    fun getResultUserStepsLive(): LiveData<String> {
        return resultUserStepsLive
    }

    fun saveUserSteps(steps: String) {
        resultUserStepsLive.value = steps
    }

    fun resetUserSteps() {
        resultUserStepsLive.value = "0"
    }

    //USER-LAST-STEP
    fun saveLastSteps(steps: Int) {
        resultLastStepsLive.value = steps
    }

    fun resetLastSteps() {
        resultLastStepsLive.value = 0
        numSteps = 0
    }

    //USER-STEP-PROGRESS
    fun getResultProgressStateLive(): LiveData<Float> {
        return resultProgressStateLive
    }

    fun saveProgressState(state: Float) {
        resultProgressStateLive.value = state
    }

    fun resetProgressState() {
        resultProgressStateLive.value = 0F
    }

    //USER-BUTTON-STATE
    fun getResultStartButtonStateLive(): LiveData<Boolean> {
        return resultStartButtonStateLive
    }

    fun getResultStopButtonStateLive(): LiveData<Boolean> {
        return resultStopButtonStateLive
    }

    fun getResultResumeButtonStateLive(): LiveData<Boolean> {
        return resultResumeButtonStateLive
    }

    fun getResultResetButtonStateLive(): LiveData<Boolean> {
        return resultResetButtonStateLive
    }

    fun resetResultStartButtonStateLive() {
        resultStartButtonStateLive.value = false
        resultStopButtonStateLive.value = true
        resultResumeButtonStateLive.value = false
        resultResetButtonStateLive.value = false
    }

    fun resetResultStopButtonStateLive() {
        resultStartButtonStateLive.value = false
        resultStopButtonStateLive.value = false
        resultResumeButtonStateLive.value = true
        resultResetButtonStateLive.value = true
    }

    fun resetResultResumeButtonStateLive() {
        resultStartButtonStateLive.value = false
        resultStopButtonStateLive.value = true
        resultResumeButtonStateLive.value = false
        resultResetButtonStateLive.value = false
    }

    fun resetResultResetButtonStateLive() {
        resultStartButtonStateLive.value = true
        resultStopButtonStateLive.value = false
        resultResumeButtonStateLive.value = false
        resultResetButtonStateLive.value = false
    }

    fun registerSensorManager() {
        sensorManager!!.registerListener(
            this,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    fun unRegisterSensorManager() {
        sensorManager!!.unregisterListener(this)
    }

    //USER-KM
    fun getResultUserKmLive(): LiveData<String> {
        return resultUserKmLive
    }

    fun saveUserKm(km: String) {
        resultUserKmLive.value = km
    }

    fun resetUserKm() {
        resultUserKmLive.value = "0.00"
    }


    override fun step(timeNs: Long) {
        numSteps++
        previousSteps++

        if (previousSteps==10) {

            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING

            var kmTextViewValue = resultUserKmLive.value?.toFloat()
            if (kmTextViewValue==null) {
                kmTextViewValue = 0.00f
            }

            var calculatedKm = kmTextViewValue.plus(0.01).toFloat()

            if (calculatedKm.toString().length>4) {

                if (calculatedKm.toString().length>4 && calculatedKm.toString()[4]=='0') {
                    calculatedKm = calculatedKm.toString().subSequence(0,4).toString().toFloat()
                    Log.d("km_v", calculatedKm.toString())
                }

                if (calculatedKm.toString().length>4 && calculatedKm.toString()[4]=='9') {
                    calculatedKm = df.format(calculatedKm).replace(",",".").toFloat()
                    Log.d("km_v", calculatedKm.toString())
                }
            }

            saveUserKm(calculatedKm.toString())

            previousSteps = 0
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector!!.updateAccelerometer(event.timestamp, event.values[0], event.values[1], event.values[2])
            saveUserSteps(numSteps.toString())
            saveLastSteps(numSteps)
            saveProgressState(numSteps.toFloat())
            calculateKkalLive(numSteps,time)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    fun resetTimer() {
        stopTimer()
        time = 0.0
        resultTimeLive.value = getTimeStringFromDouble(time)
    }

    fun startStopTimer() {
        if(timeStarted)
            stopTimer()
        else
            startTimer()
    }

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        getApplication<Application>().startService(serviceIntent)
        timeStarted = true
    }

    private fun stopTimer() {
        getApplication<Application>().stopService(serviceIntent)
        timeStarted = false
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format("%02d:%02d:%02d", hour, min, sec)

    private fun getSavedHeight(): String {
        val sharedPreferences: SharedPreferences = getApplication<Application>().getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_HEIGHT, "0") ?: "0"
    }

    private fun getSavedWeight(): String {
        val sharedPreferences: SharedPreferences = getApplication<Application>().getSharedPreferences(
            MY_APP_USER_ACTIVITY,
            Context.MODE_PRIVATE
        )

        return sharedPreferences.getString(USER_WEIGHT, "0") ?: "0"
    }

}
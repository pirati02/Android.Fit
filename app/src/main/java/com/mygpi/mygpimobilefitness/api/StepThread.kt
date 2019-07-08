package com.mygpi.mygpimobilefitness.api

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.mygpi.mygpimobilefitness.model.StepModel
import com.mygpi.mygpimobilefitness.today
import io.realm.Realm

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

import java.util.Date

class StepThread(private val context: Context) : Thread(), SensorEventListener, StepListener {

    private var sensorManager: SensorManager? = null
    private var accel: Sensor? = null
    private var stepDetector: StepDetector? = null
    private var numSteps: Long = 0
    private var lastSteps: Long = 0
    private var isRegister = false
    private var today: Date? = null

    init {
        initStepDetector()
    }

    override fun run() {
        if (!isRegister) {
            sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
            isRegister = true
        }
    }

    fun threadStop() {
        if (isRegister) {
            sensorManager?.unregisterListener(this)
            isRegister = false
        }
    }

    private fun initStepDetector() {
        stepDetector = StepDetector(this)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        today = Date().today()
        //val realm = Realm.getDefaultInstance()
        //val result = realm.where(StepModel::class.java)
        //        .equalTo("startDate", today)
        //        .findFirst()
        //numSteps =result?.numSteps ?: 0
        //realm.close()

        SessionManager.startSession(numSteps, false)
        //if (!EventBus.getDefault().isRegistered(this))
        //    EventBus.getDefault().register(this)
    }

    @Subscribe
    fun subscribeActivity(stopped: Boolean?) {
        //if (stopped!!)
        //EventBus.getDefault().post(numSteps)
        //else
        //    save(Date().today(), numSteps)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            stepDetector?.updateModel()
            stepDetector?.updateStep(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {

    }

    override fun step(num: Long) {
        if (!SessionManager.sessionExpired()) {
            SessionManager.update(num)
        } else
            SessionManager.startSession(num, true)

        if (today != Date().today()) {
            //save(today, numSteps)
            numSteps = 0
            lastSteps = 0
            today = Date().today()
        }
        numSteps += num
        EventBus.getDefault().post(num)
    }
}
package com.mygpi.mygpimobilefitness.api

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.mygpi.mygpimobilefitness.model.StepModel
import com.mygpi.mygpimobilefitness.model.StepTransaction
import com.mygpi.mygpimobilefitness.model.SuccessTransaction
import com.mygpi.mygpimobilefitness.today

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

import java.util.Date

import io.realm.Realm
import io.realm.RealmAsyncTask

class StepThread(private val context: Context) : Thread(), SensorEventListener, StepListener {

    private var sensorManager: SensorManager? = null
    private var accel: Sensor? = null
    private var stepDetector: StepDetector? = null
    private var numStpes: Long = 0
    private var lastStpes: Long = 0
    private var realmAsyncTask: RealmAsyncTask? = null
    private var isRegiter = false
    private var today: Date? = null

    init {
        initStepDetector()
    }

    override fun run() {
        if (!isRegiter) {
            sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
            isRegiter = true
        }
    }

    fun threadStop() {
        if (isRegiter) {
            sensorManager?.unregisterListener(this)
            isRegiter = false
        }
    }

    private fun initStepDetector() {
        today = Date().today()
        stepDetector = StepDetector(this)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val realm = Realm.getDefaultInstance()
        val result = realm.where(StepModel::class.java)
                .equalTo("date", today)
                .findFirst()

        lastStpes = result?.numSteps ?: 0
        step(lastStpes)
        realm.close()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    @Subscribe
    fun subscribeActivity(stopped: Boolean?) {
        if (stopped!!)
            EventBus.getDefault().post(numStpes)
        else
            save(Date().today(), numStpes)
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
        if (today != Date().today()) {
            save(today, numStpes)
            numStpes = 0
            lastStpes = 0
            today = Date().today()
        }
        numStpes += num
        EventBus.getDefault().post(numStpes)
        if (numStpes - lastStpes > 10) {
            lastStpes = numStpes
            save(today, numStpes)
        }

    }

    private fun save(date: Date?, num: Long) {
        val realm = Realm.getDefaultInstance()
        realmAsyncTask = realm.executeTransactionAsync(
                StepTransaction(date, num),
                SuccessTransaction(realmAsyncTask),
                Realm.Transaction.OnError { error -> error.printStackTrace() }
        )
        realm.close()
    }
}
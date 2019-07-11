package ge.dev.baqari.api.step

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ge.dev.baqari.dayOnly
import ge.dev.baqari.today
import io.realm.Realm

import org.greenrobot.eventbus.EventBus
import java.util.*

class StepThread(private val context: Context) : Thread(), SensorEventListener, StepListener {

    var onStep: ((step: Long) -> Unit)? = null
    private var sensorManager: SensorManager? = null
    private var accel: Sensor? = null
    private var stepDetector: StepDetector? = null
    private var numSteps: Long = 0
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
        today = Date().today()?.dayOnly()
        SessionManager.startSession(0, false)
        numSteps = BaseCalculator.currentSteps(Realm.getDefaultInstance()).toLong()
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

        if (today != Date().today()?.dayOnly()) {
            numSteps = 0
            today = Date().today()?.dayOnly()
        }
        numSteps += num
        onStep?.invoke(numSteps)
        EventBus.getDefault().post(numSteps)
    }

    fun invokeOnStep() {
        onStep?.invoke(numSteps)
    }
}
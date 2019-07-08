package com.mygpi.mygpimobilefitness.fundamentals

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mygpi.mygpimobilefitness.ApplicationImpl
import com.mygpi.mygpimobilefitness.R
import com.mygpi.mygpimobilefitness.model.StepModel
import com.mygpi.mygpimobilefitness.round
import com.mygpi.mygpimobilefitness.today
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.showSteps
import kotlinx.android.synthetic.main.activity_main_2.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class MainActivity : AppCompatActivity() {

    private var bus: EventBus? = null
    private var numSteps: Double = 0.0
        set(value) {
            field = value
            showSteps?.text = value.toLong().toString()
            circularProgress.setCurrentProgress((value / 100).toDouble())
            calculateKilometers()
        }
    private var kilometersCount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)
        bus = EventBus.getDefault()
        if (bus?.isRegistered(this) == false)
            bus?.register(this)
        val realm = Realm.getDefaultInstance()
        val stepResult = realm.where(StepModel::class.java)
                .equalTo("startDate", Date().today())
                .findAll()

        numSteps = (stepResult?.sum("numSteps") ?: 0.0).toDouble()
        //bus?.post(true)
        realm.close()
        startService()

        circularProgress.maxProgress = 100.0
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSteps(num: Long?) {
        numSteps += num?.toDouble()!!
    }

    private fun calculateKilometers() {
        kilometersCount += (numSteps / 100000)
        wentKilometersCount?.apply {
            text = kilometersCount.round(2).toString()
        }
    }

    private fun startService() {
        startService(Intent(this, StepService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        //bus?.post(false)
        if (bus?.isRegistered(this) == true)
            bus?.unregister(this)
    }
}

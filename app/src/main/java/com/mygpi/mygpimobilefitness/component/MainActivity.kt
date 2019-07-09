package com.mygpi.mygpimobilefitness.component

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mygpi.mygpimobilefitness.R
import com.mygpi.mygpimobilefitness.api.BaseCalculator
import com.mygpi.mygpimobilefitness.dayOnly
import com.mygpi.mygpimobilefitness.model.StepModel
import com.mygpi.mygpimobilefitness.round
import com.mygpi.mygpimobilefitness.today
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.showSteps
import kotlinx.android.synthetic.main.activity_main.*
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
            circularProgress.setCurrentProgress((value / 100))
            calculateKilometers()
            calculateTimes()
        }
    private var kilometersCount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bus = EventBus.getDefault()
        if (bus?.isRegistered(this) == false)
            bus?.register(this)
        val realm = Realm.getDefaultInstance()
        numSteps = BaseCalculator.currentSteps(realm)
        realm.close()
        startService()
        circularProgress.maxProgress = 100.0
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSteps(num: Long?) {
        numSteps += num?.toDouble()!!
    }

    private fun calculateKilometers() {
        kilometersCount += BaseCalculator.calculateKilometers(numSteps.toLong())
        wentKilometersCount?.apply {
            text = kilometersCount.round(2).toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTimes() {
        //val string = BaseCalculator.calculateTimes(getString(R.string.today_at_11_41_am))
    }

    private fun startService() {
        startService(Intent(this, StepService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bus?.isRegistered(this) == true)
            bus?.unregister(this)
    }
}

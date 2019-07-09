package com.mygpi.mygpimobilefitness.component

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mygpi.mygpimobilefitness.R
import com.mygpi.mygpimobilefitness.dayOnly
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
import java.util.concurrent.TimeUnit


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
        setContentView(R.layout.activity_main_2)
        bus = EventBus.getDefault()
        if (bus?.isRegistered(this) == false)
            bus?.register(this)
        val realm = Realm.getDefaultInstance()
        val stepResult = realm.where(StepModel::class.java)
                .equalTo("startDate", Date().today()?.dayOnly())
                .findAll()

        numSteps = (stepResult?.sum("numSteps") ?: 0.0).toDouble()
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

    @SuppressLint("SetTextI18n")
    private fun calculateTimes() {
        //val realm = Realm.getDefaultInstance()
        //val stepResult = realm.where(StepModel::class.java)
        //        .equalTo("startDate", Date().today()?.dayOnly())
        //        .findAll()
//
        //val stepTime = stepResult.map { it.endDateTime?.time!! - it.startDateTime?.time!! }.sum()
        //val days = TimeUnit.MILLISECONDS.toDays(stepTime)
        //val hours = TimeUnit.MILLISECONDS.toHours(stepTime)
        //val minutes = TimeUnit.MILLISECONDS.toMinutes(stepTime)
        //val formattedString = StringBuffer()
        //formattedString.append(getString(R.string.today_at_11_41_am))
        //if (days > 1) {
        //    formattedString.append("0$days")
        //    formattedString.append(":")
        //}
        //formattedString.append(if (hours > 9) hours.toString() else "0$hours")
        //formattedString.append(":")
        //formattedString.append(if (minutes > 9) minutes.toString() else "0$minutes")
//
        //wasASleep?.apply {
        //    text = formattedString.toString()
        //}
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
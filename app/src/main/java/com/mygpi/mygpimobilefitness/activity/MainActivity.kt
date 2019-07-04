package com.mygpi.mygpimobilefitness.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mygpi.mygpimobilefitness.ApplicationImpl
import com.mygpi.mygpimobilefitness.R
import com.mygpi.mygpimobilefitness.fundamentals.StepService
import com.mygpi.mygpimobilefitness.fundamentals.StepsProvider
import com.mygpi.mygpimobilefitness.model.StepModel
import com.mygpi.mygpimobilefitness.today
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class MainActivity : AppCompatActivity() {

    private var bus: EventBus? = null
    private var numSteps: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bus = EventBus.getDefault()
        bus?.register(this)
        val realm = Realm.getDefaultInstance()
        val stepResult = realm.where(StepModel::class.java)
                .equalTo("date", Date().today())
                .findFirst()

        numSteps = stepResult?.numSteps ?: 0
        bus?.post(true)
        updateShowSteps()
        realm.close()
        startService()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSteps(num: Long?) {
        numSteps = num!!
        updateShowSteps()
    }

    private fun updateShowSteps() {
        val text = numSteps.toString()

        if (numSteps >= 10000) {
            notifyIsUpToStandard("Great, you have more than 10,000 steps today.")
        } else {
            if (numSteps >= 5000)
                notifyIsUpToStandard("Come on, you have already walked away and you have reached 10,000 steps.")
            else
                notifyIsUpToStandard("You didn’t walk very much today, go out and go out.")
        }
        showSteps?.text = text
    }

    private fun notifyIsUpToStandard(msg: String) {
        val app = ApplicationImpl.instance
        if (app?.isShowToast == false) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            app.isShowToast = true
        }
    }

    fun startService() {
        startService(Intent(this, StepService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.post(false)
        if (bus?.isRegistered(this) == true)
            bus?.unregister(this)
    }
}

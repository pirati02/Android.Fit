package ge.dev.baqari.fit.component

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ge.dev.baqari.fit.R
import ge.dev.baqari.fit.api.step.BaseCalculator
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private var bus: EventBus? = null
    private var numSteps: Double = 0.0
        set(value) {
            field = value
            showSteps?.text = value.toLong().toString()
            //progressBar.progress = (value * 100 / 10000).roundToInt()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bus = EventBus.getDefault()
        if (bus?.isRegistered(this) == false)
            bus?.register(this)
        numSteps = BaseCalculator.currentSteps(Realm.getDefaultInstance())
        startService()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSteps(num: Long?) {
        numSteps = num?.toDouble()!!
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

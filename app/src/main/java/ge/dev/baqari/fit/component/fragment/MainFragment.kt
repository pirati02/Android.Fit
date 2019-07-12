package ge.dev.baqari.fit.component.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ge.dev.baqari.fit.R
import ge.dev.baqari.fit.api.step.BaseCalculator
import ge.dev.baqari.fit.component.MainActivity
import ge.dev.baqari.fit.component.StepService
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainFragment : Fragment() {

    private var bus: EventBus? = null
    private var numSteps: Double = 0.0
        set(value) {
            field = value
            showSteps?.text = value.toLong().toString()
            //progressBar.progress = (value * 100 / 10000).roundToInt()
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bus = EventBus.getDefault()
        if (bus?.isRegistered(this) == false)
            bus?.register(this)
        numSteps = BaseCalculator.currentSteps(Realm.getDefaultInstance())
        startService()

        expandMoreLayout.setOnClickListener {
            (activity as MainActivity).openDetails()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSteps(num: Long?) {
        numSteps = num?.toDouble()!!
    }

    private fun startService() {
        activity?.startService(Intent(activity, StepService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bus?.isRegistered(this) == true)
            bus?.unregister(this)
    }
}

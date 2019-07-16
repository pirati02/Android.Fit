package ge.dev.baqari.fit.component.fragment

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ge.dev.baqari.fit.api.BaseCalculator
import ge.dev.baqari.fit.component.MainActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_main.*
import androidx.annotation.RequiresApi
import ge.dev.baqari.fit.R
import ge.dev.baqari.fit.component.StepService
import ge.dev.baqari.fit.utils.LocalKeys
import ge.dev.baqari.fit.utils.get
import ge.dev.baqari.fit.utils.set
import ge.dev.baqari.fit.utils.storage
import kotlin.math.roundToInt


class MainFragment : Fragment() {

    private val BATTERY_INGORE_REQUEST = 2048
    private var numSteps: Double = 0.0
        set(value) {
            field = value
            showSteps?.text = value.toLong().toString()
            progressBar.progress = (value * 100 / 10000).roundToInt()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            numSteps = BaseCalculator.currentSteps(Realm.getDefaultInstance())
            expandMoreLayout.setOnClickListener {
                (activity as MainActivity).openDetails()
            }
            startService()
            (activity as MainActivity?)?.onStep = {
                numSteps = it?.toDouble()!!
            }
            (activity as MainActivity?)?.onNotificationStopped = {
                notificationOffImage.setImageResource(if (it == false) R.drawable.ic_notification_on else R.drawable.ic_notification_off)
            }
            checkBatteryOptimization()
            enableAutoStart()
            checkNotification()
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    private fun checkNotification() {
        val notificationEnabled: Boolean? = storage()[LocalKeys.notificationEnabledKey, true]
        notificationOffImage.setImageResource(if (notificationEnabled == false || notificationEnabled == null) R.drawable.ic_notification_on else R.drawable.ic_notification_off)
        notificationOffImage.setOnClickListener {
            val enabled: Boolean? = storage()[LocalKeys.notificationEnabledKey, true]
            if (!enabled!!) {
                startService()
                notificationOffImage.setImageResource(R.drawable.ic_notification_off)
                storage()[LocalKeys.notificationEnabledKey] = true
            } else {
                stopService()
                notificationOffImage.setImageResource(R.drawable.ic_notification_on)
                storage()[LocalKeys.notificationEnabledKey] = false
            }
        }
    }

    @SuppressLint("BatteryLife")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBatteryOptimization() {
        try {
            val powerManager = activity?.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(activity?.packageName)
            batteryOptimizationImage.visibility = if (!isIgnoringBatteryOptimizations) VISIBLE else GONE

            batteryOptimizationImage.setOnClickListener {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:${activity?.packageName}")
                startActivityForResult(intent, BATTERY_INGORE_REQUEST)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun enableAutoStart() {
        try {
            val intent = Intent()
            val manufacturer = android.os.Build.MANUFACTURER
            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
                intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            }

            val list = activity?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            autoStartImage.visibility = if (list?.size!! > 0) VISIBLE else GONE
            autoStartImage.setOnClickListener {
                startActivity(intent)
            }
        } catch (e: Exception) {
            autoStartImage.visibility = GONE
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BATTERY_INGORE_REQUEST) {
            val powerManager = activity?.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(activity?.packageName)
            batteryOptimizationImage.visibility = if (!isIgnoringBatteryOptimizations) VISIBLE else GONE
        }
    }

    private fun startService() {
        activity?.startService(Intent(activity, StepService::class.java).apply {
            action = StepService.START
        })
    }

    private fun stopService() {
        activity?.startService(Intent(activity, StepService::class.java).apply {
            action = StepService.STOP_REMOTELY
        })
    }
}

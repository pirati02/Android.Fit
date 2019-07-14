package ge.dev.baqari.fit.component.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Context.BIND_IMPORTANT
import android.content.Intent
import android.content.ServiceConnection
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
import ge.dev.baqari.fit.component.StepService
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import androidx.core.content.ContextCompat.startForegroundService
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import ge.dev.baqari.fit.R
import ge.dev.baqari.fit.get
import ge.dev.baqari.fit.set
import ge.dev.baqari.fit.storage
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
        expandMoreLayout.setOnClickListener {
            (activity as MainActivity).openDetails()
        }
        startService()
        (activity as MainActivity?)?.onStep = {
            numSteps = it?.toDouble()!!
        }

        numSteps = BaseCalculator.currentSteps(Realm.getDefaultInstance())

        checkBatteryOptimization()
        enableAutoStart()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBatteryOptimization() {
        try {
            val powerManager = activity?.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(activity?.packageName)
            batteryOptimizationImage.visibility = if (isIgnoringBatteryOptimizations) VISIBLE else GONE

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
            batteryOptimizationImage.visibility = if (isIgnoringBatteryOptimizations) VISIBLE else GONE
        }
    }

    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, StepService::class.java))
        } else {
            activity?.startService(Intent(activity, StepService::class.java))
        }
    }
}

package ge.dev.baqari.myfit.component

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.eftimoff.viewpagertransformers.StackTransformer
import ge.dev.baqari.myfit.R
import ge.dev.baqari.myfit.component.fragment.MainFragment
import ge.dev.baqari.myfit.utils.dayOnly
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class MainActivity : AppCompatActivity() {

    private var bus: EventBus? = null
    var onStep: ((step: Long?) -> Unit)? = null
    var onNotificationStopped: ((disabled: Boolean?) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bus = EventBus.getDefault()
        bus?.register(this)
        viewPager?.adapter = ViewPagerAdapter(supportFragmentManager, mutableListOf(MainFragment()))
        viewPager.setPageTransformer(true, StackTransformer())
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.post(false)
        if (bus?.isRegistered(this) == true)
            bus?.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSteps(num: Long?) {
        onStep?.invoke(num)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun disableNotification(notificationDisabled: Boolean?) {
        onNotificationStopped?.invoke(notificationDisabled)
    }

    fun openDetails() {
        viewPager.currentItem = 1
    }
}

package ge.dev.baqari.fit.component

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eftimoff.viewpagertransformers.StackTransformer
import ge.dev.baqari.fit.R
import ge.dev.baqari.fit.component.fragment.DetailedFragment
import ge.dev.baqari.fit.component.fragment.MainFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewPager?.adapter = ViewPagerAdapter(supportFragmentManager, mutableListOf(MainFragment()))
        viewPager.setPageTransformer(true, StackTransformer())
    }

    fun openDetails() {
        viewPager.currentItem = 1
    }

    fun backToMain() {
        viewPager.currentItem = 0
    }
}

package by.bsu.newsly.ui.mainActivity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import by.bsu.newsly.R
import by.bsu.newsly.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().apply {
            setOnExitAnimationListener { screen ->
                val zoomX = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_X, 0.7f, 0f)
                val zoomY = ObjectAnimator.ofFloat(screen.iconView, View.SCALE_Y, 0.7f, 0f)
                listOf(zoomX, zoomY).forEach { anim ->
                    anim.interpolator = OvershootInterpolator()
                    anim.duration = 500L
                    anim.doOnEnd { screen.remove() }
                    anim.start()
                }
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment_activity_main)
            .navigateUp() || super.onSupportNavigateUp()
}
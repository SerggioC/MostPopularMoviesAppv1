package com.sergiocruz.mostpopularmovies.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import com.sergiocruz.mostpopularmovies.R
import kotlinx.android.synthetic.main.activity_about.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class AboutActivity : AppCompatActivity() {
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        slideInViewsOnPreDraw(about_root, arrayOf(app_image, content, git_repo))

        supportActionBar?.hide()

        mHidePart2Runnable.run()

        about_button.setOnClickListener { finish() }

    }

    private fun slideInViewsOnPreDraw(parent: View, viewsToAnimate: Array<View>) {
        val listener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val leftAnimation = AnimationUtils.loadAnimation(parent.context, R.anim.slide_in_left)
                val rightAnimation = AnimationUtils.loadAnimation(parent.context, R.anim.slide_in_right)

                val length = viewsToAnimate.size
                for (i in 0 until length) {
                    val animation = if (i % 2 == 0) rightAnimation else leftAnimation /* alternate animations */
                    animation.startOffset = (100 * i).toLong()
                    viewsToAnimate[i].animation = animation
                    viewsToAnimate[i].startAnimation(animation)
                }

                parent.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        }
        parent.viewTreeObserver.addOnPreDrawListener(listener)

    }



}


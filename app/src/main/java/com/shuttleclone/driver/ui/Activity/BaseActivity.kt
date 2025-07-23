package com.shuttleclone.driver.ui.Activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.shuttleclone.driver.R
import com.shuttleclone.driver.Util.CustomToast

open class BaseActivity : AppCompatActivity() {
    /*variable declaration*/
    var mToast: CustomToast? = null

    /*show custom toast*/
    fun showToast(aContent: String?) {
        mToast!!.setCustomView(aContent)
        mToast!!.duration = Toast.LENGTH_LONG
        mToast!!.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mToast = CustomToast(this)
        setStatusBarGradiant(this)
    }

    /*set type face*/
    fun setTypeFace(tv: EditText) {
        val face = Typeface.createFromAsset(assets, "font/googlesansregular.ttf")
        tv.typeface = face
    }

    open fun loadFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit()
            fadeOutIn(findViewById(R.id.frame_container))
        }
    }


    /*Fade out in*/
    fun fadeOutIn(view: View) {
        view.alpha = 0f
        val animatorSet = AnimatorSet()
        val animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 0.5f, 1f)
        ObjectAnimator.ofFloat(view, "alpha", 0f).start()
        animatorAlpha.duration = 300
        animatorSet.play(animatorAlpha)
        animatorSet.start()
    }

    /*intent*/
    fun startActivity(aClass: Class<*>?) {
        startActivity(Intent(this, aClass))
    }

    /*show view*/
    fun showView(view: View) {
        view.visibility = View.VISIBLE
    }

    /*hide view*/
    fun hideView(view: View) {
        view.visibility = View.GONE
    }

    /*invisible view*/
    fun invisibleView(view: View) {
        view.visibility = View.INVISIBLE
    }

    /* set notification */
    fun SetNotificationImage(view: ImageView?) {
        Glide.with(this).load(R.raw.gif_bell).into(view!!)
    }

    /* load image */
    fun setImage(i: Int, imageView: ImageView?) {
        Glide.with(this).load(i).into(imageView!!)
    }

    companion object {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun setStatusBarGradiant(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                val background = activity.resources.getDrawable(R.drawable.bg_toolbar)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = activity.resources.getColor(android.R.color.transparent)
                window.setBackgroundDrawable(background)
            }
        }
    }
}
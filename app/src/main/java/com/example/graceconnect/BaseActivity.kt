package com.example.graceconnect

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/** Shared behaviour for every inner screen: colored header with a back button, insets, and intents. */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableBrandEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    protected fun setupScreen(title: String, @ColorRes headerColor: Int) {
        val color = ContextCompat.getColor(this, headerColor)
        val header = findViewById<View>(R.id.header)
        header.background = headerGradient(color)
        val titleView = findViewById<TextView>(R.id.screen_title)
        titleView.text = title
        ViewCompat.setAccessibilityHeading(titleView, true)
        // Go through the dispatcher so screens that intercept back (e.g. unsaved edits) also catch the header button.
        findViewById<View>(R.id.backButton).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // On wide screens, line the header's back button and title up with the centered content column.
        val content = findViewById<MaxWidthLinearLayout>(R.id.content)
        findViewById<MaxWidthLinearLayout>(R.id.header_content).contentMaxWidth = content.contentMaxWidth

        // The header scrolls away, so a solid strip keeps content from showing under the status bar.
        val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup
        val statusScrim = View(this).apply { setBackgroundColor(color) }
        root.addView(statusScrim, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))

        // Let the header color extend behind the status bar; pad everything else away from system bars.
        val headerTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            header.updatePadding(top = headerTop + bars.top)
            statusScrim.updateLayoutParams { height = bars.top }
            insets
        }
    }

    /** Top-to-bottom gradient from the screen color to a deeper shade, rounded at the bottom. Its top edge
     *  matches the solid status bar strip exactly. */
    private fun headerGradient(color: Int): GradientDrawable {
        val radius = resources.getDimension(R.dimen.header_corner_radius)
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color, ColorUtils.blendARGB(color, Color.BLACK, 0.28f))
        ).apply { cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, radius, radius, radius, radius) }
    }

    protected fun openUrl(url: String) = launch(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    protected fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        launch(Intent.createChooser(send, getString(R.string.share)))
    }

    protected fun launch(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(getString(R.string.no_app_found))
        }
    }

    protected fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/** Draw behind the system bars on every Android version, with white status bar icons over the colored headers. */
fun AppCompatActivity.enableBrandEdgeToEdge() {
    enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
}

package com.example.graceconnect

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/** Shared behaviour for every inner screen: colored header with a back button, insets, and intents. */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableBrandEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    protected fun setupScreen(title: String, @ColorRes headerColor: Int) {
        val header = findViewById<View>(R.id.header)
        header.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, headerColor))
        findViewById<TextView>(R.id.screen_title).text = title
        findViewById<View>(R.id.backButton).setOnClickListener { finish() }

        // Let the header color extend behind the status bar; pad everything else away from system bars.
        val root = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val headerTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            header.updatePadding(top = headerTop + bars.top)
            insets
        }
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

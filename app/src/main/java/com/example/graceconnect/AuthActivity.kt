package com.example.graceconnect

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/** Shared behaviour for the login and register screens: brand header insets, field helpers, busy state. */
abstract class AuthActivity : BaseActivity() {

    protected fun setupAuthScreen() {
        val header = findViewById<View>(R.id.header_bg)
        val statusScrim = findViewById<View>(R.id.status_scrim)
        val headerTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            header.updatePadding(top = headerTop + bars.top)
            statusScrim.updateLayoutParams { height = bars.top }
            insets
        }
    }

    /** Firebase, or null after telling the user it isn't configured (google-services.json missing). */
    protected fun authOrWarn(): FirebaseAuth? =
        Auth.instance(this) ?: null.also { toast(getString(R.string.auth_error_not_configured)) }

    protected fun text(layout: TextInputLayout): String = layout.editText?.text?.toString().orEmpty()

    /** Clears a field's error as soon as the user edits it. */
    protected fun clearErrorOnEdit(vararg layouts: TextInputLayout) {
        layouts.forEach { layout -> layout.editText?.doAfterTextChanged { layout.error = null } }
    }

    /** Runs [action] when the on-screen keyboard's Done key, or Enter on a physical keyboard, is pressed in [field]. */
    protected fun onDone(field: EditText, action: () -> Unit) {
        field.setOnEditorActionListener { _, actionId, event ->
            val enterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER
            when {
                actionId == EditorInfo.IME_ACTION_DONE -> true.also { action() }
                // A physical Enter sends a down then an up event; act once and consume both.
                enterKey -> true.also { if (event?.action == KeyEvent.ACTION_DOWN) action() }
                else -> false
            }
        }
    }

    /** Disables the form button and shows [busyText] on it while a request is in flight. */
    protected fun setBusy(button: MaterialButton, busy: Boolean, busyText: Int, idleText: Int) {
        button.isEnabled = !busy
        button.setText(if (busy) busyText else idleText)
    }

    /** Remembers who is signed in and opens the home screen as the only screen in the task. */
    protected fun openHome(user: FirebaseUser, name: String = user.displayName.orEmpty()) {
        AppPrefs.onSignedIn(this, user.uid, name, user.email.orEmpty())
        Auth.openClearingBackStack(this, MainActivity::class.java)
    }
}

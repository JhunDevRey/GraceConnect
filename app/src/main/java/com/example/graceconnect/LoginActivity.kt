package com.example.graceconnect

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/** App entry point: signs in with Firebase email + password, or skips straight to Home if already signed in. */
class LoginActivity : AuthActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Auth.currentUser(this)?.let { user ->
            openHome(user)
            return
        }

        setContentView(R.layout.activity_login)
        setupAuthScreen()

        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        loginButton = findViewById(R.id.loginButton)

        clearErrorOnEdit(emailLayout, passwordLayout)
        onDone(passwordLayout.editText!!) { login() }
        loginButton.setOnClickListener { login() }
        findViewById<MaterialButton>(R.id.forgotPassword).setOnClickListener { sendPasswordReset(it as MaterialButton) }
        findViewById<MaterialButton>(R.id.goToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = text(emailLayout).trim()
        val password = text(passwordLayout)

        var valid = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.invalid_email)
            valid = false
        }
        if (password.isEmpty()) {
            passwordLayout.error = getString(R.string.password_required)
            valid = false
        }
        if (!valid) return
        val auth = authOrWarn() ?: return

        setBusy(loginButton, true, R.string.logging_in, R.string.login)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            val user = if (task.isSuccessful) task.result.user else null
            if (user != null) {
                toast(getString(R.string.login_success))
                openHome(user)
            } else {
                setBusy(loginButton, false, R.string.logging_in, R.string.login)
                toast(Auth.errorMessage(this, task.exception))
            }
        }
    }

    /** Emails a reset link to the address typed in the email field. */
    private fun sendPasswordReset(button: MaterialButton) {
        val email = text(emailLayout).trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.reset_email_required)
            emailLayout.requestFocus()
            return
        }
        val auth = authOrWarn() ?: return

        button.isEnabled = false
        auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { task ->
            button.isEnabled = true
            // Firebase doesn't reveal whether the account exists, so the success message stays neutral.
            toast(
                if (task.isSuccessful) getString(R.string.reset_email_sent, email)
                else Auth.errorMessage(this, task.exception)
            )
        }
    }
}

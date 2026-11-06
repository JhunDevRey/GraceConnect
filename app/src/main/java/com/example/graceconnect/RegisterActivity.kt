package com.example.graceconnect

import android.os.Bundle
import android.util.Patterns
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.UserProfileChangeRequest

/** Creates a Firebase account with email + password, then signs the new user straight in. */
class RegisterActivity : AuthActivity() {

    private lateinit var nameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupAuthScreen()

        nameLayout = findViewById(R.id.nameLayout)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        registerButton = findViewById(R.id.registerButton)

        clearErrorOnEdit(nameLayout, emailLayout, passwordLayout, confirmPasswordLayout)
        onDone(confirmPasswordLayout.editText!!) { register() }
        registerButton.setOnClickListener { register() }
        // Register is always opened from Login, so going back returns there.
        findViewById<MaterialButton>(R.id.goToLogin).setOnClickListener { finish() }
    }

    private fun register() {
        val name = text(nameLayout).trim()
        val email = text(emailLayout).trim()
        val password = text(passwordLayout)

        var valid = true
        if (name.isEmpty()) {
            nameLayout.error = getString(R.string.name_required)
            valid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.invalid_email)
            valid = false
        }
        if (password.length < Auth.MIN_PASSWORD_LENGTH) {
            passwordLayout.error = getString(R.string.password_too_short, Auth.MIN_PASSWORD_LENGTH)
            valid = false
        }
        if (text(confirmPasswordLayout) != password) {
            confirmPasswordLayout.error = getString(R.string.passwords_do_not_match)
            valid = false
        }
        if (!valid) return
        val auth = authOrWarn() ?: return

        setBusy(registerButton, true, R.string.registering, R.string.register)
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            val user = if (task.isSuccessful) task.result.user else null
            if (user != null) {
                // Store the name on the account too; Home doesn't need to wait for it since it's also saved locally.
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
                toast(getString(R.string.register_success))
                openHome(user, name)
            } else {
                setBusy(registerButton, false, R.string.registering, R.string.register)
                toast(Auth.errorMessage(this, task.exception))
            }
        }
    }
}

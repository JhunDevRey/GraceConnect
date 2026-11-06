package com.example.graceconnect

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser

/** Thin wrapper around Firebase Authentication (email + password). */
object Auth {
    const val MIN_PASSWORD_LENGTH = 6

    /** Null when app/google-services.json was not added, so Firebase was never initialized. */
    fun instance(c: Context): FirebaseAuth? =
        if (FirebaseApp.getApps(c).isEmpty()) null else FirebaseAuth.getInstance()

    fun currentUser(c: Context): FirebaseUser? = instance(c)?.currentUser

    fun isSignedIn(c: Context): Boolean = currentUser(c) != null

    /** Signs out and returns to the login screen, closing every other screen. */
    fun logout(activity: Activity) {
        instance(activity)?.signOut()
        openClearingBackStack(activity, LoginActivity::class.java)
    }

    /** Starts [screen] as the only screen in the task, so back can't return to the previous ones. */
    fun openClearingBackStack(activity: Activity, screen: Class<out Activity>) {
        activity.startActivity(
            Intent(activity, screen).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }

    /** Readable message for a failed sign-in or registration. */
    fun errorMessage(c: Context, e: Exception?): String = when (e) {
        is FirebaseAuthWeakPasswordException -> c.getString(R.string.auth_error_weak_password)
        is FirebaseAuthUserCollisionException -> c.getString(R.string.auth_error_email_in_use)
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> c.getString(R.string.auth_error_invalid_credentials)
        is FirebaseNetworkException -> c.getString(R.string.auth_error_network)
        is FirebaseTooManyRequestsException -> c.getString(R.string.auth_error_too_many_requests)
        // Anything else is usually a project setup problem (e.g. Email/Password sign-in not enabled), so show
        // Firebase's own reason to make it fixable.
        else -> e?.message?.let { c.getString(R.string.auth_error_with_reason, it) }
            ?: c.getString(R.string.auth_error_generic)
    }
}

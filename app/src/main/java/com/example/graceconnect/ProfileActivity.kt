package com.example.graceconnect

import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileActivity : BaseActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var emailLayout: TextInputLayout

    // Enabled only while the form differs from what's saved, so back asks before discarding edits.
    private val unsavedChangesCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = confirmDiscard()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupScreen(getString(R.string.nav_profile), R.color.maroon_primary)

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        emailLayout = findViewById(R.id.emailLayout)

        emailInput.doAfterTextChanged { emailLayout.error = null }
        listOf(nameInput, emailInput, phoneInput).forEach { input ->
            input.doAfterTextChanged { unsavedChangesCallback.isEnabled = hasUnsavedChanges() }
        }
        onBackPressedDispatcher.addCallback(this, unsavedChangesCallback)
        findViewById<MaterialButton>(R.id.saveButton).setOnClickListener { saveProfile() }
        findViewById<MaterialButton>(R.id.clearButton).setOnClickListener { confirmClear() }

        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        renderStats()
    }

    private fun loadProfile() {
        nameInput.setText(AppPrefs.getName(this))
        emailInput.setText(AppPrefs.getEmail(this))
        phoneInput.setText(AppPrefs.getPhone(this))
        renderHeader()
    }

    private fun hasUnsavedChanges(): Boolean =
        nameInput.text?.toString().orEmpty().trim() != AppPrefs.getName(this) ||
            emailInput.text?.toString().orEmpty().trim() != AppPrefs.getEmail(this) ||
            phoneInput.text?.toString().orEmpty().trim() != AppPrefs.getPhone(this)

    private fun confirmDiscard() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.unsaved_changes_title)
            .setMessage(R.string.unsaved_changes_message)
            .setPositiveButton(R.string.save) { _, _ -> if (saveProfile()) finish() }
            .setNegativeButton(R.string.discard) { _, _ -> finish() }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    private fun renderHeader() {
        val name = AppPrefs.getName(this)
        findViewById<TextView>(R.id.displayName).text = name.ifBlank { getString(R.string.guest) }
        findViewById<TextView>(R.id.avatarText).text =
            name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
                .ifEmpty { "?" }
    }

    private fun renderStats() {
        findViewById<TextView>(R.id.statEvents).text = AppPrefs.getSet(this, AppPrefs.KEY_RSVPS).size.toString()
        findViewById<TextView>(R.id.statGroups).text = AppPrefs.getSet(this, AppPrefs.KEY_JOINED_GROUPS).size.toString()
        findViewById<TextView>(R.id.statSermons).text = AppPrefs.getSet(this, AppPrefs.KEY_SAVED_SERMONS).size.toString()
        findViewById<TextView>(R.id.statGiven).text = Format.money(AppPrefs.getGifts(this).sumOf { it.amount })
    }

    /** Returns false if the form is invalid and nothing was saved. */
    private fun saveProfile(): Boolean {
        val name = nameInput.text?.toString().orEmpty().trim()
        val email = emailInput.text?.toString().orEmpty().trim()
        val phone = phoneInput.text?.toString().orEmpty().trim()

        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.invalid_email)
            return false
        }
        AppPrefs.saveProfile(this, name, email, phone)
        unsavedChangesCallback.isEnabled = false
        renderHeader()
        toast(getString(R.string.profile_saved))
        return true
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_data_title)
            .setMessage(R.string.clear_data_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                AppPrefs.clearAll(this)
                loadProfile()
                renderStats()
                toast(getString(R.string.data_cleared))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

package com.example.graceconnect

import android.os.Bundle
import android.text.format.DateUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CommunityActivity : BaseActivity() {

    private lateinit var groupList: LinearLayout
    private lateinit var prayerSection: LinearLayout
    private lateinit var prayerList: LinearLayout
    private lateinit var prayerLayout: TextInputLayout
    private lateinit var prayerInput: TextInputEditText
    private lateinit var anonymousCheck: MaterialCheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)
        setupScreen(getString(R.string.card_community), R.color.person_red)

        groupList = findViewById(R.id.groupList)
        prayerSection = findViewById(R.id.prayerSection)
        prayerList = findViewById(R.id.prayerList)
        prayerLayout = findViewById(R.id.prayerLayout)
        prayerInput = findViewById(R.id.prayerInput)
        anonymousCheck = findViewById(R.id.anonymousCheck)

        findViewById<ChipGroup>(R.id.tabChips).setOnCheckedStateChangeListener { _, checked ->
            val showPrayer = checked.firstOrNull() == R.id.chipPrayer
            prayerSection.isVisible = showPrayer
            groupList.isVisible = !showPrayer
        }

        prayerInput.doAfterTextChanged { prayerLayout.error = null }
        findViewById<MaterialButton>(R.id.postButton).setOnClickListener { postPrayer() }

        renderGroups()
        renderPrayers()
    }

    // ---------- Small groups ----------

    private fun renderGroups() {
        val joined = AppPrefs.getSet(this, AppPrefs.KEY_JOINED_GROUPS)
        groupList.removeAllViews()
        ChurchData.groups.forEach { group ->
            val isMember = group.id in joined
            groupList.addView(layoutInflater.inflate(R.layout.item_group, groupList, false).apply {
                findViewById<TextView>(R.id.groupName).text = group.name
                findViewById<TextView>(R.id.groupSchedule).text = group.schedule
                val memberCount = group.members + if (isMember) 1 else 0
                findViewById<TextView>(R.id.groupMembers).text =
                    resources.getQuantityString(R.plurals.members, memberCount, memberCount)
                findViewById<TextView>(R.id.groupDescription).text = group.description
                findViewById<MaterialButton>(R.id.joinButton).apply {
                    setText(if (isMember) R.string.leave_group else R.string.join_group)
                    setOnClickListener {
                        val nowMember = AppPrefs.toggle(this@CommunityActivity, AppPrefs.KEY_JOINED_GROUPS, group.id)
                        toast(getString(if (nowMember) R.string.joined_group else R.string.left_group, group.name))
                        renderGroups()
                    }
                }
            })
        }
    }

    // ---------- Prayer wall ----------

    private fun postPrayer() {
        val text = prayerInput.text?.toString().orEmpty().trim()
        if (text.isEmpty()) {
            prayerLayout.error = getString(R.string.prayer_empty_error)
            return
        }
        val profileName = AppPrefs.getName(this)
        val name = if (anonymousCheck.isChecked || profileName.isBlank()) getString(R.string.anonymous) else profileName

        AppPrefs.addPrayer(this, name, text)
        prayerInput.text = null
        anonymousCheck.isChecked = false
        toast(getString(R.string.prayer_posted))
        renderPrayers()
    }

    private fun renderPrayers() {
        prayerList.removeAllViews()
        AppPrefs.getPrayers(this).forEach { prayer ->
            prayerList.addView(layoutInflater.inflate(R.layout.item_prayer, prayerList, false).apply {
                val ago = DateUtils.getRelativeTimeSpanString(prayer.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
                findViewById<TextView>(R.id.prayerAuthor).text = getString(R.string.dot_separated, prayer.name, ago)
                findViewById<TextView>(R.id.prayerText).text = prayer.text
                findViewById<MaterialButton>(R.id.prayButton).apply {
                    text = getString(if (prayer.prayedByMe) R.string.prayed else R.string.pray, prayer.prayCount)
                    setOnClickListener {
                        AppPrefs.togglePrayed(this@CommunityActivity, prayer.id)
                        renderPrayers()
                    }
                }
                findViewById<MaterialButton>(R.id.deleteButton).apply {
                    isVisible = prayer.isMine
                    setOnClickListener { confirmDelete(prayer) }
                }
            })
        }
    }

    private fun confirmDelete(prayer: PrayerRequest) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_prayer_title)
            .setMessage(R.string.delete_prayer_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                AppPrefs.deletePrayer(this, prayer.id)
                toast(getString(R.string.prayer_deleted))
                renderPrayers()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

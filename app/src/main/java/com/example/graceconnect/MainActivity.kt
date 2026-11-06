package com.example.graceconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Home is only for signed-in users; anyone else goes to Login first.
        if (!Auth.isSignedIn(this)) {
            Auth.openClearingBackStack(this, LoginActivity::class.java)
            return
        }
        setContentView(R.layout.activity_main)
        // Extend the maroon header behind the status bar; a solid scrim keeps scrolled content out from under it.
        val header = findViewById<View>(R.id.header_bg)
        val statusScrim = findViewById<View>(R.id.status_scrim)
        val headerTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // The bottom nav pads itself for the navigation bar, so the root only needs side padding.
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            header.updatePadding(top = headerTop + systemBars.top)
            statusScrim.updateLayoutParams { height = systemBars.top }
            insets
        }

        // Explore cards
        openOnClick(R.id.sermonsButton, SermonsActivity::class.java)
        openOnClick(R.id.eventsButton, EventsActivity::class.java)
        openOnClick(R.id.giveButton, GiveActivity::class.java)
        openOnClick(R.id.communityButton, CommunityActivity::class.java)

        // Upcoming events -> Events screen
        openOnClick(R.id.seeAllEvents, EventsActivity::class.java)
        openOnClick(R.id.event1Card, EventsActivity::class.java)
        openOnClick(R.id.event2Card, EventsActivity::class.java)

        // Verse of the day -> share
        findViewById<View>(R.id.verseCard).setOnClickListener {
            val verse = ChurchData.verseOfTheDay()
            shareText(getString(R.string.verse_share_text, verse.text, verse.reference))
        }

        // Bottom navigation: Home stays selected; other tabs open their screen.
        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bible -> startActivity(Intent(this, BibleActivity::class.java))
                R.id.nav_events -> startActivity(Intent(this, EventsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            }
            item.itemId == R.id.nav_home
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNav.selectedItemId = R.id.nav_home

        // Refresh content that can change on other screens (name) or with the date (verse, events).
        val name = AppPrefs.getName(this).ifBlank { getString(R.string.welcome_default_name) }
        findViewById<TextView>(R.id.welcomeText).text = getString(R.string.welcome_message, name)

        val verse = ChurchData.verseOfTheDay()
        findViewById<TextView>(R.id.verseText).text = getString(R.string.quoted, verse.text)
        findViewById<TextView>(R.id.verseRef).text = getString(R.string.verse_attribution, verse.reference)

        val upcoming = ChurchData.upcomingEvents()
        bindEvent(upcoming[0], R.id.event1Title, R.id.event1Sub)
        bindEvent(upcoming[1], R.id.event2Title, R.id.event2Sub)
    }

    private fun bindEvent(event: ChurchEvent, titleId: Int, subId: Int) {
        findViewById<TextView>(titleId).text = event.title
        findViewById<TextView>(subId).text = getString(R.string.dot_separated, Format.dayAndTime(event.nextStart()), event.location)
    }

    private fun openOnClick(viewId: Int, screen: Class<*>) {
        findViewById<View>(viewId).setOnClickListener {
            startActivity(Intent(this, screen))
        }
    }
}

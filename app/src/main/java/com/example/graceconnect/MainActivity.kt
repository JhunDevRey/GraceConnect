package com.example.graceconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableBrandEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Extend the maroon header behind the status bar and push the header content below it.
        val headerBg = findViewById<View>(R.id.header_bg)
        val logo = findViewById<View>(R.id.logo)
        val welcomeCard = findViewById<View>(R.id.welcome_card)
        val headerHeight = headerBg.layoutParams.height
        val logoTop = (logo.layoutParams as ViewGroup.MarginLayoutParams).topMargin
        val cardTop = (welcomeCard.layoutParams as ViewGroup.MarginLayoutParams).topMargin
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // The bottom nav pads itself for the navigation bar, so the root only needs side padding.
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            headerBg.updateLayoutParams { height = headerHeight + systemBars.top }
            logo.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = logoTop + systemBars.top }
            welcomeCard.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = cardTop + systemBars.top }
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
            val send = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "\"${verse.text}\" — ${verse.reference}")
            startActivity(Intent.createChooser(send, getString(R.string.share)))
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
        findViewById<TextView>(R.id.verseText).text = "\"${verse.text}\""
        findViewById<TextView>(R.id.verseRef).text = "— ${verse.reference}"

        val upcoming = ChurchData.upcomingEvents()
        bindEvent(upcoming[0], R.id.event1Title, R.id.event1Sub)
        bindEvent(upcoming[1], R.id.event2Title, R.id.event2Sub)
    }

    private fun bindEvent(event: ChurchEvent, titleId: Int, subId: Int) {
        findViewById<TextView>(titleId).text = event.title
        findViewById<TextView>(subId).text = "${Format.dayAndTime(event.nextStart())} · ${event.location}"
    }

    private fun openOnClick(viewId: Int, screen: Class<*>) {
        findViewById<View>(viewId).setOnClickListener {
            startActivity(Intent(this, screen))
        }
    }
}

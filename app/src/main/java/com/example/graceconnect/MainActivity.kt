package com.example.graceconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Button 1: Sermons card -> SermonsActivity
        val sermonsButton = findViewById<View>(R.id.sermonsButton)
        sermonsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SermonsActivity::class.java)
            startActivity(intent)
        }

        // Button 2: Events card -> EventsActivity
        val eventsButton = findViewById<View>(R.id.eventsButton)
        eventsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, EventsActivity::class.java)
            startActivity(intent)
        }

        // Button 3: Give card -> GiveActivity
        val giveButton = findViewById<View>(R.id.giveButton)
        giveButton.setOnClickListener {
            val intent = Intent(this@MainActivity, GiveActivity::class.java)
            startActivity(intent)
        }

        // Button 4: Community card -> CommunityActivity
        val communityButton = findViewById<View>(R.id.communityButton)
        communityButton.setOnClickListener {
            val intent = Intent(this@MainActivity, CommunityActivity::class.java)
            startActivity(intent)
        }
    }
}

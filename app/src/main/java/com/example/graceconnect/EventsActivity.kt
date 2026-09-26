package com.example.graceconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup

class EventsActivity : BaseActivity() {

    private lateinit var filterChips: ChipGroup
    private lateinit var list: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        setupScreen(getString(R.string.card_events), R.color.person_green)

        filterChips = findViewById(R.id.filterChips)
        list = findViewById(R.id.eventList)
        emptyText = findViewById(R.id.emptyText)

        filterChips.setOnCheckedStateChangeListener { _, _ -> render() }
        render()
    }

    private fun render() {
        val goingOnly = filterChips.checkedChipId == R.id.chipGoing
        val rsvps = AppPrefs.getSet(this, AppPrefs.KEY_RSVPS)
        val events = ChurchData.upcomingEvents().filter { !goingOnly || it.id in rsvps }

        list.removeAllViews()
        events.forEach { list.addView(eventRow(it, it.id in rsvps)) }
        emptyText.isVisible = events.isEmpty()
    }

    private fun eventRow(event: ChurchEvent, isGoing: Boolean) =
        layoutInflater.inflate(R.layout.item_event, list, false).apply {
            val start = event.nextStart()
            findViewById<TextView>(R.id.eventMonth).text = Format.month(start)
            findViewById<TextView>(R.id.eventDay).text = Format.dayOfMonth(start)
            findViewById<TextView>(R.id.eventTitle).text = event.title
            findViewById<TextView>(R.id.eventWhen).text = "${Format.dayAndTime(start)} · ${event.location}"
            findViewById<TextView>(R.id.eventDescription).text = event.description
            findViewById<TextView>(R.id.eventGoing).isVisible = isGoing

            findViewById<MaterialButton>(R.id.rsvpButton).apply {
                setText(if (isGoing) R.string.going else R.string.rsvp)
                setOnClickListener { toggleRsvp(event) }
            }
            findViewById<MaterialButton>(R.id.calendarButton).setOnClickListener { addToCalendar(event) }
            findViewById<MaterialButton>(R.id.directionsButton).setOnClickListener {
                launch(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(CHURCH_ADDRESS))))
            }
        }

    private fun toggleRsvp(event: ChurchEvent) {
        val going = AppPrefs.toggle(this, AppPrefs.KEY_RSVPS, event.id)
        toast(if (going) getString(R.string.rsvp_added, event.title) else getString(R.string.rsvp_removed))
        render()
    }

    private fun addToCalendar(event: ChurchEvent) {
        val start = event.nextStart().timeInMillis
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + event.durationMinutes * 60_000L)
            .putExtra(CalendarContract.Events.TITLE, event.title)
            .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            .putExtra(CalendarContract.Events.EVENT_LOCATION, "${event.location}, $CHURCH_NAME")
            .putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY")
        launch(intent)
    }
}

package com.example.graceconnect

import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SermonsActivity : BaseActivity() {

    private lateinit var searchInput: TextInputEditText
    private lateinit var filterChips: ChipGroup
    private lateinit var list: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sermons)
        setupScreen(getString(R.string.card_sermons), R.color.person_blue)

        searchInput = findViewById(R.id.searchInput)
        filterChips = findViewById(R.id.filterChips)
        list = findViewById(R.id.sermonList)
        emptyText = findViewById(R.id.emptyText)

        searchInput.doAfterTextChanged { render() }
        filterChips.setOnCheckedStateChangeListener { _, _ -> render() }
        render()
    }

    private fun render() {
        val query = searchInput.text?.toString().orEmpty().trim()
        val savedOnly = filterChips.checkedChipId == R.id.chipSaved
        val saved = AppPrefs.getSet(this, AppPrefs.KEY_SAVED_SERMONS)

        val sermons = ChurchData.sermons.filter { s ->
            (!savedOnly || s.id in saved) &&
                listOf(s.title, s.speaker, s.scripture).any { it.contains(query, ignoreCase = true) }
        }

        list.removeAllViews()
        sermons.forEach { list.addView(sermonRow(it, it.id in saved)) }

        emptyText.isVisible = sermons.isEmpty()
        emptyText.setText(if (savedOnly && query.isEmpty()) R.string.no_saved_sermons else R.string.no_sermons_found)
    }

    private fun sermonRow(sermon: Sermon, isSaved: Boolean) =
        layoutInflater.inflate(R.layout.item_sermon, list, false).apply {
            findViewById<TextView>(R.id.sermonTitle).text = sermon.title
            findViewById<TextView>(R.id.sermonMeta).text =
                "${sermon.speaker} · ${Format.date(sermon.date())} · ${sermon.durationMinutes} min"
            findViewById<TextView>(R.id.sermonScripture).text = sermon.scripture

            findViewById<ImageButton>(R.id.saveButton).apply {
                setImageResource(if (isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border)
                contentDescription = getString(if (isSaved) R.string.unsave_sermon else R.string.save_sermon)
                setOnClickListener { toggleSaved(sermon) }
            }
            setOnClickListener { showDetails(sermon) }
        }

    private fun toggleSaved(sermon: Sermon) {
        val nowSaved = AppPrefs.toggle(this, AppPrefs.KEY_SAVED_SERMONS, sermon.id)
        toast(getString(if (nowSaved) R.string.sermon_saved else R.string.sermon_unsaved))
        render()
    }

    private fun showDetails(sermon: Sermon) {
        val message = "${sermon.speaker}\n${Format.date(sermon.date())} · ${sermon.durationMinutes} min\n" +
            "${sermon.scripture}\n\n${sermon.summary}"

        MaterialAlertDialogBuilder(this)
            .setTitle(sermon.title)
            .setMessage(message)
            .setPositiveButton(R.string.watch) { _, _ -> openUrl(sermon.watchUrl()) }
            .setNeutralButton(R.string.share) { _, _ ->
                shareText(getString(R.string.sermon_share_text, sermon.title, sermon.speaker, sermon.scripture, sermon.watchUrl()))
            }
            .setNegativeButton(R.string.read_scripture) { _, _ ->
                openUrl(Verse(sermon.scripture, "").chapterUrl())
            }
            .show()
    }
}

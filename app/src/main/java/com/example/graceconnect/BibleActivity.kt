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

class BibleActivity : BaseActivity() {

    private lateinit var searchInput: TextInputEditText
    private lateinit var filterChips: ChipGroup
    private lateinit var list: LinearLayout
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bible)
        setupScreen(getString(R.string.nav_bible), R.color.maroon_primary)

        val daily = ChurchData.verseOfTheDay()
        findViewById<TextView>(R.id.dailyVerseText).text = "\"${daily.text}\""
        findViewById<TextView>(R.id.dailyVerseRef).text = "— ${daily.reference} (KJV)"
        findViewById<TextView>(R.id.shareDailyButton).setOnClickListener { shareVerse(daily) }
        findViewById<TextView>(R.id.readDailyButton).setOnClickListener { openUrl(daily.chapterUrl()) }
        findViewById<TextView>(R.id.openBibleButton).setOnClickListener { openUrl(ChurchData.bibleUrl()) }

        searchInput = findViewById(R.id.searchInput)
        filterChips = findViewById(R.id.filterChips)
        list = findViewById(R.id.verseList)
        emptyText = findViewById(R.id.emptyText)

        searchInput.doAfterTextChanged { render() }
        filterChips.setOnCheckedStateChangeListener { _, _ -> render() }
        render()
    }

    private fun render() {
        val query = searchInput.text?.toString().orEmpty().trim()
        val favoritesOnly = filterChips.checkedChipId == R.id.chipFavorites
        val favorites = AppPrefs.getSet(this, AppPrefs.KEY_FAVORITE_VERSES)

        val verses = ChurchData.verses.filter { v ->
            (!favoritesOnly || v.id in favorites) &&
                (v.reference.contains(query, ignoreCase = true) || v.text.contains(query, ignoreCase = true))
        }

        list.removeAllViews()
        verses.forEach { verse ->
            val isFavorite = verse.id in favorites
            list.addView(layoutInflater.inflate(R.layout.item_verse, list, false).apply {
                findViewById<TextView>(R.id.verseText).text = verse.text
                findViewById<TextView>(R.id.verseRef).text = verse.reference
                findViewById<ImageButton>(R.id.favoriteButton).apply {
                    setImageResource(if (isFavorite) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border)
                    contentDescription = getString(R.string.save_verse)
                    setOnClickListener {
                        val saved = AppPrefs.toggle(this@BibleActivity, AppPrefs.KEY_FAVORITE_VERSES, verse.id)
                        toast(getString(if (saved) R.string.verse_saved else R.string.verse_unsaved))
                        render()
                    }
                }
                setOnClickListener { showVerse(verse) }
            })
        }

        emptyText.isVisible = verses.isEmpty()
        emptyText.setText(if (favoritesOnly && query.isEmpty()) R.string.no_saved_verses else R.string.no_verses_found)
    }

    private fun showVerse(verse: Verse) {
        MaterialAlertDialogBuilder(this)
            .setTitle(verse.reference)
            .setMessage(verse.text)
            .setPositiveButton(R.string.read_chapter) { _, _ -> openUrl(verse.chapterUrl()) }
            .setNeutralButton(R.string.share) { _, _ -> shareVerse(verse) }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    private fun shareVerse(verse: Verse) = shareText("\"${verse.text}\" — ${verse.reference}")
}

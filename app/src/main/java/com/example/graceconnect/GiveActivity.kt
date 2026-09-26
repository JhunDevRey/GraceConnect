package com.example.graceconnect

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class GiveActivity : BaseActivity() {

    private lateinit var fundChips: ChipGroup
    private lateinit var amountChips: ChipGroup
    private lateinit var amountLayout: TextInputLayout
    private lateinit var amountInput: TextInputEditText
    private lateinit var noteInput: TextInputEditText

    // Set while a preset chip writes into the amount field, so typing can clear the chip without a loop.
    private var fillingFromChip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_give)
        setupScreen(getString(R.string.card_give), R.color.person_orange)

        fundChips = findViewById(R.id.fundChips)
        amountChips = findViewById(R.id.amountChips)
        amountLayout = findViewById(R.id.amountLayout)
        amountInput = findViewById(R.id.amountInput)
        noteInput = findViewById(R.id.noteInput)

        ChurchData.funds.forEach { fundChips.addView(filterChip(it)) }
        fundChips.check(fundChips.getChildAt(0).id)
        ChurchData.presetAmounts.forEach { amount ->
            amountChips.addView(filterChip(Format.wholeMoney(amount)).apply {
                setOnClickListener {
                    if (isChecked) {
                        fillingFromChip = true
                        amountInput.setText(amount.toString())
                        amountInput.setSelection(amountInput.length())
                        fillingFromChip = false
                    }
                }
            })
        }

        amountInput.doAfterTextChanged {
            amountLayout.error = null
            if (!fillingFromChip) amountChips.clearCheck()
        }

        findViewById<View>(R.id.giveButton).setOnClickListener { confirmGift() }
        renderHistory()
    }

    private fun filterChip(label: String): Chip =
        (layoutInflater.inflate(R.layout.item_filter_chip, fundChips, false) as Chip).apply {
            text = label
            id = View.generateViewId()
        }

    private fun confirmGift() {
        val amount = amountInput.text?.toString()?.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountLayout.error = getString(R.string.give_invalid_amount)
            return
        }
        val fund = fundChips.findViewById<Chip>(fundChips.checkedChipId).text.toString()
        val note = noteInput.text?.toString().orEmpty().trim()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.give_confirm_title)
            .setMessage(getString(R.string.give_confirm_message, Format.money(amount), fund))
            .setPositiveButton(R.string.give_confirm) { _, _ ->
                AppPrefs.addGift(this, Gift(fund, amount, note, System.currentTimeMillis()))
                amountInput.text = null
                noteInput.text = null
                amountChips.clearCheck()
                renderHistory()
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.give_thanks)
                    .setPositiveButton(R.string.close, null)
                    .show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun renderHistory() {
        val gifts = AppPrefs.getGifts(this)
        val list = findViewById<LinearLayout>(R.id.historyList)
        list.removeAllViews()
        gifts.forEach { gift ->
            list.addView(layoutInflater.inflate(R.layout.item_gift, list, false).apply {
                findViewById<TextView>(R.id.giftFund).text = gift.fund
                findViewById<TextView>(R.id.giftDate).text =
                    if (gift.note.isEmpty()) Format.date(gift.time) else "${Format.date(gift.time)} · ${gift.note}"
                findViewById<TextView>(R.id.giftAmount).text = Format.money(gift.amount)
            })
        }
        findViewById<TextView>(R.id.emptyText).isVisible = gifts.isEmpty()
        findViewById<TextView>(R.id.totalText).text = getString(R.string.give_total, Format.money(gifts.sumOf { it.amount }))
    }
}

package com.example.graceconnect

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Format {
    fun money(amount: Double): String = NumberFormat.getCurrencyInstance().format(amount)

    /**
     * Parses a typed amount such as "1,000", "1000.5", "12,50" or "1.000,50", rounded to cents.
     * A trailing comma followed by one or two digits is read as a decimal separator (keyboards in
     * many locales type "," for "."), making any dots thousands separators; otherwise commas are.
     * Returns null if the text isn't a number.
     */
    fun parseAmount(text: String): Double? {
        val compact = text.trim().replace(" ", "")
        val cleaned = if (Regex(",\\d{1,2}$").containsMatchIn(compact)) {
            compact.replace(".", "").replace(',', '.')
        } else {
            compact.replace(",", "")
        }
        val value = cleaned.toBigDecimalOrNull() ?: return null
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
    }

    /** Whole amounts without cents, e.g. "$250" for the preset chips. */
    fun wholeMoney(amount: Int): String =
        NumberFormat.getCurrencyInstance().apply { maximumFractionDigits = 0 }.format(amount)

    /** e.g. "Sep 28, 2026" */
    fun date(time: Long): String = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(time))

    fun date(c: Calendar): String = date(c.timeInMillis)

    /** e.g. "Sun · 9:00 AM" */
    fun dayAndTime(c: Calendar): String =
        SimpleDateFormat("EEE · h:mm a", Locale.getDefault()).format(c.time)

    fun month(c: Calendar): String = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)

    fun dayOfMonth(c: Calendar): String = c.get(Calendar.DAY_OF_MONTH).toString()
}

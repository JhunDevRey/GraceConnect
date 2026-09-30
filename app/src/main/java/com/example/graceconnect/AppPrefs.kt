package com.example.graceconnect

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

data class Gift(val fund: String, val amount: Double, val note: String, val time: Long)

data class PrayerRequest(
    val id: String,
    val name: String,
    val text: String,
    val time: Long,
    val prayCount: Int,
    val prayedByMe: Boolean,
    /** True for requests posted from this device, which the user may delete. */
    val isMine: Boolean = false
)

/** Everything the user does in the app is stored on the device with SharedPreferences. */
object AppPrefs {
    private const val FILE = "grace_connect_prefs"

    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone"
    private const val KEY_GIFTS = "gifts"
    private const val KEY_PRAYERS = "prayers"
    const val KEY_SAVED_SERMONS = "saved_sermons"
    const val KEY_RSVPS = "rsvps"
    const val KEY_JOINED_GROUPS = "joined_groups"
    const val KEY_FAVORITE_VERSES = "favorite_verses"

    private fun prefs(c: Context): SharedPreferences =
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ---------- Profile ----------

    fun getName(c: Context): String = prefs(c).getString(KEY_NAME, "") ?: ""
    fun getEmail(c: Context): String = prefs(c).getString(KEY_EMAIL, "") ?: ""
    fun getPhone(c: Context): String = prefs(c).getString(KEY_PHONE, "") ?: ""

    fun saveProfile(c: Context, name: String, email: String, phone: String) {
        prefs(c).edit {
            putString(KEY_NAME, name)
            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone)
        }
    }

    // ---------- ID sets (saved sermons, RSVPs, groups, favorite verses) ----------

    fun getSet(c: Context, key: String): Set<String> =
        prefs(c).getStringSet(key, emptySet())?.toSet() ?: emptySet()

    fun contains(c: Context, key: String, id: String): Boolean = id in getSet(c, key)

    /** Adds [id] if absent, removes it if present. Returns true if it is now in the set. */
    fun toggle(c: Context, key: String, id: String): Boolean {
        val set = getSet(c, key).toMutableSet()
        val added = if (id in set) { set.remove(id); false } else { set.add(id); true }
        prefs(c).edit { putStringSet(key, set) }
        return added
    }

    // ---------- Giving ----------

    fun getGifts(c: Context): List<Gift> {
        val array = JSONArray(prefs(c).getString(KEY_GIFTS, "[]"))
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Gift(o.getString("fund"), o.getDouble("amount"), o.optString("note"), o.getLong("time"))
        }.sortedByDescending { it.time }
    }

    fun addGift(c: Context, gift: Gift) {
        val array = JSONArray(prefs(c).getString(KEY_GIFTS, "[]"))
        array.put(JSONObject().apply {
            put("fund", gift.fund)
            put("amount", gift.amount)
            put("note", gift.note)
            put("time", gift.time)
        })
        prefs(c).edit { putString(KEY_GIFTS, array.toString()) }
    }

    // ---------- Prayer wall ----------

    fun getPrayers(c: Context): List<PrayerRequest> {
        val json = prefs(c).getString(KEY_PRAYERS, null) ?: return samplePrayers()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            PrayerRequest(
                o.getString("id"), o.getString("name"), o.getString("text"),
                o.getLong("time"), o.getInt("count"), o.getBoolean("prayed"),
                o.optBoolean("mine")
            )
        }.sortedByDescending { it.time }
    }

    private fun savePrayers(c: Context, prayers: List<PrayerRequest>) {
        val array = JSONArray()
        prayers.forEach { p ->
            array.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("text", p.text)
                put("time", p.time)
                put("count", p.prayCount)
                put("prayed", p.prayedByMe)
                put("mine", p.isMine)
            })
        }
        prefs(c).edit { putString(KEY_PRAYERS, array.toString()) }
    }

    fun addPrayer(c: Context, name: String, text: String) {
        val now = System.currentTimeMillis()
        savePrayers(c, listOf(PrayerRequest(now.toString(), name, text, now, 0, false, isMine = true)) + getPrayers(c))
    }

    fun togglePrayed(c: Context, id: String) {
        savePrayers(c, getPrayers(c).map {
            if (it.id != id) it
            else it.copy(
                prayedByMe = !it.prayedByMe,
                prayCount = it.prayCount + if (it.prayedByMe) -1 else 1
            )
        })
    }

    fun deletePrayer(c: Context, id: String) {
        savePrayers(c, getPrayers(c).filterNot { it.id == id && it.isMine })
    }

    private fun samplePrayers(): List<PrayerRequest> {
        val now = System.currentTimeMillis()
        val hour = 60 * 60 * 1000L
        return listOf(
            PrayerRequest("sample_1", "Maria", "Please pray for my mother's recovery after her surgery this week.", now - 3 * hour, 12, false),
            PrayerRequest("sample_2", "Anonymous", "Praying for guidance as I look for a new job. Thank you, church family!", now - 26 * hour, 8, false)
        )
    }

    // ---------- Reset ----------

    fun clearAll(c: Context) {
        prefs(c).edit { clear() }
    }
}

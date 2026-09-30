package com.example.graceconnect

import java.net.URLEncoder
import java.util.Calendar

// Church-wide constants. Replace these with your church's real details.
const val CHURCH_NAME = "Grace Community Church"
const val CHURCH_ADDRESS = "Grace Community Church"

data class Sermon(
    val id: String,
    val title: String,
    val speaker: String,
    val scripture: String,
    val durationMinutes: Int,
    val weeksAgo: Int,
    val summary: String
) {
    /** The Sunday this sermon was preached, counted back from the most recent Sunday. */
    fun date(): Calendar {
        val c = Calendar.getInstance()
        val daysSinceSunday = (c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
        c.add(Calendar.DAY_OF_YEAR, -daysSinceSunday - 7 * weeksAgo)
        return c
    }

    fun watchUrl(): String =
        "https://www.youtube.com/results?search_query=" + encode("$title sermon $scripture")
}

data class ChurchEvent(
    val id: String,
    val title: String,
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int,
    val durationMinutes: Int,
    val location: String,
    val description: String
) {
    /** Next weekly occurrence of this event that hasn't started yet. */
    fun nextStart(now: Calendar = Calendar.getInstance()): Calendar {
        val c = now.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.DAY_OF_YEAR, (dayOfWeek - c.get(Calendar.DAY_OF_WEEK) + 7) % 7)
        if (c.before(now)) c.add(Calendar.DAY_OF_YEAR, 7)
        return c
    }
}

data class SmallGroup(
    val id: String,
    val name: String,
    val schedule: String,
    val members: Int,
    val description: String
)

data class Verse(val reference: String, val text: String) {
    val id: String get() = reference

    fun chapterUrl(): String =
        "https://www.biblegateway.com/passage/?search=" + encode(reference) + "&version=KJV"
}

private fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")

object ChurchData {

    val sermons = listOf(
        Sermon("walking_by_faith", "Walking by Faith", "Ptr. Daniel Cruz", "2 Corinthians 5:7", 42, 0,
            "What does it look like to trust God when we cannot see the next step? A look at faith that moves us forward."),
        Sermon("power_of_prayer", "The Power of Prayer", "Ptr. Ruth Santos", "James 5:16", 38, 1,
            "Prayer is not a last resort but our first response. Learn how honest, persistent prayer changes us and the world around us."),
        Sermon("grace_that_transforms", "Grace That Transforms", "Ptr. Daniel Cruz", "Ephesians 2:8-9", 45, 2,
            "We are saved by grace through faith, not by our own works. Discover how grace reshapes our identity and our everyday life."),
        Sermon("love_one_another", "Love One Another", "Ptr. Mark Reyes", "John 13:34-35", 40, 3,
            "Jesus said the world would know us by our love. Practical ways to love our neighbors, families, and even our enemies."),
        Sermon("hope_in_hard_times", "Hope in Hard Times", "Ptr. Ruth Santos", "Romans 5:3-5", 44, 4,
            "Suffering produces perseverance, character, and hope. Finding God's purpose in the middle of life's storms."),
        Sermon("serving_with_joy", "Serving With Joy", "Ptr. Mark Reyes", "Galatians 5:13", 36, 5,
            "We are called to serve one another humbly in love. Discover your gifts and how to use them in the church and community.")
    )

    val events = listOf(
        ChurchEvent("sunday_worship", "Sunday Worship Service", Calendar.SUNDAY, 9, 0, 90, "Main Hall",
            "Join us for worship, prayer, and the preaching of God's Word. Everyone is welcome!"),
        ChurchEvent("kids_church", "Kids Church", Calendar.SUNDAY, 9, 0, 90, "Children's Wing",
            "Fun, Bible-centered lessons, songs, and crafts for kids ages 4 to 12 during the main service."),
        ChurchEvent("womens_study", "Women's Bible Study", Calendar.TUESDAY, 10, 0, 90, "Room 2",
            "A warm gathering of women studying Scripture together. Coffee and snacks provided."),
        ChurchEvent("midweek_prayer", "Midweek Prayer Meeting", Calendar.WEDNESDAY, 19, 0, 60, "Prayer Room",
            "Come and pray for our church, our city, and one another."),
        ChurchEvent("youth_night", "Youth Fellowship Night", Calendar.FRIDAY, 18, 30, 120, "Fellowship Hall",
            "Games, worship, and real talk for teens ages 13 to 19. Bring a friend!"),
        ChurchEvent("mens_breakfast", "Men's Breakfast", Calendar.SATURDAY, 7, 0, 90, "Fellowship Hall",
            "Breakfast, fellowship, and a short devotional for men of all ages.")
    )

    val groups = listOf(
        SmallGroup("young_adults", "Young Adults", "Thursdays · 7:00 PM", 24,
            "For singles and young professionals ages 20 to 35 growing in faith and friendship."),
        SmallGroup("couples", "Couples Connect", "Every other Saturday · 6:00 PM", 16,
            "Married and engaged couples building Christ-centered relationships."),
        SmallGroup("worship_team", "Worship Team", "Saturdays · 3:00 PM", 12,
            "Singers and musicians who lead the congregation in worship every Sunday."),
        SmallGroup("outreach", "Outreach Ministry", "First Saturday of the month · 8:00 AM", 30,
            "Serving our neighbors through feeding programs, visits, and community projects."),
        SmallGroup("prayer_warriors", "Prayer Warriors", "Wednesdays · 7:00 PM", 18,
            "Dedicated intercessors who pray over every request shared with the church.")
    )

    // King James Version (public domain).
    val verses = listOf(
        Verse("Proverbs 3:5", "Trust in the LORD with all thine heart; and lean not unto thine own understanding."),
        Verse("John 3:16", "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."),
        Verse("Philippians 4:13", "I can do all things through Christ which strengtheneth me."),
        Verse("Jeremiah 29:11", "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end."),
        Verse("Psalm 23:1", "The LORD is my shepherd; I shall not want."),
        Verse("Isaiah 40:31", "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint."),
        Verse("Romans 8:28", "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."),
        Verse("Joshua 1:9", "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest."),
        Verse("Matthew 11:28", "Come unto me, all ye that labour and are heavy laden, and I will give you rest."),
        Verse("Psalm 46:1", "God is our refuge and strength, a very present help in trouble."),
        Verse("2 Corinthians 5:7", "For we walk by faith, not by sight."),
        Verse("Philippians 4:6", "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God."),
        Verse("Psalm 119:105", "Thy word is a lamp unto my feet, and a light unto my path."),
        Verse("Lamentations 3:22-23", "It is of the LORD's mercies that we are not consumed, because his compassions fail not. They are new every morning: great is thy faithfulness."),
        Verse("Ephesians 2:8", "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God."),
        Verse("1 John 4:19", "We love him, because he first loved us.")
    )

    val funds = listOf("Tithe", "Offering", "Missions", "Building Fund")

    val presetAmounts = listOf(100, 250, 500, 1000)

    /** Largest single gift accepted, to catch typos like an extra zero or two. */
    const val MAX_GIFT = 1_000_000.0

    fun verseOfTheDay(): Verse =
        verses[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % verses.size]

    fun upcomingEvents(): List<ChurchEvent> {
        val now = Calendar.getInstance()
        return events.sortedBy { it.nextStart(now).timeInMillis }
    }

    fun bibleUrl(): String = "https://www.biblegateway.com/"
}

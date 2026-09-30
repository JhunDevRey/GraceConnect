package com.example.graceconnect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatTest {

    @Test
    fun parseAmount_acceptsCommonFormats() {
        assertEquals(1000.0, Format.parseAmount("1000")!!, 0.0)
        assertEquals(1000.0, Format.parseAmount("1,000")!!, 0.0)
        assertEquals(1000.5, Format.parseAmount("1,000.50")!!, 0.0)
        assertEquals(12.5, Format.parseAmount("12,50")!!, 0.0)
        assertEquals(1000.5, Format.parseAmount("1.000,50")!!, 0.0)
        assertEquals(250.0, Format.parseAmount(" 250 ")!!, 0.0)
    }

    @Test
    fun parseAmount_roundsToCents() {
        assertEquals(10.13, Format.parseAmount("10.125")!!, 0.0)
        assertEquals(0.0, Format.parseAmount("0.001")!!, 0.0)
    }

    @Test
    fun parseAmount_rejectsNonNumbers() {
        assertNull(Format.parseAmount(""))
        assertNull(Format.parseAmount("."))
        assertNull(Format.parseAmount("1.2.3"))
        assertNull(Format.parseAmount("abc"))
    }
}

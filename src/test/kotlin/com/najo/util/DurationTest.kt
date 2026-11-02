package com.najo.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DurationTest {

    @Test
    fun `custom durations accept one minute up to a full day`() {
        assertEquals(1, parseCustomDuration("1"))
        assertEquals(180, parseCustomDuration("180"))
        assertEquals(MAX_TASK_DURATION_MINUTES, parseCustomDuration("1440"))
        assertEquals(90, parseCustomDuration(" 90 "))
    }

    @Test
    fun `invalid custom durations are rejected`() {
        assertNull(parseCustomDuration(""))
        assertNull(parseCustomDuration("   "))
        assertNull(parseCustomDuration("0"))
        assertNull(parseCustomDuration("1441"))
        assertNull(parseCustomDuration("-30"))
        assertNull(parseCustomDuration("2h"))
    }
}

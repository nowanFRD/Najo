package com.najo.util

/** The longest block the planner accepts: one full day. */
const val MAX_TASK_DURATION_MINUTES = 24 * 60

/**
 * Parses a typed duration, accepting only 1..[MAX_TASK_DURATION_MINUTES]
 * minutes. Blank or out-of-range input is rejected.
 */
fun parseCustomDuration(text: String): Int? =
    text.trim().toIntOrNull()?.takeIf { it in 1..MAX_TASK_DURATION_MINUTES }

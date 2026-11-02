package com.najo.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val longDate = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
private val shortDate = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)
private val upperDate = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ENGLISH)

fun formatDateLong(date: LocalDate): String = date.format(longDate)

fun formatDateShort(date: LocalDate): String = date.format(shortDate)

fun formatDateUpper(date: LocalDate): String = date.format(upperDate).uppercase(Locale.ENGLISH)

/** 615 -> "10:15" */
fun formatTime(minuteOfDay: Int): String {
    val normalized = ((minuteOfDay % (24 * 60)) + 24 * 60) % (24 * 60)
    return "%02d:%02d".format(normalized / 60, normalized % 60)
}

/** 90 -> "1h 30m", 45 -> "45m", 120 -> "2h" */
fun formatDuration(minutes: Int): String {
    if (minutes < 60) return "${minutes}m"
    val hours = minutes / 60
    val rest = minutes % 60
    return if (rest == 0) "${hours}h" else "${hours}h ${rest}m"
}

/** 615 -> "10:15 – 11:00" */
fun formatRange(startMinute: Int, durationMinutes: Int): String =
    "${formatTime(startMinute)} – ${formatTime(startMinute + durationMinutes)}"

fun greeting(hour: Int): String = when {
    hour < 5 -> "Still up"
    hour < 12 -> "Good morning"
    hour < 18 -> "Good afternoon"
    else -> "Good evening"
}

fun currentMinuteOfDay(time: LocalTime = LocalTime.now()): Int = time.hour * 60 + time.minute

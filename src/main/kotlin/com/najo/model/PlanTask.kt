package com.najo.model

import kotlinx.serialization.Serializable
import java.util.UUID

/** How much attention a task deserves. Drives the accent colour in the UI. */
@Serializable
enum class Priority(val label: String) {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High"),
}

/** A single block on the day's schedule. Times are stored as minutes from midnight. */
@Serializable
data class PlanTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startMinute: Int,
    val durationMinutes: Int = 30,
    val priority: Priority = Priority.NORMAL,
    val note: String = "",
    val done: Boolean = false,
) {
    val endMinute: Int get() = startMinute + durationMinutes

    fun overlaps(other: PlanTask): Boolean =
        startMinute < other.endMinute && other.startMinute < endMinute
}

/** The parts of a day used to group the schedule. */
enum class DaySection(val label: String, val startMinute: Int) {
    MORNING("Morning", 0),
    AFTERNOON("Afternoon", 12 * 60),
    EVENING("Evening", 17 * 60),
    NIGHT("Night", 21 * 60);

    companion object {
        fun of(minute: Int): DaySection = entries.last { minute >= it.startMinute }
    }
}

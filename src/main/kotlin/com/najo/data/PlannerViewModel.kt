package com.najo.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.najo.model.PlanTask
import com.najo.model.Priority
import java.time.LocalDate
import java.time.LocalDateTime

enum class TaskFilter(val label: String) {
    ALL("All"),
    TODO("To do"),
    DONE("Done"),
}

/** Values captured by the add/edit dialog. */
data class TaskDraft(
    val title: String,
    val startMinute: Int,
    val durationMinutes: Int,
    val priority: Priority,
    val note: String,
)

data class DayStats(
    val total: Int,
    val done: Int,
    val plannedMinutes: Int,
    val doneMinutes: Int,
) {
    val progress: Float get() = if (total == 0) 0f else done.toFloat() / total
    val remainingMinutes: Int get() = (plannedMinutes - doneMinutes).coerceAtLeast(0)
    val todo: Int get() = total - done
}

/**
 * Holds the whole screen state. Deliberately a plain state holder (no DI, no
 * Android ViewModel) so the UI stays a pure function of this state.
 */
class PlannerViewModel(
    private val store: PlannerStore,
    private val clock: () -> LocalDateTime = { LocalDateTime.now() },
) {
    var tasks by mutableStateOf<List<PlanTask>>(emptyList())
        private set

    var darkTheme by mutableStateOf(false)
        private set

    /** Consecutive days with at least one finished task. */
    var streak by mutableStateOf(0)
        private set

    var bestStreak by mutableStateOf(0)
        private set

    var lastStreakDay by mutableStateOf<LocalDate?>(null)
        private set

    var filter by mutableStateOf(TaskFilter.ALL)
        private set

    var editorOpen by mutableStateOf(false)
        private set

    var editingTask by mutableStateOf<PlanTask?>(null)
        private set

    val storagePath: String get() = store.displayName

    init {
        restore()
    }

    private fun restore() {
        val todayDate = clock().toLocalDate()
        val saved = store.load()
        darkTheme = saved?.darkTheme ?: false
        bestStreak = saved?.bestStreak ?: 0
        lastStreakDay = saved?.lastStreakDay
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            // A streak stays alive while its last day is today or yesterday.
            ?.takeIf { !it.isBefore(todayDate.minusDays(1)) }
        streak = if (lastStreakDay == null) 0 else saved?.streak ?: 0
        if (streak > bestStreak) bestStreak = streak
        // A first launch starts with a clean day; never inject demo tasks into
        // someone's plan.
        tasks = saved?.tasks?.sortedBy { it.startMinute }.orEmpty()
        persist()
    }

    private fun persist() {
        store.save(
            PlannerFile(
                day = clock().toLocalDate().toString(),
                darkTheme = darkTheme,
                tasks = tasks,
                streak = streak,
                bestStreak = bestStreak,
                lastStreakDay = lastStreakDay?.toString().orEmpty(),
            ),
        )
    }

    fun selectFilter(value: TaskFilter) {
        filter = value
    }

    fun toggleTheme() {
        darkTheme = !darkTheme
        persist()
    }

    fun openNewTask() {
        editingTask = null
        editorOpen = true
    }

    fun openTask(task: PlanTask) {
        editingTask = task
        editorOpen = true
    }

    fun closeEditor() {
        editorOpen = false
        editingTask = null
    }

    fun saveDraft(draft: TaskDraft) {
        val edited = editingTask
        tasks = if (edited == null) {
            (tasks + draft.toTask()).sortedBy { it.startMinute }
        } else {
            tasks.map {
                if (it.id == edited.id) {
                    it.copy(
                        title = draft.title,
                        startMinute = draft.startMinute,
                        durationMinutes = draft.durationMinutes,
                        priority = draft.priority,
                        note = draft.note,
                    )
                } else {
                    it
                }
            }.sortedBy { it.startMinute }
        }
        closeEditor()
        persist()
    }

    fun toggleDone(id: String) {
        val current = tasks.firstOrNull { it.id == id } ?: return
        val nowDone = !current.done
        tasks = tasks.map { if (it.id == id) it.copy(done = nowDone) else it }
        if (nowDone) extendStreak()
        persist()
    }

    /** One completion a day is enough to keep the flame going. */
    private fun extendStreak() {
        val today = clock().toLocalDate()
        val last = lastStreakDay
        if (last == today) return
        streak = if (last == today.minusDays(1)) streak + 1 else 1
        lastStreakDay = today
        if (streak > bestStreak) bestStreak = streak
    }

    fun hasCompletedToday(): Boolean = lastStreakDay == clock().toLocalDate()

    /** Unfinished blocks whose scheduled time has already passed. */
    fun overdueTasks(nowMinute: Int): List<PlanTask> =
        tasks.filter { !it.done && it.endMinute <= nowMinute }.sortedBy { it.startMinute }

    /**
     * Pulls a slipped block to the next five-minute mark, keeping its length.
     * This is the one-tap recovery offered by the missed-block banner.
     */
    fun moveToNow(id: String) {
        val task = tasks.firstOrNull { it.id == id } ?: return
        if (task.done) return
        val now = clock()
        val minute = now.hour * 60 + now.minute
        val start = (((minute + 4) / 5) * 5).coerceAtMost(23 * 60 + 55)
        tasks = tasks
            .map { if (it.id == id) it.copy(startMinute = start) else it }
            .sortedBy { it.startMinute }
        persist()
    }

    fun delete(id: String) {
        tasks = tasks.filterNot { it.id == id }
        if (editingTask?.id == id) closeEditor()
        persist()
    }

    fun clearCompleted() {
        tasks = tasks.filterNot { it.done }
        persist()
    }

    fun visibleTasks(): List<PlanTask> {
        val sorted = tasks.sortedBy { it.startMinute }
        return when (filter) {
            TaskFilter.ALL -> sorted
            TaskFilter.TODO -> sorted.filterNot { it.done }
            TaskFilter.DONE -> sorted.filter { it.done }
        }
    }

    fun conflictsFor(task: PlanTask): List<PlanTask> =
        tasks.filter { it.id != task.id && it.overlaps(task) }

    fun stats(): DayStats = DayStats(
        total = tasks.size,
        done = tasks.count { it.done },
        plannedMinutes = tasks.sumOf { it.durationMinutes },
        doneMinutes = tasks.filter { it.done }.sumOf { it.durationMinutes },
    )

    fun activeTask(minuteOfDay: Int): PlanTask? =
        tasks.sortedBy { it.startMinute }.firstOrNull {
            !it.done && minuteOfDay >= it.startMinute && minuteOfDay < it.endMinute
        }

    fun nextTask(minuteOfDay: Int): PlanTask? =
        tasks.sortedBy { it.startMinute }.firstOrNull { !it.done && it.startMinute > minuteOfDay }

    private fun TaskDraft.toTask() = PlanTask(
        title = title,
        startMinute = startMinute,
        durationMinutes = durationMinutes,
        priority = priority,
        note = note,
    )
}

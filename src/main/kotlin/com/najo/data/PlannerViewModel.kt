package com.najo.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.najo.model.PlanTask
import com.najo.model.Priority
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
        val today = clock().toLocalDate().toString()
        val saved = store.load()
        darkTheme = saved?.darkTheme ?: false
        tasks = if (saved != null && saved.day == today) {
            saved.tasks.sortedBy { it.startMinute }
        } else {
            seedDay()
        }
        persist()
    }

    private fun persist() {
        store.save(
            PlannerFile(
                day = clock().toLocalDate().toString(),
                darkTheme = darkTheme,
                tasks = tasks,
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
        tasks = tasks.map { if (it.id == id) it.copy(done = !it.done) else it }
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

    fun restoreSampleDay() {
        tasks = seedDay()
        filter = TaskFilter.ALL
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

    private fun seedDay(): List<PlanTask> = listOf(
        PlanTask(
            title = "Morning pages & coffee",
            startMinute = 8 * 60 + 30,
            durationMinutes = 30,
            priority = Priority.LOW,
            done = true,
        ),
        PlanTask(
            title = "Plan the day, clear the inbox",
            startMinute = 9 * 60 + 15,
            durationMinutes = 45,
            priority = Priority.NORMAL,
            done = true,
            note = "Pick three things that matter.",
        ),
        PlanTask(
            title = "Deep work — Najo roadmap",
            startMinute = 10 * 60,
            durationMinutes = 90,
            priority = Priority.HIGH,
            note = "Phone in the drawer. One tab at a time.",
        ),
        PlanTask(
            title = "Lunch & a short walk",
            startMinute = 12 * 60 + 30,
            durationMinutes = 45,
            priority = Priority.LOW,
        ),
        PlanTask(
            title = "Design review with the team",
            startMinute = 14 * 60,
            durationMinutes = 60,
            priority = Priority.NORMAL,
        ),
        PlanTask(
            title = "Ship the weekly update",
            startMinute = 16 * 60,
            durationMinutes = 45,
            priority = Priority.HIGH,
        ),
        PlanTask(
            title = "Evening reset — tidy desk",
            startMinute = 18 * 60,
            durationMinutes = 30,
            priority = Priority.LOW,
        ),
        PlanTask(
            title = "Read, no screens",
            startMinute = 20 * 60 + 30,
            durationMinutes = 45,
            priority = Priority.LOW,
        ),
    )

    private fun TaskDraft.toTask() = PlanTask(
        title = title,
        startMinute = startMinute,
        durationMinutes = durationMinutes,
        priority = priority,
        note = note,
    )
}

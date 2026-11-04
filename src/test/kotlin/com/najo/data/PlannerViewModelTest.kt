package com.najo.data

import com.najo.model.PlanTask
import com.najo.model.Priority
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlannerViewModelTest {

    @TempDir
    lateinit var directory: Path

    private val today = LocalDateTime.of(2026, 11, 1, 12, 0)
    private lateinit var store: PlannerStore

    private val fixtureTasks = listOf(
        PlanTask(
            id = "morning",
            title = "Morning pages",
            startMinute = 8 * 60 + 30,
            durationMinutes = 30,
            priority = Priority.LOW,
            done = true,
        ),
        PlanTask(
            id = "plan",
            title = "Plan the day",
            startMinute = 9 * 60 + 15,
            durationMinutes = 45,
            done = true,
        ),
        PlanTask(
            id = "deep",
            title = "Deep work",
            startMinute = 10 * 60,
            durationMinutes = 90,
            priority = Priority.HIGH,
        ),
        PlanTask(
            id = "lunch",
            title = "Lunch",
            startMinute = 12 * 60 + 30,
            durationMinutes = 45,
            priority = Priority.LOW,
        ),
        PlanTask(
            id = "review",
            title = "Design review",
            startMinute = 14 * 60,
            durationMinutes = 60,
        ),
    )

    @BeforeEach
    fun setUp() {
        store = PlannerStore(directory.resolve("planner.json"))
    }

    private fun viewModel(now: LocalDateTime = today) = PlannerViewModel(store) { now }

    /** Loads a predictable plan instead of relying on production sample data. */
    private fun plannedViewModel(now: LocalDateTime = today): PlannerViewModel {
        store.save(
            PlannerFile(
                day = now.toLocalDate().toString(),
                tasks = fixtureTasks,
            ),
        )
        return viewModel(now)
    }

    @Test
    fun `first launch starts with a clean day`() {
        val vm = viewModel()

        assertTrue(vm.tasks.isEmpty())
        assertEquals(0, vm.stats().total)
        assertEquals(today.toLocalDate().toString(), store.load()?.day)
    }

    @Test
    fun `adding a task keeps the schedule ordered`() {
        val vm = plannedViewModel()
        vm.openNewTask()
        vm.saveDraft(
            TaskDraft(
                title = "Stretch",
                startMinute = 9 * 60 + 45,
                durationMinutes = 15,
                priority = Priority.LOW,
                note = "",
            ),
        )

        val titles = vm.tasks.map { it.title }
        assertEquals(6, titles.size)
        assertEquals("Stretch", titles[2])
        assertEquals("Deep work", titles[3])
        assertFalse(vm.editorOpen)
    }

    @Test
    fun `editing a task replaces its values`() {
        val vm = plannedViewModel()
        val target = vm.tasks.first { it.title == "Design review" }

        vm.openTask(target)
        vm.saveDraft(
            TaskDraft(
                title = "Design review (moved)",
                startMinute = 15 * 60,
                durationMinutes = 30,
                priority = Priority.HIGH,
                note = "Bring the deck",
            ),
        )

        val updated = vm.tasks.first { it.id == target.id }
        assertEquals("Design review (moved)", updated.title)
        assertEquals(15 * 60, updated.startMinute)
        assertEquals(30, updated.durationMinutes)
        assertEquals(Priority.HIGH, updated.priority)
        assertEquals("Bring the deck", updated.note)
    }

    @Test
    fun `ticking a task updates progress and survives a reload`() {
        val vm = plannedViewModel()
        val task = vm.tasks.first { !it.done }

        vm.toggleDone(task.id)

        assertEquals(3, vm.stats().done)
        assertTrue(vm.tasks.first { it.id == task.id }.done)
        assertTrue(PlannerStore(store.location).load()!!.tasks.first { it.id == task.id }.done)
    }

    @Test
    fun `filters expose only the requested tasks`() {
        val vm = plannedViewModel()

        vm.selectFilter(TaskFilter.DONE)
        assertTrue(vm.visibleTasks().all { it.done })
        assertEquals(2, vm.visibleTasks().size)

        vm.selectFilter(TaskFilter.TODO)
        assertTrue(vm.visibleTasks().none { it.done })
        assertEquals(3, vm.visibleTasks().size)
    }

    @Test
    fun `deleting and clearing keep the stats honest`() {
        val vm = plannedViewModel()
        val done = vm.tasks.first { it.done }

        vm.delete(done.id)
        assertEquals(4, vm.stats().total)

        vm.clearCompleted()
        assertEquals(3, vm.stats().total)
        assertEquals(0, vm.stats().done)
        assertEquals(0f, vm.stats().progress)
    }

    @Test
    fun `overlapping blocks are reported`() {
        val vm = plannedViewModel()
        vm.openNewTask()
        vm.saveDraft(
            TaskDraft(
                title = "Call",
                startMinute = 10 * 60 + 30,
                durationMinutes = 30,
                priority = Priority.NORMAL,
                note = "",
            ),
        )

        val call = vm.tasks.first { it.title == "Call" }
        val conflicts = vm.conflictsFor(call)

        assertEquals(1, conflicts.size)
        assertEquals("Deep work", conflicts.single().title)
    }

    @Test
    fun `focus and next task follow the clock`() {
        val vm = plannedViewModel()

        assertEquals("Deep work", vm.activeTask(10 * 60 + 15)?.title)
        assertEquals("Lunch", vm.nextTask(10 * 60 + 15)?.title)
        assertNull(vm.activeTask(23 * 60))
    }

    @Test
    fun `a plan from another day is kept instead of replaced`() {
        store.save(
            PlannerFile(
                day = "2020-01-01",
                darkTheme = true,
                tasks = listOf(
                    PlanTask(
                        title = "Carry over",
                        startMinute = 9 * 60,
                        durationMinutes = 30,
                    ),
                ),
            ),
        )

        val vm = viewModel()

        assertEquals(listOf("Carry over"), vm.tasks.map { it.title })
        assertTrue(vm.darkTheme)
        assertEquals(today.toLocalDate().toString(), store.load()?.day)
    }

    @Test
    fun `theme choice is remembered`() {
        val vm = viewModel()
        vm.toggleTheme()

        assertTrue(viewModel().darkTheme)
    }

    @Test
    fun `stats add up the planned minutes`() {
        val stats = plannedViewModel().stats()

        assertEquals(5, stats.total)
        assertEquals(2, stats.done)
        assertEquals(3, stats.todo)
        assertEquals(270, stats.plannedMinutes)
        assertEquals(75, stats.doneMinutes)
        assertEquals(195, stats.remainingMinutes)
        assertTrue(stats.progress > 0f)
    }

    @Test
    fun `a custom block longer than two hours survives a save`() {
        val vm = plannedViewModel()
        vm.openNewTask()
        vm.saveDraft(
            TaskDraft(
                title = "Workshop",
                startMinute = 18 * 60,
                durationMinutes = 240,
                priority = Priority.NORMAL,
                note = "",
            ),
        )

        assertEquals(240, vm.tasks.first { it.title == "Workshop" }.durationMinutes)
        assertEquals(270 + 240, vm.stats().plannedMinutes)
        assertEquals(
            240,
            store.load()!!.tasks.first { it.title == "Workshop" }.durationMinutes,
        )
    }

    @Test
    fun `overdue tasks are the unfinished blocks whose time has passed`() {
        val overdue = plannedViewModel().overdueTasks(14 * 60)

        assertEquals(
            listOf("Deep work", "Lunch"),
            overdue.map { it.title },
        )
    }

    @Test
    fun `a finished task starts and keeps a daily streak`() {
        val vm = plannedViewModel()
        assertEquals(0, vm.streak)
        assertFalse(vm.hasCompletedToday())

        val first = vm.tasks.first { !it.done }
        vm.toggleDone(first.id)

        assertEquals(1, vm.streak)
        assertTrue(vm.hasCompletedToday())
        assertEquals(1, PlannerStore(store.location).load()!!.streak)

        val second = vm.tasks.first { !it.done }
        vm.toggleDone(second.id)
        assertEquals(1, vm.streak)

        // Un-ticking does not take back a flame that was already earned today.
        vm.toggleDone(first.id)
        assertFalse(vm.tasks.first { it.id == first.id }.done)
        assertEquals(1, vm.streak)
    }

    @Test
    fun `a streak grows across back to back days`() {
        val dayOne = LocalDateTime.of(2026, 11, 1, 12, 0)
        val first = plannedViewModel(dayOne)
        first.toggleDone(first.tasks.first { !it.done }.id)
        assertEquals(1, first.streak)

        val dayTwo = PlannerViewModel(store) { dayOne.plusDays(1) }
        assertEquals(1, dayTwo.streak)
        assertFalse(dayTwo.hasCompletedToday())

        dayTwo.toggleDone(dayTwo.tasks.first { !it.done }.id)
        assertEquals(2, dayTwo.streak)
        assertEquals(2, dayTwo.bestStreak)
    }

    @Test
    fun `a skipped day resets the streak but keeps the best`() {
        val dayOne = LocalDateTime.of(2026, 11, 1, 12, 0)
        val first = plannedViewModel(dayOne)
        first.toggleDone(first.tasks.first { !it.done }.id)

        val dayThree = PlannerViewModel(store) { dayOne.plusDays(2) }
        assertEquals(0, dayThree.streak)
        assertEquals(1, dayThree.bestStreak)
        assertFalse(dayThree.hasCompletedToday())

        dayThree.toggleDone(dayThree.tasks.first { !it.done }.id)
        assertEquals(1, dayThree.streak)
        assertEquals(1, dayThree.bestStreak)
    }
}

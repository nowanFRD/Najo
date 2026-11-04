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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlannerViewModelTest {

    @TempDir
    lateinit var directory: Path

    private val today = LocalDateTime.of(2026, 11, 1, 12, 0)
    private lateinit var store: PlannerStore

    @BeforeEach
    fun setUp() {
        store = PlannerStore(directory.resolve("planner.json"))
    }

    private fun viewModel(now: LocalDateTime = today) = PlannerViewModel(store) { now }

    @Test
    fun `first launch seeds a sample day for today`() {
        val vm = viewModel()

        assertEquals(8, vm.tasks.size)
        assertEquals(today.toLocalDate().toString(), store.load()?.day)
    }

    @Test
    fun `adding a task keeps the schedule ordered`() {
        val vm = viewModel()
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
        assertEquals(9, titles.size)
        assertEquals("Stretch", titles[2])
        assertEquals("Deep work — Najo roadmap", titles[3])
        assertFalse(vm.editorOpen)
    }

    @Test
    fun `editing a task replaces its values`() {
        val vm = viewModel()
        val target = vm.tasks.first { it.title == "Design review with the team" }

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
        val vm = viewModel()
        val task = vm.tasks.first { !it.done }

        vm.toggleDone(task.id)

        assertEquals(3, vm.stats().done)
        assertTrue(vm.tasks.first { it.id == task.id }.done)
        assertTrue(PlannerStore(store.location).load()!!.tasks.first { it.id == task.id }.done)
    }

    @Test
    fun `filters expose only the requested tasks`() {
        val vm = viewModel()

        vm.selectFilter(TaskFilter.DONE)
        assertTrue(vm.visibleTasks().all { it.done })
        assertEquals(2, vm.visibleTasks().size)

        vm.selectFilter(TaskFilter.TODO)
        assertTrue(vm.visibleTasks().none { it.done })
        assertEquals(6, vm.visibleTasks().size)
    }

    @Test
    fun `deleting and clearing keep the stats honest`() {
        val vm = viewModel()
        val done = vm.tasks.first { it.done }

        vm.delete(done.id)
        assertEquals(7, vm.stats().total)

        vm.clearCompleted()
        assertEquals(6, vm.stats().total)
        assertEquals(0, vm.stats().done)
        assertEquals(0, vm.stats().progress.toInt())
    }

    @Test
    fun `overlapping blocks are reported`() {
        val vm = viewModel()
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
        assertEquals("Deep work — Najo roadmap", conflicts.single().title)
    }

    @Test
    fun `focus and next task follow the clock`() {
        val vm = viewModel()

        assertEquals("Deep work — Najo roadmap", vm.activeTask(10 * 60 + 15)?.title)
        assertEquals("Lunch & a short walk", vm.nextTask(10 * 60 + 15)?.title)
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
        val stats = viewModel().stats()

        assertEquals(8, stats.total)
        assertEquals(2, stats.done)
        assertEquals(6, stats.todo)
        assertEquals(390, stats.plannedMinutes)
        assertEquals(75, stats.doneMinutes)
        assertEquals(315, stats.remainingMinutes)
        assertNotNull(stats.progress)
    }

    @Test
    fun `a custom block longer than two hours survives a save`() {
        val vm = viewModel()
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
        assertEquals(390 + 240, vm.stats().plannedMinutes)
        assertEquals(
            240,
            store.load()!!.tasks.first { it.title == "Workshop" }.durationMinutes,
        )
    }

    @Test
    fun `overdue tasks are the unfinished blocks whose time has passed`() {
        val overdue = viewModel().overdueTasks(14 * 60)

        assertEquals(
            listOf("Deep work — Najo roadmap", "Lunch & a short walk"),
            overdue.map { it.title },
        )
    }

    @Test
    fun `a finished task starts and keeps a daily streak`() {
        val vm = viewModel()
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
        val first = PlannerViewModel(store) { dayOne }
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
        val first = PlannerViewModel(store) { dayOne }
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

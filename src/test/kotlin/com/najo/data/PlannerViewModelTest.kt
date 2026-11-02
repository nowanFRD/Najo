package com.najo.data

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
    fun `a plan from another day starts fresh`() {
        store.save(PlannerFile(day = "2020-01-01", darkTheme = true, tasks = emptyList()))

        val vm = viewModel()

        assertEquals(8, vm.tasks.size)
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
}

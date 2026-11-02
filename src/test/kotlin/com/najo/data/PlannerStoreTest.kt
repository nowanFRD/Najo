package com.najo.data

import com.najo.model.PlanTask
import com.najo.model.Priority
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlannerStoreTest {

    @TempDir
    lateinit var directory: Path

    private fun store(name: String = "planner.json") = PlannerStore(directory.resolve(name))

    @Test
    fun `missing file loads as null`() {
        assertNull(store().load())
    }

    @Test
    fun `plan survives a save and load round trip`() {
        val plan = PlannerFile(
            day = "2026-11-01",
            darkTheme = true,
            tasks = listOf(
                PlanTask(
                    title = "Deep work",
                    startMinute = 10 * 60,
                    durationMinutes = 90,
                    priority = Priority.HIGH,
                    note = "One tab at a time",
                    done = false,
                ),
                PlanTask(
                    title = "Walk",
                    startMinute = 17 * 60,
                    durationMinutes = 30,
                    priority = Priority.LOW,
                    done = true,
                ),
            ),
        )

        store().save(plan)

        val loaded = store().load()
        assertNotNull(loaded)
        assertEquals(plan.day, loaded.day)
        assertTrue(loaded.darkTheme)
        assertEquals(plan.tasks, loaded.tasks)
    }

    @Test
    fun `save creates the parent directory`() {
        val nested = PlannerStore(directory.resolve("deep/nested/planner.json"))
        nested.save(PlannerFile(day = "2026-11-01"))

        assertTrue(Files.exists(directory.resolve("deep/nested/planner.json")))
    }

    @Test
    fun `damaged file is ignored instead of crashing`() {
        val path = directory.resolve("broken.json")
        Files.writeString(path, "{ this is not json")

        assertNull(PlannerStore(path).load())
    }
}

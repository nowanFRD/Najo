package com.najo.data

import com.najo.model.PlanTask
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/** Everything Najo needs to remember between launches. */
@Serializable
data class PlannerFile(
    val day: String = "",
    val darkTheme: Boolean = false,
    val tasks: List<PlanTask> = emptyList(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastStreakDay: String = "",
)

/**
 * Stores the day plan as a single readable JSON file in ~/.najo/planner.json.
 * Writes are atomic (temp file + move) so a crash cannot leave a broken plan.
 */
class PlannerStore(
    private val file: Path,
    /** Human friendly location shown in the UI. */
    val displayName: String = file.toString(),
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val location: Path get() = file

    fun load(): PlannerFile? = runCatching {
        if (!Files.exists(file)) return null
        val text = Files.readAllBytes(file).toString(Charsets.UTF_8)
        if (text.isBlank()) return null
        json.decodeFromString(PlannerFile.serializer(), text)
    }.getOrNull()

    fun save(state: PlannerFile) {
        runCatching {
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling("${file.fileName}.tmp")
            Files.write(temp, json.encodeToString(PlannerFile.serializer(), state).toByteArray(Charsets.UTF_8))
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
        }.onFailure { error ->
            System.err.println("Najo: could not save plan to $file (${error.message})")
        }
    }

    companion object {
        fun defaultFile(): Path = Paths.get(System.getProperty("user.home"), ".najo", "planner.json")
    }
}

package com.najo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.najo.data.PlannerViewModel
import com.najo.data.TaskFilter
import com.najo.model.DaySection
import com.najo.model.PlanTask
import com.najo.ui.components.EmptyState
import com.najo.ui.components.FilterBar
import com.najo.ui.components.HeroCard
import com.najo.ui.components.MotivationBanner
import com.najo.ui.components.PlusIcon
import com.najo.ui.components.SectionHeader
import com.najo.ui.components.StatsPanel
import com.najo.ui.components.TaskCard
import com.najo.ui.components.TaskEditorDialog
import com.najo.ui.components.TopBar
import com.najo.ui.theme.NajoPalette
import com.najo.util.currentMinuteOfDay
import com.najo.util.greeting
import kotlinx.coroutines.delay
import java.time.LocalDate

/** Below this width (or height) the side panel folds away and the list takes the whole screen. */
private val WideLayoutBreakpoint = 900.dp
private val WideLayoutMinHeight = 440.dp

@Composable
fun PlannerScreen(vm: PlannerViewModel) {
    val colors = MaterialTheme.colorScheme
    val nowMinute = rememberNowMinute()
    val day = LocalDate.now()
    val stats = vm.stats()
    val visible = vm.visibleTasks()
    val overdue = vm.overdueTasks(nowMinute)
    val grouped = visible.groupBy { DaySection.of(it.startMinute) }
    val counts = mapOf(
        TaskFilter.ALL to stats.total,
        TaskFilter.TODO to stats.todo,
        TaskFilter.DONE to stats.done,
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(NajoPalette.extras.backgroundGradient)),
    ) {
        val compact = maxWidth < WideLayoutBreakpoint || maxHeight < WideLayoutMinHeight
        val dense = compact && maxHeight < 520.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    horizontal = if (compact) 16.dp else 26.dp,
                    vertical = if (compact) 12.dp else 20.dp,
                ),
        ) {
            TopBar(
                date = day,
                isDark = vm.darkTheme,
                onToggleTheme = vm::toggleTheme,
                compact = compact,
            )
            Spacer(
                Modifier.height(
                    when {
                        dense -> 6.dp
                        compact -> 12.dp
                        else -> 16.dp
                    },
                ),
            )
            HeroCard(
                date = day,
                greeting = greeting(nowMinute / 60),
                stats = stats,
                streak = vm.streak,
                onAddTask = vm::openNewTask,
                compact = compact,
                dense = dense,
            )
            Spacer(
                Modifier.height(
                    when {
                        dense -> 6.dp
                        compact -> 12.dp
                        else -> 16.dp
                    },
                ),
            )

            if (compact) {
                if (overdue.isNotEmpty()) {
                    MotivationBanner(
                        overdue = overdue,
                        onOpenTask = vm::openTask,
                        onMoveToNow = vm::moveToNow,
                        compact = dense,
                    )
                    Spacer(Modifier.height(if (dense) 6.dp else 10.dp))
                }
                FilterBar(
                    selected = vm.filter,
                    counts = counts,
                    onSelect = vm::selectFilter,
                    doneCount = stats.done,
                    onClearDone = vm::clearCompleted,
                )
                ScheduleList(
                    vm = vm,
                    visible = visible,
                    grouped = grouped,
                    compact = true,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Row(Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (overdue.isNotEmpty()) {
                            MotivationBanner(
                                overdue = overdue,
                                onOpenTask = vm::openTask,
                                onMoveToNow = vm::moveToNow,
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        FilterBar(
                            selected = vm.filter,
                            counts = counts,
                            onSelect = vm::selectFilter,
                            doneCount = stats.done,
                            onClearDone = vm::clearCompleted,
                        )
                        Spacer(Modifier.height(4.dp))
                        ScheduleList(
                            vm = vm,
                            visible = visible,
                            grouped = grouped,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.width(18.dp))
                    StatsPanel(
                        nowMinute = nowMinute,
                        tasks = vm.tasks,
                        stats = stats,
                        streak = vm.streak,
                        bestStreak = vm.bestStreak,
                        lastStreakDay = vm.lastStreakDay,
                        today = day,
                        storagePath = vm.storagePath,
                        modifier = Modifier.width(334.dp).fillMaxHeight(),
                    )
                }
            }
        }

        if (compact) {
            FloatingActionButton(
                onClick = vm::openNewTask,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(20.dp),
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            ) {
                PlusIcon(Modifier.size(22.dp), colors.onPrimary, strokeWidth = 2.6.dp)
            }
        }

        if (vm.editorOpen) {
            val editing = vm.editingTask
            TaskEditorDialog(
                initial = editing,
                onDismiss = vm::closeEditor,
                onSave = vm::saveDraft,
                onDelete = if (editing == null) {
                    null
                } else {
                    { vm.delete(editing.id) }
                },
            )
        }
    }
}

@Composable
private fun ScheduleList(
    vm: PlannerViewModel,
    visible: List<PlanTask>,
    grouped: Map<DaySection, List<PlanTask>>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (visible.isEmpty()) {
        EmptyState(
            title = when (vm.filter) {
                TaskFilter.ALL -> "A fresh page"
                TaskFilter.TODO -> "Nothing left to do"
                TaskFilter.DONE -> "Nothing finished yet"
            },
            subtitle = when (vm.filter) {
                TaskFilter.ALL -> "Add your first block and let the day take shape."
                TaskFilter.TODO -> "Every task is ticked off. Take the win."
                TaskFilter.DONE -> "Tick something off and it will show up here."
            },
            actionLabel = if (vm.filter == TaskFilter.ALL) "Add a task" else null,
            onAction = if (vm.filter == TaskFilter.ALL) vm::openNewTask else null,
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = if (compact) 92.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        DaySection.entries.forEach { section ->
            val sectionTasks = grouped[section].orEmpty()
            if (sectionTasks.isEmpty()) return@forEach

            item(key = "header-${section.name}") {
                SectionHeader(section, sectionTasks.size, compact)
            }
            items(sectionTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    conflicts = vm.conflictsFor(task),
                    onToggle = { vm.toggleDone(task.id) },
                    onEdit = { vm.openTask(task) },
                    onDelete = { vm.delete(task.id) },
                    compact = compact,
                )
            }
        }
    }
}

/** Re-reads the clock every half minute so "focus now" stays honest. */
@Composable
private fun rememberNowMinute(): Int {
    var minute by remember { mutableStateOf(currentMinuteOfDay()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            minute = currentMinuteOfDay()
        }
    }
    return minute
}

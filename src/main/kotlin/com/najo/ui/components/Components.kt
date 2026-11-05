package com.najo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najo.data.DayStats
import com.najo.data.TaskFilter
import com.najo.model.DaySection
import com.najo.model.PlanTask
import com.najo.model.Priority
import com.najo.ui.theme.NajoPalette
import com.najo.util.formatDateShort
import com.najo.util.formatDateUpper
import com.najo.util.formatDuration
import com.najo.util.formatRange
import com.najo.util.formatTime
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> MaterialTheme.colorScheme.primary
    Priority.NORMAL -> MaterialTheme.colorScheme.tertiary
    Priority.LOW -> MaterialTheme.colorScheme.outline
}

/* ------------------------------------------------------------------------- */
/*  Brand                                                                     */
/* ------------------------------------------------------------------------- */

/** The launcher artwork, supplied by each platform; null keeps the vector fallback. */
val LocalBrandPainter = staticCompositionLocalOf<Painter?> { null }

@Composable
fun BrandMark(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val painter = LocalBrandPainter.current
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF60A5FA)))),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            NajoMark(Modifier.size(size * 0.62f), Color.Transparent, Color.White)
        }
    }
}

@Composable
fun TopBar(
    date: LocalDate,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark(size = if (compact) 36.dp else 40.dp)
        Spacer(Modifier.width(if (compact) 9.dp else 12.dp))
        Column {
            Text(
                "Najo",
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                color = colors.onBackground,
            )
            if (!compact) {
                Text(
                    "plan your day, gently",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(
            shape = CircleShape,
            color = colors.surface,
            border = BorderStroke(1.dp, NajoPalette.extras.cardOutline),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (compact) 11.dp else 14.dp,
                    vertical = if (compact) 7.dp else 8.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ClockIcon(Modifier.size(14.dp), colors.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(
                    formatDateShort(date),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(if (compact) 8.dp else 10.dp))
        SoftIconButton(
            onClick = onToggleTheme,
            size = if (compact) 36.dp else 38.dp,
            tooltip = if (isDark) "Switch to light" else "Switch to dark",
        ) {
            if (isDark) {
                SunIcon(Modifier.size(18.dp), colors.primary)
            } else {
                MoonIcon(Modifier.size(17.dp), colors.primary)
            }
        }
    }
}

@Composable
fun SoftIconButton(
    onClick: () -> Unit,
    size: Dp = 38.dp,
    tooltip: String = "",
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val background by animateColorAsState(
        if (hovered) colors.primaryContainer.copy(alpha = 0.65f) else colors.surface,
        animationSpec = tween(150),
        label = "icon-background",
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .hoverable(interaction)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/* ------------------------------------------------------------------------- */
/*  Hero                                                                      */
/* ------------------------------------------------------------------------- */

@Composable
fun HeroCard(
    date: LocalDate,
    greeting: String,
    stats: DayStats,
    streak: Int,
    onAddTask: () -> Unit,
    compact: Boolean = false,
    dense: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(NajoPalette.heroBrush()),
    ) {
        // Soft decorative glow, drawn behind the content.
        androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.minDimension * 0.75f,
                center = Offset(size.width * 0.92f, size.height * 0.15f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.minDimension * 0.5f,
                center = Offset(size.width * 0.78f, size.height * 1.15f),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (dense) 18.dp else if (compact) 20.dp else 28.dp,
                    vertical = if (dense) 10.dp else if (compact) 18.dp else 24.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!dense) {
                        Text(
                            formatDateUpper(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f),
                            letterSpacing = 1.4.sp,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    StreakBadge(streak = streak, compact = compact)
                }
                Spacer(Modifier.height(if (dense) 4.dp else 6.dp))
                Text(
                    "$greeting.",
                    style = when {
                        dense -> MaterialTheme.typography.titleLarge
                        compact -> MaterialTheme.typography.headlineLarge
                        else -> MaterialTheme.typography.displaySmall
                    },
                    color = Color.White,
                )
                Spacer(Modifier.height(if (dense) 4.dp else if (compact) 6.dp else 8.dp))
                Text(
                    buildString {
                        append(
                            when {
                                stats.total == 0 -> "No tasks yet"
                                stats.todo == 0 -> "All tasks done"
                                stats.todo == 1 -> "1 task left"
                                else -> "${stats.todo} tasks left"
                            },
                        )
                        append("  ·  ")
                        append("${formatDuration(stats.plannedMinutes)} planned")
                        if (stats.done > 0 && stats.todo > 0 && !compact) {
                            append("  ·  ")
                            append("${stats.done} done")
                        }
                    },
                    style = if (dense) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    color = Color.White.copy(alpha = 0.88f),
                )
                if (!compact) {
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = onAddTask,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1D4ED8),
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 18.dp,
                            vertical = 10.dp,
                        ),
                    ) {
                        PlusIcon(Modifier.size(15.dp), Color(0xFF1D4ED8), strokeWidth = 2.4.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Add task", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(Modifier.width(if (dense) 8.dp else if (compact) 10.dp else 20.dp))
            HeroProgress(stats, compact, dense)
        }
    }
}

@Composable
private fun HeroProgress(stats: DayStats, compact: Boolean, dense: Boolean) {
    val progress by animateFloatAsState(stats.progress, animationSpec = tween(600), label = "hero-progress")
    Box(contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(
            Modifier.size(if (dense) 64.dp else if (compact) 84.dp else 116.dp),
        ) {
            val stroke = (if (dense) 7.dp else if (compact) 8.dp else 10.dp).toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (progress > 0f) {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(progress * 100).roundToInt()}%",
                style = when {
                    dense -> MaterialTheme.typography.titleMedium
                    compact -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.headlineMedium
                },
                color = Color.White,
            )
            Text(
                if (compact) "${stats.done}/${stats.total}" else "${stats.done} of ${stats.total} done",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

/* ------------------------------------------------------------------------- */
/*  Filters                                                                   */
/* ------------------------------------------------------------------------- */

@Composable
fun FilterBar(
    selected: TaskFilter,
    counts: Map<TaskFilter, Int>,
    onSelect: (TaskFilter) -> Unit,
    doneCount: Int,
    onClearDone: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskFilter.entries.forEach { filter ->
                FilterPill(
                    label = filter.label,
                    count = counts[filter] ?: 0,
                    selected = filter == selected,
                    onClick = { onSelect(filter) },
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (doneCount > 0) {
            TextButton(onClick = onClearDone) {
                Text(
                    "Clear done",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) colors.primary else colors.surface,
        animationSpec = tween(180),
        label = "pill-background",
    )
    val content by animateColorAsState(
        if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = tween(180),
        label = "pill-content",
    )
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .then(
                if (selected) Modifier else Modifier.drawBehind {
                    drawRoundRect(
                        color = colors.outlineVariant,
                        cornerRadius = CornerRadius(size.height / 2f),
                        style = Stroke(1.dp.toPx()),
                    )
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = content)
        Spacer(Modifier.width(7.dp))
        Text(
            count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onPrimary.copy(alpha = 0.75f) else colors.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

/* ------------------------------------------------------------------------- */
/*  Section heading                                                           */
/* ------------------------------------------------------------------------- */

@Composable
fun SectionHeader(section: DaySection, count: Int, compact: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (compact) 62.dp else 78.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            section.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            letterSpacing = 1.1.sp,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.55f),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.outlineVariant.copy(alpha = 0.8f)),
        )
    }
}

/* ------------------------------------------------------------------------- */
/*  Side panel                                                                */
/* ------------------------------------------------------------------------- */

@Composable
fun StatsPanel(
    nowMinute: Int,
    tasks: List<PlanTask>,
    stats: DayStats,
    streak: Int,
    bestStreak: Int,
    lastStreakDay: LocalDate?,
    today: LocalDate,
    storagePath: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 4.dp, end = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StreakCard(
            streak = streak,
            bestStreak = bestStreak,
            lastStreakDay = lastStreakDay,
            today = today,
        )
        FocusCard(nowMinute = nowMinute, tasks = tasks)
        ProgressCard(stats = stats)
        TimelineCard(nowMinute = nowMinute, tasks = tasks)
        Text(
            "Autosaved · $storagePath",
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
fun PanelCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NajoPalette.extras.cardOutline),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.1.sp,
                )
                Spacer(Modifier.weight(1f))
                if (trailing != null) trailing()
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun FocusCard(nowMinute: Int, tasks: List<PlanTask>) {
    val sorted = remember(tasks) { tasks.sortedBy { it.startMinute } }
    val active = sorted.firstOrNull {
        !it.done && nowMinute >= it.startMinute && nowMinute < it.endMinute
    }
    val upcoming = sorted.firstOrNull { !it.done && it.startMinute > nowMinute }
    val allDone = tasks.isNotEmpty() && tasks.all { it.done }
    val colors = MaterialTheme.colorScheme
    val extras = NajoPalette.extras

    PanelCard(
        title = if (active != null) "Focus now" else "Up next",
        trailing = {
            if (active != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(extras.success),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "in progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.success,
                    )
                }
            }
        },
    ) {
        when {
            active != null -> {
                Text(active.title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Spacer(Modifier.height(5.dp))
                Text(
                    "${formatRange(active.startMinute, active.durationMinutes)} · " +
                        "${formatDuration(active.endMinute - nowMinute)} left",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                val elapsed = (nowMinute - active.startMinute).toFloat() / active.durationMinutes
                ThickProgress(
                    progress = elapsed.coerceIn(0f, 1f),
                    color = colors.primary,
                    track = colors.surfaceVariant,
                )
            }

            upcoming != null -> {
                Text(upcoming.title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Starts at ${formatTime(upcoming.startMinute)} · " +
                        "in ${formatDuration(upcoming.startMinute - nowMinute)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }

            allDone -> {
                Text("Day complete", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Everything is ticked off. Enjoy the evening.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }

            else -> {
                Text("A clear page", style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Spacer(Modifier.height(5.dp))
                Text(
                    "Nothing scheduled yet. Add a task to give the day a shape.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ThickProgress(progress: Float, color: Color, track: Color) {
    val animated by animateFloatAsState(progress, animationSpec = tween(500), label = "thick-progress")
    Box(
        Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(CircleShape)
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun ProgressCard(stats: DayStats) {
    val colors = MaterialTheme.colorScheme
    val progress by animateFloatAsState(stats.progress, animationSpec = tween(600), label = "progress-ring")
    PanelCard(title = "Progress") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(Modifier.size(84.dp)) {
                    val stroke = 8.dp.toPx()
                    drawArc(
                        color = colors.surfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                    if (progress > 0f) {
                        drawArc(
                            color = colors.primary,
                            startAngle = -90f,
                            sweepAngle = 360f * progress.coerceIn(0f, 1f),
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                        )
                    }
                }
                Text(
                    "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                StatLine("Done", "${stats.done} / ${stats.total}")
                StatLine("Planned", formatDuration(stats.plannedMinutes))
                StatLine("Finished", formatDuration(stats.doneMinutes))
                StatLine("Remaining", formatDuration(stats.remainingMinutes))
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun TimelineCard(nowMinute: Int, tasks: List<PlanTask>) {
    val colors = MaterialTheme.colorScheme
    PanelCard(title = "Day at a glance") {
        androidx.compose.foundation.Canvas(
            Modifier
                .fillMaxWidth()
                .height(34.dp),
        ) {
            val startOfDay = 6 * 60
            val span = 18 * 60f
            val height = 10.dp.toPx()
            val top = (size.height - height) / 2f
            drawRoundRect(
                color = colors.surfaceVariant,
                topLeft = Offset(0f, top),
                size = Size(size.width, height),
                cornerRadius = CornerRadius(height / 2f),
            )
            tasks.forEach { task ->
                val from = task.startMinute.coerceIn(startOfDay, startOfDay + span.toInt())
                val to = task.endMinute.coerceIn(startOfDay, startOfDay + span.toInt())
                if (to <= from) return@forEach
                val x = (from - startOfDay) / span * size.width
                val width = ((to - from) / span * size.width).coerceAtLeast(2.dp.toPx())
                val base = when {
                    task.done -> colors.primary.copy(alpha = 0.35f)
                    task.priority == Priority.HIGH -> colors.primary
                    task.priority == Priority.NORMAL -> colors.tertiary
                    else -> colors.outline
                }
                drawRoundRect(
                    color = if (task.done) base else base.copy(alpha = 0.85f),
                    topLeft = Offset(x, top - 2.dp.toPx()),
                    size = Size(width.coerceAtMost(size.width - x), height + 4.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }
            if (nowMinute in startOfDay..(startOfDay + span.toInt())) {
                val x = (nowMinute - startOfDay) / span * size.width
                drawLine(
                    color = colors.onSurface,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(colors.onSurface, radius = 2.5.dp.toPx(), center = Offset(x, size.height - 2.dp.toPx()))
            }
        }
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("06:00", "12:00", "18:00", "24:00").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(Priority.HIGH, Priority.NORMAL, Priority.LOW).forEach { priority ->
                val matching = tasks.filter { it.priority == priority }
                LegendDot(
                    "${priority.label} ${matching.count { it.done }}/${matching.size}",
                    priorityColor(priority),
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ------------------------------------------------------------------------- */
/*  Empty state                                                               */
/* ------------------------------------------------------------------------- */

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            CheckIcon(Modifier.size(26.dp), colors.primary, strokeWidth = 2.6.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(300.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
            ) {
                PlusIcon(Modifier.size(14.dp), colors.onPrimary, strokeWidth = 2.4.dp)
                Spacer(Modifier.width(7.dp))
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/* ------------------------------------------------------------------------- */
/*  Task card                                                                 */
/* ------------------------------------------------------------------------- */

@Composable
fun TaskCard(
    task: PlanTask,
    conflicts: List<PlanTask>,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val extras = NajoPalette.extras
    val accent = priorityColor(task.priority)
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val actionAlpha by animateFloatAsState(if (hovered) 1f else 0.45f, tween(150), label = "actions")
    val container by animateColorAsState(
        if (hovered) colors.primaryContainer.copy(alpha = if (task.done) 0.22f else 0.28f) else colors.surface,
        tween(160),
        label = "card-background",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
    ) {
        Column(
            modifier = Modifier.width(if (compact) 50.dp else 60.dp).padding(top = 13.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                formatTime(task.startMinute),
                style = MaterialTheme.typography.titleSmall,
                color = if (task.done) colors.onSurfaceVariant.copy(alpha = 0.6f) else colors.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                formatDuration(task.durationMinutes),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Box(Modifier.width(if (compact) 16.dp else 20.dp).fillMaxHeight()) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val x = size.width / 2f
                drawLine(
                    color = colors.outlineVariant,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
                drawCircle(
                    color = if (task.done) colors.primary.copy(alpha = 0.35f) else accent,
                    radius = 4.dp.toPx(),
                    center = Offset(x, 23.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 9.dp)
                .clip(RoundedCornerShape(18.dp))
                .hoverable(interaction)
                .then(if (compact) Modifier.clickable(onClick = onEdit) else Modifier),
            shape = RoundedCornerShape(18.dp),
            color = container,
            border = BorderStroke(1.dp, if (hovered) colors.primary.copy(alpha = 0.25f) else extras.cardOutline),
        ) {
            Row(
                modifier = Modifier.padding(
                    start = if (compact) 10.dp else 12.dp,
                    end = if (compact) 8.dp else 10.dp,
                    top = 12.dp,
                    bottom = 12.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(if (compact) 34.dp else 38.dp)
                        .clip(CircleShape)
                        .background(if (task.done) colors.outlineVariant else accent),
                )
                Spacer(Modifier.width(if (compact) 9.dp else 12.dp))
                CircleCheck(checked = task.done, onClick = onToggle)
                Spacer(Modifier.width(if (compact) 9.dp else 12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.done) colors.onSurfaceVariant.copy(alpha = 0.65f) else colors.onSurface,
                        textDecoration = if (task.done) {
                            androidx.compose.ui.text.style.TextDecoration.LineThrough
                        } else {
                            null
                        },
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            formatRange(task.startMinute, task.durationMinutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant.copy(alpha = 0.85f),
                        )
                        if (task.note.isNotBlank()) {
                            Text(
                                "  ·  ${task.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (conflicts.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Overlaps ${conflicts.joinToString { formatTime(it.startMinute) }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = extras.warning,
                        )
                    }
                }
                if (!compact) {
                    Spacer(Modifier.width(6.dp))
                    Row(
                        modifier = Modifier.graphicsLayer { alpha = actionAlpha },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SoftIconButton(onClick = onEdit, size = 30.dp) {
                            PencilIcon(Modifier.size(15.dp), colors.onSurfaceVariant)
                        }
                        Spacer(Modifier.width(4.dp))
                        SoftIconButton(onClick = onDelete, size = 30.dp) {
                            TrashIcon(Modifier.size(15.dp), colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleCheck(checked: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val fill by animateColorAsState(
        if (checked) colors.primary else Color.Transparent,
        tween(180),
        label = "check-fill",
    )
    val border by animateColorAsState(
        if (checked) colors.primary else colors.outline,
        tween(180),
        label = "check-border",
    )
    val tick by animateFloatAsState(if (checked) 1f else 0f, tween(180), label = "check-tick")
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(fill)
            .drawBehind {
                drawCircle(color = border, radius = size.minDimension / 2f - 1.dp.toPx(), style = Stroke(1.6.dp.toPx()))
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (tick > 0.05f) {
            CheckIcon(
                Modifier
                    .size(15.dp)
                    .graphicsLayer { alpha = tick },
                if (checked) colors.onPrimary else colors.primary,
                strokeWidth = 2.6.dp,
            )
        }
    }
}


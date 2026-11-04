package com.najo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najo.model.PlanTask
import com.najo.ui.theme.NajoPalette
import com.najo.util.formatTime
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val FlameGradient = listOf(Color(0xFFFFB020), Color(0xFFFF7A00), Color(0xFFFF2D6D))
private val FlameDot = Color(0xFFFF7200)

private val MissedMessages = listOf(
    "A late start still counts. Pick one up now.",
    "Five minutes is enough to begin again.",
    "Don't write off the day for one missed block.",
    "Start with the smallest one — momentum follows.",
    "The clock moved on. You can still move with it.",
)

/* ------------------------------------------------------------------------- */
/*  Streak                                                                    */
/* ------------------------------------------------------------------------- */

@Composable
fun StreakBadge(
    streak: Int,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val active = streak > 0
    var pulse by remember { mutableStateOf(false) }
    var previous by remember { mutableStateOf(streak) }

    LaunchedEffect(streak) {
        if (streak > previous) {
            pulse = true
            delay(420)
            pulse = false
        }
        previous = streak
    }

    val scale by animateFloatAsState(
        targetValue = if (pulse) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "streak-pop",
    )

    Row(
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = if (streak == 0) {
                    "No streak yet. Complete a task to start one."
                } else {
                    "$streak day streak"
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .then(
                if (active) {
                    Modifier.background(Brush.linearGradient(FlameGradient))
                } else {
                    Modifier
                        .background(colors.surface)
                        .border(1.dp, colors.outlineVariant, CircleShape)
                },
            )
            .padding(horizontal = if (compact) 9.dp else 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlameIcon(
            Modifier.size(if (compact) 13.dp else 15.dp),
            if (active) Color.White else colors.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            streak.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else colors.onSurfaceVariant,
        )
        if (!compact) {
            Text(
                " day streak",
                style = MaterialTheme.typography.labelMedium,
                color = if (active) Color.White.copy(alpha = 0.9f) else colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StreakCard(
    streak: Int,
    bestStreak: Int,
    lastStreakDay: LocalDate?,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val completedToday = lastStreakDay == today
    val alive = lastStreakDay != null && !lastStreakDay.isBefore(today.minusDays(1))
    val message = when {
        completedToday -> "You showed up today. Keep the flame warm."
        alive && streak > 0 -> "Tick one task to keep your $streak-day streak alive."
        else -> "Tick any task to light the flame."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(FlameGradient))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp)),
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.95f, size.height * 0.1f),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.8f, size.height * 1.2f),
            )
        }
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    FlameIcon(Modifier.size(28.dp), Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            streak.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (streak == 1) "day" else "days",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Text(
                        "STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        letterSpacing = 1.1.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "BEST",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.1.sp,
                    )
                    Text(
                        bestStreak.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            WeekDots(today = today, lastStreakDay = lastStreakDay, streak = streak)
            Spacer(Modifier.height(11.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Composable
private fun WeekDots(today: LocalDate, lastStreakDay: LocalDate?, streak: Int) {
    val days = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        days.forEach { day ->
            val onStreak = if (lastStreakDay != null && streak > 0) {
                !day.isAfter(lastStreakDay) &&
                    day.isAfter(lastStreakDay.minusDays(streak.toLong()))
            } else {
                false
            }
            val isToday = day == today
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.ENGLISH),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (onStreak) Color.White else Color.White.copy(alpha = 0.16f),
                        )
                        .then(
                            if (isToday && !onStreak) {
                                Modifier.border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (onStreak) {
                        FlameIcon(Modifier.size(11.dp), FlameDot)
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------------- */
/*  Gentle nudge for blocks that slipped past                                 */
/* ------------------------------------------------------------------------- */

@Composable
fun MotivationBanner(
    overdue: List<PlanTask>,
    onOpenTask: (PlanTask) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (overdue.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val warning = NajoPalette.extras.warning
    val first = overdue.first()
    val message = remember(overdue.size, first.id) {
        MissedMessages[(overdue.size + first.startMinute) % MissedMessages.size]
    }
    val title = if (overdue.size == 1) {
        "The ${formatTime(first.startMinute)} block slipped by"
    } else {
        "${overdue.size} blocks slipped by"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = "Reschedule ${first.title}",
            ) { onOpenTask(first) },
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, warning.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .background(warning.copy(alpha = 0.07f))
                .padding(
                    horizontal = if (compact) 10.dp else 12.dp,
                    vertical = if (compact) 7.dp else 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 26.dp else 32.dp)
                    .clip(CircleShape)
                    .background(warning.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                SparkIcon(Modifier.size(if (compact) 14.dp else 17.dp), warning)
            }
            Spacer(Modifier.width(if (compact) 8.dp else 10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = if (compact) {
                        MaterialTheme.typography.labelMedium
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!compact) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Reschedule",
                style = if (compact) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.labelMedium
                },
                fontWeight = FontWeight.SemiBold,
                color = warning,
            )
        }
    }
}

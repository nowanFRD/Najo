package com.najo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.najo.data.TaskDraft
import com.najo.model.PlanTask
import com.najo.model.Priority
import com.najo.ui.theme.NajoPalette
import com.najo.util.formatDuration
import com.najo.util.formatTime
import com.najo.util.parseCustomDuration
import java.time.LocalTime

private val DURATIONS = listOf(15, 30, 45, 60, 90, 120)

/**
 * A modal task editor rendered over the window (rather than in a separate
 * dialog window) so it looks and behaves identically on every desktop.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    initial: PlanTask?,
    onDismiss: () -> Unit,
    onSave: (TaskDraft) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val startSeed = remember(initial) { initial?.startMinute ?: defaultStartMinute() }
    var title by remember(initial) { mutableStateOf(initial?.title ?: "") }
    var hour by remember(initial) { mutableStateOf(startSeed / 60) }
    var minute by remember(initial) { mutableStateOf((startSeed % 60) / 5 * 5) }
    var duration by remember(initial) { mutableStateOf(initial?.durationMinutes ?: 30) }
    val customSeed = remember(initial) {
        initial?.durationMinutes?.takeIf { it !in DURATIONS }
    }
    var customMode by remember(initial) { mutableStateOf(customSeed != null) }
    var customText by remember(initial) { mutableStateOf(customSeed?.toString().orEmpty()) }
    var priority by remember(initial) { mutableStateOf(initial?.priority ?: Priority.NORMAL) }
    var note by remember(initial) { mutableStateOf(initial?.note ?: "") }
    var showTitleError by remember { mutableStateOf(false) }
    var showDurationError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val previewDuration = if (customMode) parseCustomDuration(customText) else duration

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun submit() {
        val cleaned = title.trim()
        if (cleaned.isEmpty()) {
            showTitleError = true
            return
        }
        val finalDuration = if (customMode) parseCustomDuration(customText) else duration
        if (finalDuration == null) {
            showDurationError = true
            return
        }
        onSave(
            TaskDraft(
                title = cleaned,
                startMinute = hour * 60 + minute,
                durationMinutes = finalDuration,
                priority = priority,
                note = note.trim(),
            ),
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220).copy(alpha = 0.55f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.Escape -> {
                        onDismiss()
                        true
                    }

                    event.key == Key.Enter && !event.isShiftPressed -> {
                        submit()
                        true
                    }

                    else -> false
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val maxPanelHeight = (maxHeight - 32.dp).coerceAtLeast(300.dp)
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .heightIn(max = maxPanelHeight)
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, NajoPalette.extras.cardOutline),
            shadowElevation = 24.dp,
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(start = 26.dp, end = 26.dp, top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (initial == null) "New task" else "Edit task",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Give this block a purpose and a place in the day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    SoftIconButton(onClick = onDismiss, size = 34.dp) {
                        CloseIcon(Modifier.size(14.dp), colors.onSurfaceVariant)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 26.dp)
                        .padding(top = 20.dp, bottom = 4.dp),
                ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) showTitleError = false
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    isError = showTitleError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(14.dp),
                    label = { Text("Task") },
                    placeholder = { Text("What's the plan?") },
                    supportingText = if (showTitleError) {
                        { Text("A task needs a name.") }
                    } else {
                        null
                    },
                )

                Spacer(Modifier.height(18.dp))
                FieldLabel("Starts")
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NumberPicker(
                        value = hour,
                        values = (0..23).toList(),
                        suffix = "h",
                        onChange = { hour = it },
                    )
                    Text(":", style = MaterialTheme.typography.titleLarge, color = colors.onSurfaceVariant)
                    NumberPicker(
                        value = minute,
                        values = (0..55 step 5).toList(),
                        suffix = "m",
                        onChange = { minute = it },
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        previewDuration?.let {
                            "Ends at ${formatTime(hour * 60 + minute + it)}"
                        } ?: "Enter a duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(18.dp))
                FieldLabel("Duration")
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DURATIONS.forEach { value ->
                        SelectChip(
                            label = formatDuration(value),
                            selected = !customMode && duration == value,
                            onClick = {
                                duration = value
                                customMode = false
                                showDurationError = false
                            },
                        )
                    }
                    SelectChip(
                        label = "Custom",
                        selected = customMode,
                        onClick = {
                            if (!customMode) customText = duration.toString()
                            customMode = true
                            showDurationError = false
                        },
                    )
                }
                if (customMode) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customText,
                        onValueChange = {
                            customText = it.filter(Char::isDigit).take(4)
                            showDurationError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showDurationError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        shape = RoundedCornerShape(14.dp),
                        label = { Text("Custom duration") },
                        suffix = { Text("min") },
                        supportingText = {
                            Text(
                                if (showDurationError) {
                                    "Enter a length from 1 minute to 24 hours."
                                } else {
                                    "Anything from 1 minute to 24 hours."
                                },
                            )
                        },
                    )
                }

                Spacer(Modifier.height(18.dp))
                FieldLabel("Priority")
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Priority.entries.forEach { value ->
                        SelectChip(
                            label = value.label,
                            selected = priority == value,
                            dot = priorityColor(value),
                            onClick = { priority = value },
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    label = { Text("Note") },
                    placeholder = { Text("Optional — how, where, why") },
                )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (initial != null && onDelete != null) {
                        TextButton(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                        ) {
                            Text(
                                "Delete",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.error,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = { submit() },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                    ) {
                        CheckIcon(Modifier.size(14.dp), colors.onPrimary, strokeWidth = 2.4.dp)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            if (initial == null) "Add to day" else "Save changes",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    values: List<Int>,
    suffix: String,
    onChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box {
        Row(
            modifier = Modifier
                .height(46.dp)
                .background(colors.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "%02d".format(value),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                suffix,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            ChevronDownIcon(Modifier.size(14.dp), colors.onSurfaceVariant)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 240.dp),
        ) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "%02d".format(option),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (option == value) colors.primary else colors.onSurface,
                        )
                    },
                    onClick = {
                        onChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    dot: Color? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(11.dp)
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) colors.primary else colors.surface)
            .border(1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(if (selected) Color.White else dot))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        )
    }
}

private fun defaultStartMinute(): Int {
    val now = LocalTime.now()
    val rounded = ((now.hour * 60 + now.minute + 14) / 15) * 15
    return if (rounded > 22 * 60) 9 * 60 else rounded
}

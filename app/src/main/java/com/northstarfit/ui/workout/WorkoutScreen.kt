package com.northstarfit.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.northstarfit.ui.theme.Spacing
import java.util.Calendar

/**
 * The live workout logger: add movements from the searchable exercise
 * sheet, log sets with weight and reps, check sets off to start the rest
 * timer, reorder movements in edit mode, then finish (with a name and a
 * confirmation) to save. The in-progress workout is drafted to the
 * database on every edit, so leaving and coming back resumes the session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onFinished: () -> Unit,
    viewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModel.Factory),
) {
    val movements by viewModel.movements.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val restTimer by viewModel.restTimer.collectAsState()
    val restDuration by viewModel.restDurationSeconds.collectAsState()
    val restTimerEnabled by viewModel.restTimerEnabled.collectAsState()

    var editMode by rememberSaveable { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { from, to ->
        viewModel.moveMovement(from, to)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editMode) "Reorder movements" else "Log Workout") },
                navigationIcon = {
                    IconButton(onClick = onFinished) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (movements.size > 1) {
                        IconButton(onClick = { editMode = !editMode }) {
                            Icon(
                                if (editMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (editMode) "Done reordering" else "Reorder",
                            )
                        }
                    }
                    if (movements.isNotEmpty() && !editMode) {
                        IconButton(onClick = { showDiscardDialog = true }) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = "Discard workout",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            WorkoutBottomBar(
                totals = totals,
                restTimer = restTimer,
                restDuration = restDuration,
                restTimerEnabled = restTimerEnabled,
                finishEnabled = !saving && !editMode,
                onSetRestDuration = viewModel::setRestDuration,
                onSetRestTimerEnabled = viewModel::setRestTimerEnabled,
                onStartRest = viewModel::startRestTimer,
                onSkipRest = viewModel::skipRestTimer,
                onFinish = { showFinishDialog = true },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
        ) {
            if (!editMode) {
                FilledTonalButton(
                    onClick = { showPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.md),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add movement")
                }
            }

            if (movements.isEmpty()) {
                Text(
                    "Tap Add movement to start logging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xl),
                )
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(movements, key = { _, m -> m.id }) { index, movement ->
                        if (editMode) {
                            ReorderableMovementRow(
                                movement = movement,
                                index = index,
                                dragDropState = dragDropState,
                            )
                        } else {
                            MovementCard(
                                movement = movement,
                                onAddSet = { viewModel.addSet(movement.id) },
                                onRemoveMovement = { viewModel.removeMovement(movement.id) },
                                onRemoveSet = { setId ->
                                    viewModel.removeSet(movement.id, setId)
                                },
                                onToggleDone = { setId ->
                                    viewModel.toggleSetDone(movement.id, setId)
                                },
                                onWeightChange = { setId, value ->
                                    viewModel.updateSet(movement.id, setId, weight = value)
                                },
                                onRepsChange = { setId, value ->
                                    viewModel.updateSet(movement.id, setId, reps = value)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        ExercisePickerSheet(
            exercises = exercises,
            onPick = { exercise ->
                viewModel.addMovement(exercise)
                showPicker = false
            },
            onCreate = { name, muscles ->
                viewModel.addExercise(name, muscles)
                showPicker = false
                viewModel.addMovement(
                    com.northstarfit.data.ExerciseEntity(name = name.trim(), muscles = muscles)
                )
            },
            onDismiss = { showPicker = false },
        )
    }

    if (showFinishDialog) {
        FinishWorkoutDialog(
            totals = totals,
            hasSavableSets = viewModel.hasSavableSets,
            onConfirm = { name ->
                showFinishDialog = false
                viewModel.finishWorkout(name, onSaved = onFinished)
            },
            onDismiss = { showFinishDialog = false },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard workout?") },
            text = { Text("Everything logged in this session will be thrown away.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardWorkout(onDiscarded = onFinished)
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep logging") }
            },
        )
    }
}

/** Collapsed movement row with a drag handle, shown in reorder mode. */
@Composable
private fun ReorderableMovementRow(
    movement: MovementDraft,
    index: Int,
    dragDropState: DragDropState,
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .dragDropItem(dragDropState, index)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(movement.exercise, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${movement.sets.size} sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                modifier = Modifier.dragDropHandle(dragDropState, index),
            )
        }
    }
}

/** Confirmation shown when finishing: name the workout, see the summary. */
@Composable
private fun FinishWorkoutDialog(
    totals: WorkoutTotals,
    hasSavableSets: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(suggestedWorkoutName()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish workout?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (hasSavableSets) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Workout name") },
                        singleLine = true,
                    )
                    Text(
                        "Sets: ${totals.sets}   Reps: ${totals.reps}   " +
                            "Volume: ${"%.1f".format(totals.volume)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        "No completed sets were logged, so nothing will be saved.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(if (hasSavableSets) "Save workout" else "Finish anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep logging") }
        },
    )
}

private fun suggestedWorkoutName(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "Morning Workout"
        in 12..16 -> "Afternoon Workout"
        else -> "Evening Workout"
    }
}

@Composable
private fun MovementCard(
    movement: MovementDraft,
    onAddSet: () -> Unit,
    onRemoveMovement: () -> Unit,
    onRemoveSet: (Long) -> Unit,
    onToggleDone: (Long) -> Unit,
    onWeightChange: (Long, String) -> Unit,
    onRepsChange: (Long, String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(movement.exercise, style = MaterialTheme.typography.titleMedium)
                    if (movement.muscles.isNotBlank()) {
                        Text(
                            movement.muscles,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onRemoveMovement) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove movement")
                }
            }

            movement.sets.forEachIndexed { index, set ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = Spacing.xs),
                    )
                    OutlinedTextField(
                        value = set.weight,
                        onValueChange = { onWeightChange(set.id, it) },
                        label = { Text("Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = set.reps,
                        onValueChange = { onRepsChange(set.id, it) },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Checkbox(
                        checked = set.done,
                        onCheckedChange = { onToggleDone(set.id) },
                    )
                    IconButton(
                        onClick = { onRemoveSet(set.id) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove set")
                    }
                }
            }

            TextButton(onClick = onAddSet) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add set")
            }
        }
    }
}

/** Totals, rest timer, and the finish action, pinned to the bottom. */
@Composable
private fun WorkoutBottomBar(
    totals: WorkoutTotals,
    restTimer: RestTimer?,
    restDuration: Int,
    restTimerEnabled: Boolean,
    finishEnabled: Boolean,
    onSetRestDuration: (Int) -> Unit,
    onSetRestTimerEnabled: (Boolean) -> Unit,
    onStartRest: () -> Unit,
    onSkipRest: () -> Unit,
    onFinish: () -> Unit,
) {
    var durationMenuOpen by remember { mutableStateOf(false) }

    Surface(tonalElevation = 3.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (restTimer != null) {
                LinearProgressIndicator(
                    progress = {
                        restTimer.remainingSeconds / restTimer.totalSeconds.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sets: ${totals.sets}   Reps: ${totals.reps}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Volume: ${"%.1f".format(totals.volume)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Column {
                    AssistChip(
                        onClick = {
                            if (restTimer != null) onSkipRest() else durationMenuOpen = true
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        label = {
                            Text(
                                when {
                                    restTimer != null ->
                                        "${formatSeconds(restTimer.remainingSeconds)} · skip"
                                    !restTimerEnabled -> "Rest off"
                                    else -> "Rest ${formatSeconds(restDuration)}"
                                }
                            )
                        },
                    )
                    DropdownMenu(
                        expanded = durationMenuOpen,
                        onDismissRequest = { durationMenuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Auto rest timer") },
                            trailingIcon = {
                                Switch(
                                    checked = restTimerEnabled,
                                    onCheckedChange = onSetRestTimerEnabled,
                                )
                            },
                            onClick = { onSetRestTimerEnabled(!restTimerEnabled) },
                        )
                        HorizontalDivider()
                        listOf(30, 60, 90, 120, 180).forEach { seconds ->
                            DropdownMenuItem(
                                text = { Text(formatSeconds(seconds)) },
                                onClick = {
                                    onSetRestDuration(seconds)
                                    durationMenuOpen = false
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Start timer") },
                            leadingIcon = {
                                Icon(Icons.Default.Timer, contentDescription = null)
                            },
                            onClick = {
                                durationMenuOpen = false
                                onStartRest()
                            },
                        )
                    }
                }

                Button(onClick = onFinish, enabled = finishEnabled) {
                    Text("Finish")
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

package com.northstarfit.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * The live workout logger: pick an exercise (or create a new one), add
 * movements, log sets with weight and reps, watch the totals update, then
 * finish to save. The in-progress workout is drafted to the database on
 * every edit, so leaving and coming back resumes where you left off.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onFinished: () -> Unit,
    viewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModel.Factory),
) {
    val movements by viewModel.movements.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showNewExerciseDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Fall back to the first exercise until the user picks one.
    val currentExercise = selectedExercise ?: exercises.firstOrNull()?.name.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Workout") },
                navigationIcon = {
                    IconButton(onClick = onFinished) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (movements.isNotEmpty()) {
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
                totals = viewModel.totals,
                finishEnabled = !saving,
                onFinish = { viewModel.finishWorkout(onSaved = onFinished) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = currentExercise,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exercise") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        exercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise.name) },
                                onClick = {
                                    selectedExercise = exercise.name
                                    dropdownExpanded = false
                                },
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("New exercise…") },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            onClick = {
                                dropdownExpanded = false
                                showNewExerciseDialog = true
                            },
                        )
                    }
                }
                Button(
                    onClick = { viewModel.addMovement(currentExercise) },
                    enabled = currentExercise.isNotBlank(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add")
                }
            }

            if (movements.isEmpty()) {
                Text(
                    "Pick an exercise and tap Add to start logging.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(movements, key = { it.id }) { movement ->
                        MovementCard(
                            movement = movement,
                            onAddSet = { viewModel.addSet(movement.id) },
                            onRemoveMovement = { viewModel.removeMovement(movement.id) },
                            onRemoveSet = { setId -> viewModel.removeSet(movement.id, setId) },
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

    if (showNewExerciseDialog) {
        NewExerciseDialog(
            onConfirm = { name ->
                viewModel.addExercise(name)
                selectedExercise = name.trim()
                showNewExerciseDialog = false
            },
            onDismiss = { showNewExerciseDialog = false },
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

@Composable
private fun NewExerciseDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New exercise") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun MovementCard(
    movement: MovementDraft,
    onAddSet: () -> Unit,
    onRemoveMovement: () -> Unit,
    onRemoveSet: (Long) -> Unit,
    onWeightChange: (Long, String) -> Unit,
    onRepsChange: (Long, String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    movement.exercise,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemoveMovement) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove movement")
                }
            }

            movement.sets.forEachIndexed { index, set ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = 4.dp),
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
                    IconButton(onClick = { onRemoveSet(set.id) }) {
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

@Composable
private fun WorkoutBottomBar(
    totals: WorkoutTotals,
    finishEnabled: Boolean,
    onFinish: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Button(onClick = onFinish, enabled = finishEnabled) {
                Text("Finish workout")
            }
        }
    }
}

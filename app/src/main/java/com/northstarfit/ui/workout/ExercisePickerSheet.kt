package com.northstarfit.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.northstarfit.data.ExerciseEntity
import com.northstarfit.ui.theme.Spacing

/**
 * Modal sheet for choosing the next movement: type-to-search, filter by
 * muscle group, each exercise shows the muscles it activates, and a "New"
 * action for exercises not in the library yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    exercises: List<ExerciseEntity>,
    onPick: (ExerciseEntity) -> Unit,
    onCreate: (name: String, muscles: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<String?>(null) }
    var showNewDialog by remember { mutableStateOf(false) }

    val allMuscles = remember(exercises) {
        exercises.flatMap { it.muscleList }.distinct().sorted()
    }
    val filtered = exercises.filter { exercise ->
        exercise.name.contains(query.trim(), ignoreCase = true) &&
            (selectedMuscle == null || exercise.muscleList.any { it.equals(selectedMuscle, true) })
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = Spacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Choose exercise",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showNewDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("New")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search exercises") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.padding(bottom = Spacing.sm),
            ) {
                items(allMuscles) { muscle ->
                    FilterChip(
                        selected = muscle == selectedMuscle,
                        onClick = {
                            selectedMuscle = if (muscle == selectedMuscle) null else muscle
                        },
                        label = { Text(muscle) },
                    )
                }
            }

            if (filtered.isEmpty()) {
                Text(
                    "No exercises match — create one with New.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.xl),
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.id }) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = {
                                if (exercise.muscles.isNotBlank()) {
                                    Text(
                                        exercise.muscles,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(exercise) },
                        )
                    }
                }
            }
        }
    }

    if (showNewDialog) {
        NewExerciseDialog(
            onConfirm = { name, muscles ->
                showNewDialog = false
                onCreate(name, muscles)
            },
            onDismiss = { showNewDialog = false },
        )
    }
}

/** Dialog for adding an exercise to the library, with optional muscles. */
@Composable
fun NewExerciseDialog(
    onConfirm: (name: String, muscles: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var muscles by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = muscles,
                    onValueChange = { muscles = it },
                    label = { Text("Muscles (comma-separated)") },
                    placeholder = { Text("e.g. Chest, Triceps") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, muscles) },
                enabled = name.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

package com.northstarfit.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.northstarfit.data.WorkoutWithMovements
import com.northstarfit.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d yyyy · h:mm a")

/** List of every saved workout: searchable, sortable, tappable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory),
) {
    val workouts by viewModel.workouts.collectAsState()
    val query by viewModel.query.collectAsState()
    val sort by viewModel.sort.collectAsState()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenuOpen,
                            onDismissRequest = { sortMenuOpen = false },
                        ) {
                            HistorySort.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    leadingIcon = {
                                        RadioButton(
                                            selected = option == sort,
                                            onClick = null,
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSort(option)
                                        sortMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search by name or exercise") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
            )

            if (workouts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (query.isBlank()) "No workouts yet — go lift something!"
                        else "No workouts match your search.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    items(workouts, key = { it.workout.id }) { workout ->
                        WorkoutHistoryCard(
                            workout = workout,
                            onClick = { onOpenWorkout(workout.workout.id) },
                            onDelete = { viewModel.deleteWorkout(workout) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutWithMovements,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        workout.workout.name.ifBlank { "Workout" },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        dateFormatter.format(
                            Instant.ofEpochMilli(workout.workout.startedAt)
                                .atZone(ZoneId.systemDefault())
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete workout")
                }
            }
            Text(
                workout.movements.joinToString { "${it.movement.exercise} ×${it.sets.size}" },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Sets: ${workout.totalSets}   Reps: ${workout.totalReps}   " +
                    "Volume: ${"%.1f".format(workout.totalVolume)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

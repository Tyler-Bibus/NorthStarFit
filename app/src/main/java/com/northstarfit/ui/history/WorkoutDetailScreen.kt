package com.northstarfit.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.northstarfit.data.MovementWithSets
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, MMM d yyyy · h:mm a")

/** Full breakdown of one saved workout: every movement, every set. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: WorkoutDetailViewModel =
        viewModel(factory = WorkoutDetailViewModel.factory(workoutId)),
) {
    val workout by viewModel.workout.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.workout?.name?.ifBlank { null } ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val loaded = workout ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        dateFormatter.format(
                            Instant.ofEpochMilli(loaded.workout.startedAt)
                                .atZone(ZoneId.systemDefault())
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val minutes = Duration
                        .ofMillis(loaded.workout.endedAt - loaded.workout.startedAt)
                        .toMinutes()
                    Text(
                        "Duration: $minutes min   Sets: ${loaded.totalSets}   " +
                            "Reps: ${loaded.totalReps}   Volume: ${"%.1f".format(loaded.totalVolume)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(loaded.movements, key = { it.movement.id }) { movement ->
                MovementDetailCard(movement)
            }
        }
    }
}

@Composable
private fun MovementDetailCard(movement: MovementWithSets) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                movement.movement.exercise,
                style = MaterialTheme.typography.titleMedium,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            movement.sets.forEachIndexed { index, set ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                ) {
                    Text(
                        "Set ${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${set.weight} × ${set.reps}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

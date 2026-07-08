package com.northstarfit.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.northstarfit.ui.theme.Spacing

/** Landing screen: start a workout, browse history, or check progress. */
@Composable
fun HomeScreen(
    onStartWorkout: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "NorthStarFit",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Track. Lift. Repeat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.xxl))
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null)
            Text("  Start Workout")
        }
        Spacer(Modifier.height(Spacing.lg))
        OutlinedButton(
            onClick = onOpenHistory,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Text("  Workout History")
        }
        Spacer(Modifier.height(Spacing.lg))
        OutlinedButton(
            onClick = onOpenProgress,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null)
            Text("  Progress")
        }
        Spacer(Modifier.height(Spacing.lg))
        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Food Log (coming soon)")
        }
    }
}

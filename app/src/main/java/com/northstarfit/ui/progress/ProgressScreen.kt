package com.northstarfit.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.northstarfit.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val axisDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val rowDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/**
 * Per-exercise progress over time: pick an exercise, choose max weight or
 * volume, read the trend. Tap the chart to inspect a session; the list
 * below mirrors the chart as plain numbers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory),
) {
    val exerciseNames by viewModel.exerciseNames.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val metric by viewModel.metric.collectAsState()
    val points by viewModel.points.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    val currentExercise = selectedExercise ?: exerciseNames.firstOrNull().orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (exerciseNames.isEmpty()) {
                Text(
                    "Finish a few workouts and your progress will show up here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xl),
                )
                return@Column
            }

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
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
                    exerciseNames.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                viewModel.selectExercise(name)
                                dropdownExpanded = false
                            },
                        )
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ProgressMetric.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == metric,
                        onClick = { viewModel.setMetric(option) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ProgressMetric.entries.size,
                        ),
                    ) { Text(option.label) }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        "${metric.label} · $currentExercise",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (points.size < 2) {
                        Text(
                            "Need at least two sessions with this exercise to draw a trend.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Spacing.xl),
                        )
                    } else {
                        ProgressLineChart(
                            points = points,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .padding(top = Spacing.sm),
                        )
                    }
                }
            }

            // Plain-number mirror of the chart, newest first.
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(points.asReversed()) { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.sm),
                    ) {
                        Text(
                            rowDateFormatter.format(
                                Instant.ofEpochMilli(point.timestamp)
                                    .atZone(ZoneId.systemDefault())
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatValue(point.value),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Single-series line chart: 2dp line in the theme primary color, muted
 * horizontal gridlines, first/last date on the x axis, direct label on the
 * latest point only, tap to inspect any session.
 */
@Composable
private fun ProgressLineChart(
    points: List<ProgressPoint>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = labelColor)
    val tooltipStyle = MaterialTheme.typography.labelMedium
        .copy(color = MaterialTheme.colorScheme.onSurface)
    val textMeasurer = rememberTextMeasurer()

    var selectedIndex by remember(points) { mutableIntStateOf(-1) }

    Canvas(
        modifier = modifier.pointerInput(points) {
            detectTapGestures { tap ->
                // Nearest point horizontally; tap again to clear.
                val plotLeft = 0f
                val t0 = points.first().timestamp
                val t1 = points.last().timestamp
                val span = (t1 - t0).coerceAtLeast(1)
                val nearest = points.indices.minByOrNull { i ->
                    val x = plotLeft +
                        (points[i].timestamp - t0).toFloat() / span * size.width
                    kotlin.math.abs(x - tap.x)
                } ?: -1
                selectedIndex = if (nearest == selectedIndex) -1 else nearest
            }
        },
    ) {
        val ticks = niceTicks(points.maxOf { it.value })
        val maxTick = ticks.last()

        val yLabelWidth = ticks.maxOf {
            textMeasurer.measure(formatValue(it), labelStyle).size.width
        }.toFloat()
        val xLabelHeight = textMeasurer
            .measure("Sep", labelStyle).size.height.toFloat()

        val plotLeft = yLabelWidth + 12.dp.toPx()
        val plotRight = size.width - 8.dp.toPx()
        val plotTop = 12.dp.toPx()
        val plotBottom = size.height - xLabelHeight - 8.dp.toPx()
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        fun yFor(value: Double): Float =
            plotBottom - (value / maxTick).toFloat() * plotHeight

        val t0 = points.first().timestamp
        val t1 = points.last().timestamp
        val span = (t1 - t0).coerceAtLeast(1)
        fun xFor(timestamp: Long): Float =
            plotLeft + (timestamp - t0).toFloat() / span * plotWidth

        // Gridlines + y labels (recessive, ink tokens).
        ticks.forEach { tick ->
            val y = yFor(tick)
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx(),
            )
            val measured = textMeasurer.measure(formatValue(tick), labelStyle)
            drawText(
                measured,
                topLeft = Offset(
                    yLabelWidth - measured.size.width,
                    y - measured.size.height / 2f,
                ),
            )
        }

        // X labels: first and last session dates.
        listOf(points.first(), points.last()).forEachIndexed { i, point ->
            val text = axisDateFormatter.format(
                Instant.ofEpochMilli(point.timestamp).atZone(ZoneId.systemDefault())
            )
            val measured = textMeasurer.measure(text, labelStyle)
            val x = if (i == 0) plotLeft
            else plotRight - measured.size.width
            drawText(measured, topLeft = Offset(x, plotBottom + 4.dp.toPx()))
        }

        // The series: 2dp line, 4dp dots.
        val path = Path()
        points.forEachIndexed { i, point ->
            val x = xFor(point.timestamp)
            val y = yFor(point.value)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
        points.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(xFor(point.timestamp), yFor(point.value)),
            )
        }

        // Direct label on the latest point only.
        val last = points.last()
        val lastLabel = textMeasurer.measure(formatValue(last.value), tooltipStyle)
        drawText(
            lastLabel,
            topLeft = Offset(
                (xFor(last.timestamp) - lastLabel.size.width)
                    .coerceAtLeast(plotLeft),
                (yFor(last.value) - lastLabel.size.height - 6.dp.toPx())
                    .coerceAtLeast(0f),
            ),
        )

        // Tap inspection: crosshair + date/value for the selected session.
        if (selectedIndex in points.indices) {
            val sel = points[selectedIndex]
            val x = xFor(sel.timestamp)
            drawLine(
                color = labelColor,
                start = Offset(x, plotTop),
                end = Offset(x, plotBottom),
                strokeWidth = 1.dp.toPx(),
            )
            drawCircle(
                color = lineColor,
                radius = 6.dp.toPx(),
                center = Offset(x, yFor(sel.value)),
            )
            val text = axisDateFormatter.format(
                Instant.ofEpochMilli(sel.timestamp).atZone(ZoneId.systemDefault())
            ) + "  ·  " + formatValue(sel.value)
            val measured = textMeasurer.measure(text, tooltipStyle)
            drawText(
                measured,
                topLeft = Offset(
                    (x - measured.size.width / 2f)
                        .coerceIn(plotLeft, plotRight - measured.size.width),
                    plotTop,
                ),
            )
        }
    }
}

/** Round-number axis ticks from zero to just past the max value. */
private fun niceTicks(maxValue: Double, tickCount: Int = 4): List<Double> {
    if (maxValue <= 0) return listOf(0.0, 1.0)
    val rawStep = maxValue / tickCount
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude
    val step = when {
        normalized <= 1 -> 1.0
        normalized <= 2 -> 2.0
        normalized <= 5 -> 5.0
        else -> 10.0
    } * magnitude
    val top = ceil(maxValue / step) * step
    return generateSequence(0.0) { it + step }
        .takeWhile { it <= top + step / 2 }
        .toList()
}

private fun formatValue(value: Double): String =
    if (value % 1.0 == 0.0) "%.0f".format(value) else "%.1f".format(value)

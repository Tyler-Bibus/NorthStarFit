package com.northstarfit.ui.workout

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

/**
 * Minimal drag-to-reorder support for a LazyColumn whose items are all
 * draggable. Attach [DragDropState.handleModifier] to the drag handle of
 * item [index] and [itemModifier] to the item container.
 */
class DragDropState(
    private val listState: LazyListState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    private var draggedDistance by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == draggingItemIndex }

    /** How far the dragged item should be translated from its laid-out spot. */
    val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggedDistance - item.offset
        } ?: 0f

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        draggingItemInitialOffset = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }?.offset ?: 0
        draggedDistance = 0f
    }

    fun onDrag(deltaY: Float) {
        draggedDistance += deltaY
        val dragging = draggingItemLayoutInfo ?: return
        val startOffset = draggingItemInitialOffset + draggedDistance
        val middle = startOffset + dragging.size / 2f

        // Swap with whichever item the dragged item's middle is now over.
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            item.index != dragging.index &&
                middle.toInt() in item.offset..(item.offset + item.size)
        }
        if (target != null) {
            onMove(dragging.index, target.index)
            draggingItemIndex = target.index
        }
    }

    fun onDragEnd() {
        draggingItemIndex = null
        draggedDistance = 0f
    }
}

@Composable
fun rememberDragDropState(
    listState: LazyListState,
    onMove: (Int, Int) -> Unit,
): DragDropState = remember(listState) { DragDropState(listState, onMove) }

/** Lifts and moves the item while it is being dragged. */
fun Modifier.dragDropItem(state: DragDropState, index: Int): Modifier =
    if (index == state.draggingItemIndex) {
        this
            .zIndex(1f)
            .graphicsLayer { translationY = state.draggingItemOffset }
    } else {
        this
    }

/** Attach to the drag-handle icon of item [index]. */
fun Modifier.dragDropHandle(state: DragDropState, index: Int): Modifier =
    pointerInput(state, index) {
        detectDragGestures(
            onDragStart = { state.onDragStart(index) },
            onDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount.y)
            },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragEnd() },
        )
    }

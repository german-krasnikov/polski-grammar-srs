package polski.ui

import org.w3c.dom.events.Event

internal expect fun pointerIdentifier(event: Event): Int
internal expect fun isPrimaryPointer(event: Event): Boolean

/**
 * P1-6: drops any text range the browser's own native drag-select already started, right as a
 * horizontal swipe gesture locks in — one `expect`/`actual` per target rather than a shared
 * `dynamic`/`js()` call, same precedent as [withViewTransition].
 */
internal expect fun clearTextSelection()

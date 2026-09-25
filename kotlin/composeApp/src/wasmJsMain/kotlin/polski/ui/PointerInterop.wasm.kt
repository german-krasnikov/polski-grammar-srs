@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package polski.ui

import org.w3c.dom.events.Event

internal actual fun isTouchPointer(event: Event): Boolean = js("event.pointerType === 'touch'")
internal actual fun pointerIdentifier(event: Event): Int = js("event.pointerId")
internal actual fun isPrimaryPointer(event: Event): Boolean = js("event.isPrimary")

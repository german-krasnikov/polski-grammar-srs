package polski.ui

import org.w3c.dom.events.Event

internal expect fun isTouchPointer(event: Event): Boolean
internal expect fun pointerIdentifier(event: Event): Int
internal expect fun isPrimaryPointer(event: Event): Boolean

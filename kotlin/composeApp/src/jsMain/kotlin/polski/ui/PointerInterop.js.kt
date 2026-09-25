package polski.ui

import org.w3c.dom.events.Event
import kotlin.js.asDynamic

internal actual fun isTouchPointer(event: Event): Boolean = event.asDynamic().pointerType == "touch"
internal actual fun pointerIdentifier(event: Event): Int = event.asDynamic().pointerId as Int
internal actual fun isPrimaryPointer(event: Event): Boolean = event.asDynamic().isPrimary as Boolean

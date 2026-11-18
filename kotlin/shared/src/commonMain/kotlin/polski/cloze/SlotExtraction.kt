package polski.cloze

import polski.core.model.CoreExercise

internal data class SlotData(
    val correctForm: String,
    val lexemeKey: String,
    val absStart: Int,
    val absEnd: Int,
)

/** Extracts the first tile-eligible slot from [exercise]. Returns null when preconditions fail. */
internal fun extractSlot(exercise: CoreExercise): SlotData? {
    val phrase = exercise.tilePhrase ?: return null
    if (exercise.tileTargetSlots.isEmpty()) return null
    val offset = exercise.expected.indexOf(phrase.text)
    if (offset < 0) return null
    val slotName = exercise.tileTargetSlots.firstOrNull { it in phrase.slotSpans } ?: return null
    val span = phrase.slotSpans[slotName] ?: return null
    val absStart = span.first + offset
    val absEnd = span.last + 1 + offset
    val correctForm = exercise.expected.substring(absStart, absEnd)
    val lexeme = exercise.slots[slotName] ?: return null
    return SlotData(correctForm, "$slotName:$lexeme", absStart, absEnd)
}

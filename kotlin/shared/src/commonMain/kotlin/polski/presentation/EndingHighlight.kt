package polski.presentation

import polski.model.FormChange

/** Display-only fragment; form text and review identity remain unchanged. */
data class EndingPart(val text: String, val isEnding: Boolean, val isChanged: Boolean = false)

/** Literal forms with lossless display fragments. Generated grammar pairs use the conservative fallback. */
data class ContrastPair(
    val from: String,
    val to: String,
    val beforeParts: List<EndingPart>,
    val afterParts: List<EndingPart>,
) {
    fun parts(side: ChangeSide): List<EndingPart> {
        val text = if (side == ChangeSide.Before) from else to
        val parts = if (side == ChangeSide.Before) beforeParts else afterParts
        return if (parts.isNotEmpty() && parts.all { it.text.isNotEmpty() } && parts.joinToString("") { it.text } == text) {
            parts
        } else listOf(EndingPart(text, false))
    }

    companion object {
        fun generated(from: String, to: String): ContrastPair = ContrastPair(
            from, to,
            changeHighlightParts(from, to, ChangeSide.Before),
            changeHighlightParts(from, to, ChangeSide.After),
        )
    }
}

enum class ChangeSide { Before, After }

/** Distinguishes aligned short suffix changes from a whole-word replacement. */
fun changeHighlightParts(from: String, to: String, side: ChangeSide): List<EndingPart> {
    val oldWords = from.split(' ')
    val newWords = to.split(' ')
    val selected = if (side == ChangeSide.Before) oldWords else newWords
    if (oldWords.size != newWords.size || oldWords.any(String::isEmpty) || newWords.any(String::isEmpty)) {
        return listOf(EndingPart(if (side == ChangeSide.Before) from else to, false, from != to))
    }
    return buildList {
        selected.forEachIndexed { index, word ->
            if (index > 0) add(EndingPart(" ", false))
            val old = oldWords[index]
            val next = newWords[index]
            if (old == next) {
                add(EndingPart(word, false))
                return@forEachIndexed
            }
            val oldCoreEnd = old.indexOfLast(Char::isLetter) + 1
            val nextCoreEnd = next.indexOfLast(Char::isLetter) + 1
            val oldCore = old.substring(0, oldCoreEnd)
            val nextCore = next.substring(0, nextCoreEnd)
            val core = if (side == ChangeSide.Before) oldCore else nextCore
            val trailing = word.substring(if (side == ChangeSide.Before) oldCoreEnd else nextCoreEnd)
            val prefix = oldCore.zip(nextCore).takeWhile { (a, b) -> a == b }.size
            val oldSuffix = oldCore.substring(prefix)
            val newSuffix = nextCore.substring(prefix)
            val reliable = prefix >= 3 && newSuffix.length in 1..3 && oldSuffix.length <= 3 &&
                newSuffix.all(Char::isLetter) && oldSuffix.all(Char::isLetter)
            if (reliable) {
                if (prefix > 0) add(EndingPart(core.substring(0, prefix), false))
                if (prefix < core.length) add(EndingPart(core.substring(prefix), true, true))
                if (trailing.isNotEmpty()) add(EndingPart(trailing, false))
            } else add(EndingPart(word, false, true))
        }
    }
}

fun endingHighlightParts(from: String, to: String): List<EndingPart> =
    changeHighlightParts(from, to, ChangeSide.After)

private fun wholePhraseStart(sentence: String, phrase: String): Int {
    if (phrase.isEmpty()) return -1
    var start = sentence.indexOf(phrase)
    while (start >= 0) {
        val end = start + phrase.length
        if ((start == 0 || !sentence[start - 1].isLetterOrDigit()) &&
            (end == sentence.length || !sentence[end].isLetterOrDigit())) return start
        start = sentence.indexOf(phrase, start + 1)
    }
    return -1
}

/** Projects explicit form changes into a sentence and preserves all punctuation and spacing. */
fun sentenceHighlightParts(sentence: String, changes: List<FormChange>, side: ChangeSide): List<EndingPart> {
    val spans = changes.mapNotNull { change ->
        val phrase = if (side == ChangeSide.Before) change.from else change.to
        wholePhraseStart(sentence, phrase).takeIf { it >= 0 }?.let { Triple(it, phrase, change) }
    }.sortedBy { it.first }
    return buildList {
        var cursor = 0
        spans.forEach { (start, phrase, change) ->
            if (start < cursor) return@forEach
            if (start > cursor) add(EndingPart(sentence.substring(cursor, start), false))
            addAll(changeHighlightParts(change.from, change.to, side))
            cursor = start + phrase.length
        }
        if (cursor < sentence.length) add(EndingPart(sentence.substring(cursor), false))
    }
}

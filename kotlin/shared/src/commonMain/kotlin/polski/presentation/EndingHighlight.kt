package polski.presentation

import polski.data.courseStemAlternations
import polski.model.FormChange

/** Display-only fragment; form text and review identity remain unchanged. [side] is the role a
 *  changed fragment plays (Emphasis contract: "before" = warm/dashed, "after" = cool/solid) — set
 *  only on [isChanged] fragments produced by [changeHighlightParts]; a host must render two
 *  changed fragments differently when [side] differs, even within one running block of prose
 *  (EmphasisUXAudit E7/S4/W3 correction: a mixed-role list, e.g. from [styleTextHighlightParts],
 *  cannot be classed as a single role by its caller alone). */
data class EndingPart(val text: String, val isEnding: Boolean, val isChanged: Boolean = false, val side: ChangeSide? = null)

/**
 * One regular stem alternation the active language pack declares (e.g. Polish `ó~o`, `ą~ę`),
 * used only to judge whether an aligned short suffix diff is reliable — never to rewrite any
 * displayed text. Unordered: [a] and [b] each may appear on either side of the change.
 */
data class StemAlternation(val a: Char, val b: Char) {
    init { require(a != b) { "StemAlternation: a and b must differ" } }
    fun matches(x: Char, y: Char): Boolean = (x == a && y == b) || (x == b && y == a)
}

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

/**
 * Distinguishes aligned short suffix changes from a whole-word replacement. [alternations]
 * (default: the active pack's own declared pairs, UC S2) lets a regular stem alternation still
 * count as a reliable aligned prefix instead of falling back to a whole-word change.
 */
fun changeHighlightParts(
    from: String,
    to: String,
    side: ChangeSide,
    alternations: List<StemAlternation> = courseStemAlternations,
): List<EndingPart> {
    val oldWords = from.split(' ')
    val newWords = to.split(' ')
    if (oldWords.any(String::isEmpty) || newWords.any(String::isEmpty)) {
        return listOf(EndingPart(if (side == ChangeSide.Before) from else to, false, from != to, side))
    }
    if (oldWords.size != newWords.size) {
        return singleWordInsertionOrDeletionParts(oldWords, newWords, side)
            ?: listOf(EndingPart(if (side == ChangeSide.Before) from else to, false, from != to, side))
    }
    val selected = if (side == ChangeSide.Before) oldWords else newWords
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
            if (oldCore == nextCore) {
                // Letters are identical; only trailing punctuation differs. Punctuation never
                // participates in the diff (Emphasis contract, rule 1), so nothing is highlighted.
                if (core.isNotEmpty()) add(EndingPart(core, false))
                if (trailing.isNotEmpty()) add(EndingPart(trailing, false))
                return@forEachIndexed
            }
            val prefix = oldCore.zip(nextCore)
                .takeWhile { (a, b) -> a == b || alternations.any { it.matches(a, b) } }.size
            val oldSuffix = oldCore.substring(prefix)
            val newSuffix = nextCore.substring(prefix)
            val reliable = prefix >= 3 && newSuffix.length in 1..3 && oldSuffix.length <= 3 &&
                newSuffix.all(Char::isLetter) && oldSuffix.all(Char::isLetter)
            if (reliable) {
                if (prefix > 0) add(EndingPart(core.substring(0, prefix), false))
                if (prefix < core.length) add(EndingPart(core.substring(prefix), true, true, side))
                if (trailing.isNotEmpty()) add(EndingPart(trailing, false))
            } else add(EndingPart(word, false, true, side))
        }
    }
}

/**
 * Detects a single word inserted into (or deleted from) an otherwise identical word-for-word
 * phrase — e.g. the "Nie"/"Czy" particle in a system-card step ("Widzę…" → "Nie widzę…"). Word
 * case is ignored when aligning (Emphasis contract, rule 1: case never participates in the
 * diff), since a word's capitalisation can shift with its position in the sentence. Returns null
 * when no such single-word alignment exists, so the caller falls back to a whole-phrase change.
 */
private fun singleWordInsertionOrDeletionParts(
    oldWords: List<String>,
    newWords: List<String>,
    side: ChangeSide,
): List<EndingPart>? {
    val insertion = newWords.size == oldWords.size + 1
    if (!insertion && oldWords.size != newWords.size + 1) return null
    val longer = if (insertion) newWords else oldWords
    val shorter = if (insertion) oldWords else newWords
    val extraIndex = longer.indices.firstOrNull { i ->
        val remainder = longer.filterIndexed { j, _ -> j != i }
        remainder.size == shorter.size && remainder.zip(shorter).all { (a, b) -> a.equals(b, ignoreCase = true) }
    } ?: return null
    val onLongerSide = if (insertion) side == ChangeSide.After else side == ChangeSide.Before
    val words = if (onLongerSide) longer else shorter
    return buildList {
        words.forEachIndexed { index, word ->
            if (index > 0) add(EndingPart(" ", false))
            val changed = onLongerSide && index == extraIndex
            add(EndingPart(word, false, changed, if (changed) side else null))
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

/**
 * Highlights a style block's own prose ([text]: formula/rule/scene/nativeParallel-target/examples
 * /why) from one explicit pack pair — a skill's `focus.before`/`focus.after` (UC-10), never the
 * current exercise's own answer, so a style block carries no more than it always has before reveal
 * (Emphasis contract §5; EmphasisUXAudit E7). A literal, whole-word occurrence of [from] gets
 * "before"-role parts, of [to] gets "after"-role parts. Free prose is never parsed heuristically:
 * when neither phrase occurs verbatim, [text] stays a single unmarked [EndingPart] (contract rule
 * 6 — no highlight beats a wrong one).
 */
fun styleTextHighlightParts(text: String, from: String, to: String): List<EndingPart> {
    val spans = listOfNotNull(
        wholePhraseStart(text, from).takeIf { it >= 0 }?.let { Triple(it, from, ChangeSide.Before) },
        wholePhraseStart(text, to).takeIf { it >= 0 && to != from }?.let { Triple(it, to, ChangeSide.After) },
    ).sortedBy { it.first }
    return buildList {
        var cursor = 0
        spans.forEach { (start, phrase, side) ->
            if (start < cursor) return@forEach
            if (start > cursor) add(EndingPart(text.substring(cursor, start), false))
            addAll(changeHighlightParts(from, to, side))
            cursor = start + phrase.length
        }
        if (cursor < text.length) add(EndingPart(text.substring(cursor), false))
    }
}

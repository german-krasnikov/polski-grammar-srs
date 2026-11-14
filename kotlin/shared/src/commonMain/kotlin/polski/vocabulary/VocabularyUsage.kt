package polski.vocabulary

import polski.data.VocabularyItem
import polski.grammar.normalize

/**
 * M8 blueprint: `noun.<x>` vocabulary ids already strip to exactly the `nounId` the
 * generator/seed pool uses (`courses-biome/pl-ru/vocabulary.core-data`'s `noun.book` <->
 * `nouns.core-data`'s `NOUN { id: book }`). `null` for every other vocabulary id shape
 * (adjective/verb/custom words) — M8 scopes "use in a phrase" to nouns only.
 */
fun VocabularyItem.lexemeId(): String? = id.takeIf { it.startsWith("noun.") }?.removePrefix("noun.")

/**
 * M8 blueprint AC1: an automatic correct/incorrect verdict for a typed vocabulary answer — a
 * single normalized-text compare against the one correct side for [direction] (the target
 * language's [VocabularyItem.lemma] when recalling the target, the native language's
 * [VocabularyItem.translation] when recalling native), the same [normalize] rule grammar typed
 * answers already use. [target]/[native] are the active pack's language codes (as
 * [StudyDirection.nativeToTarget]/[targetToNative] already take them), used only to tell which of
 * the two directions [direction] is. No accepted-variant list exists yet for vocabulary (unlike
 * grammar `Exercise.accepted`), so a word with more than one acceptable translation is a known,
 * documented limitation, not a regression — today there is no check at all.
 */
fun checkTyped(item: VocabularyItem, direction: StudyDirection, target: String, native: String, typed: String): Boolean {
    val expected = if (direction == StudyDirection.nativeToTarget(target, native)) item.lemma else item.translation
    return normalize(typed) == normalize(expected)
}

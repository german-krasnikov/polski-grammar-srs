package polski.core.engine

/** P1 blueprint §P1.3 (AC6 "training vs. probe content separated by stable content, not only by
 *  random ID"): two held-out, disjoint pools per skill, separate from the normal training pool. */
enum class ContentPool { Training, ProbeD7, ProbeD30 }

/** Pilot tuning knobs, same "tune during the pilot" status as [RETRY_DELAY]-class placeholders
 *  elsewhere in this codebase — not derived from any statistical power calculation. */
const val PROBE_D7_PERCENT = 15
const val PROBE_D30_PERCENT = 15

/** Plain deterministic FNV-1a string hash — the same algorithm as
 *  `polski.progress.RawProgressRepository.fingerprint`, duplicated here rather than shared via a
 *  new `:core-engine` → `:srs-progress` dependency for one one-line function. Deliberately NOT
 *  [RandomSource]: that is seeded per-session/platform and must stay that way for M4/M5's own
 *  contract, while a probe split must be stable across every run, every user, every host. */
private fun stableHash(value: String): Long {
    var hash = -3750763034362895579L
    for (char in value) hash = (hash xor char.code.toLong()) * 1099511628211L
    return hash
}

/** Deterministic, no [RandomSource] — the same `(skillId, seed)` always lands in the same pool,
 *  for every user and every run, so a probe item served today is still a probe item at D7/D30.
 *  Partitioned by stable seed content (`seedKey`), never by the freshly-minted
 *  [ExerciseIdFactory]-issued exercise ID — exactly what AC6 rules out (never by random ID alone). */
fun poolOf(skillId: String, seed: Map<String, String>): ContentPool {
    val bucket = (stableHash("$skillId:${seedKey(seed)}").toULong() % 100u).toInt()
    return when {
        bucket < PROBE_D7_PERCENT -> ContentPool.ProbeD7
        bucket < PROBE_D7_PERCENT + PROBE_D30_PERCENT -> ContentPool.ProbeD30
        else -> ContentPool.Training
    }
}

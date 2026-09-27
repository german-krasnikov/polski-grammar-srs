package polski.core.engine

import polski.core.model.FeatureBundle

/**
 * UniversalCorePlan.md §5.1: how a language realizes one [lexeme] under one [FeatureBundle] into
 * surface text. `:core-engine` never hardcodes a language — a `Morphology` is language data plus
 * a lookup strategy, nothing more.
 */
interface Morphology {
    fun form(lexeme: String, bundle: FeatureBundle): String
}

/**
 * UniversalCorePlan.md §1.5/§5.1: table lookup — the only [Morphology] needed now, and the only
 * one pl (a table language) will ever need. [forms] is materialized entirely at build time (a
 * converter such as `scripts/build-pack.mjs`, UniversalCorePlan.md §8) from a language's own
 * paradigm/rewrite data; this class applies zero grammatical rules at runtime, by design — a
 * language whose morphology is too productive for a table gets `MorphologyPlugin` (§4.4), not a
 * rule engine bolted onto this class.
 */
class TableMorphology(private val forms: Map<String, Map<FeatureBundle, String>>) : Morphology {
    override fun form(lexeme: String, bundle: FeatureBundle): String =
        forms[lexeme]?.get(bundle)
            ?: error("TableMorphology: no form for lexeme=\"$lexeme\" bundle=$bundle")
}

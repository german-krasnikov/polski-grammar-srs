package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** UniversalCorePlan.md §5.1: TableMorphology is a pure lookup — hit returns the stored form, miss errors. */
class TableMorphologyTest {
    private val genBundle = mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("gen"))
    private val nomBundle = mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("nom"))
    private val morphology: Morphology = TableMorphology(mapOf("noun:wife" to mapOf(genBundle to "żony", nomBundle to "żona")))

    @Test fun looksUpTheStoredFormForALexemeAndBundle() {
        assertEquals("żona", morphology.form("noun:wife", nomBundle))
        assertEquals("żony", morphology.form("noun:wife", genBundle))
    }

    @Test fun errorsOnAnUnknownLexemeOrBundleRatherThanGuessing() {
        assertFailsWith<IllegalStateException> { morphology.form("noun:unknown", nomBundle) }
        val instBundle = mapOf(FeatureKey("Number") to FeatureValue("sg"), FeatureKey("Case") to FeatureValue("inst"))
        assertFailsWith<IllegalStateException> { morphology.form("noun:wife", instBundle) }
    }
}

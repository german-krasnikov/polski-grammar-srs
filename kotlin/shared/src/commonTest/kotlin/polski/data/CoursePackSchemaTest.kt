package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import polski.grammar.caseRows
import polski.grammar.genderNames

/**
 * Plans/Kotlin/EnRuAcceptance-2026-09-28.md §7 item 1: [CoursePack] must stop hardcoding pl's
 * gender/case-shaped grammar onto every pack (`Noun.gender`, case-keyed `possessiveForms`/
 * `futureAuxiliary`/reference rows, [polski.model.PossessiveId]'s closed set) so a genuinely
 * caseless/genderless pack (en-ru) can still parse completely — [usableCourseSelections] is the
 * exact probe [selectCoursePack]/`availableCoursePacks` gate on (see `packRegistry`'s own KDoc).
 * pl-ru's own parse must stay exactly as before — [CoursePackLoaderTest] is the byte-identical
 * fixture guard for that half.
 */
class CoursePackSchemaTest {
    @Test fun usableCourseSelectionsListsBothRegisteredPacks() {
        assertEquals(setOf("pl" to "ru", "en" to "ru"), usableCourseSelections.toSet())
    }

    @Test fun enRuBecomesSelectableOnceItParsesCompletely() {
        try {
            selectCoursePack("en-ru")
            assertEquals("en-ru", activeCoursePackId)
        } finally {
            selectCoursePack("pl-ru")
        }
    }

    /**
     * EnRuAcceptance §7 item 1 correction: [selectCoursePack] now really flips the process-wide
     * [packRegistry.active] to en-ru (Settings/[polski.ios.IosPreferencesSession] and
     * [polski.macos.MacPreferencesSession] included), so a host's existing Matrix/reference screen
     * can run with en-ru active. en-ru's own `reference` block is genuinely empty, so every wrapper
     * below must degrade to an empty/blank value instead of the `!!` this fix replaced — accessing
     * them with en-ru active must not throw, which is what this test pins.
     */
    @Test fun referenceAndMatrixWrappersDoNotCrashWithEnRuActive() {
        try {
            selectCoursePack("en-ru")
            assertEquals(emptyList(), referenceChainRows)
            assertEquals(emptyList(), referenceSystemCards)
            referencePipeline
            referenceCaseTeaching
            referenceVerbTeaching
            referencePronounTeaching
            assertEquals(emptyList(), comparisonNounIds)
            referenceRussianSupport
            assertEquals(emptyList(), referenceTenseRows)
            assertEquals(emptyList(), referenceAspectRows)
            assertEquals(emptyList(), maleAccRows)
            assertEquals("", courseMatrixIntroduction)
            assertEquals("", courseWebCaseCompositionHeader)
            courseContextHelp
            assertEquals("", courseMaleAccIntro)
            courseAspectNoPresent
            assertEquals(emptyList(), caseRows)
            assertEquals(polski.model.Gender.entries.associateWith { "" }, genderNames)
        } finally {
            selectCoursePack("pl-ru")
        }
    }
}

package dev.polski.grammarmatrix

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import polski.data.comparisonNounIds
import polski.data.nounById
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.referencePronounTeaching
import polski.data.referenceVerbTeaching
import polski.data.verbs
import polski.grammar.caseRows
import polski.grammar.caseSentence
import polski.grammar.nounPhrase
import polski.grammar.verbForm
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.presentation.AppUiState
import polski.ui.screens.AndroidCasesSection
import polski.ui.screens.AndroidPronounsSection
import polski.ui.screens.AndroidVerbsSection

/**
 * UC-09 part 2/2 (UniversalCorePlan.md §5.3.3): `AndroidCasesSection`/`AndroidVerbsSection`/
 * `AndroidPronounsSection` now source every cell from `MatrixTableEngine`+`MatrixTableViewModel`
 * instead of calling `nounPhrase`/`caseSentence`/`verbForm` ad hoc inline per card. This pins the
 * exact values the screen must keep showing — UC-09's own acceptance criterion in
 * `UniversalCorePlan.md` ("screenshot parity — the same values in the same cells") — so switching
 * the data path to the generic engine cannot silently change what a single cell says.
 *
 * Polish's own syncretism (e.g. feminine Dat==Loc) legitimately makes the same "Было → Стало" pair
 * or the same column label appear more than once (AndroidCaseReferenceLeakTest documents the same
 * fact) — [existsDescribedAs]/[existsWithText] assert presence, not uniqueness.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidMatrixTableParityTest {
    @get:Rule val composeRule = createComposeRule()

    private fun ComposeContentTestRule.existsDescribedAs(description: String) =
        assertTrue("missing contentDescription: $description",
            onAllNodesWithContentDescription(description, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())

    private fun ComposeContentTestRule.existsWithText(text: String) =
        assertTrue("missing text: $text", onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())

    @Test fun casesSectionShowsEveryCaseRowsOwnPhraseAndSentence() {
        val state = AppUiState()
        composeRule.setContent { MaterialTheme { AndroidCasesSection(state, dispatch = {}) } }
        val selected = state.matrixSelection
        val number = NumberGram.fromId(selected.numberId)
        val owner = PossessiveId.fromId(selected.ownerId)
        val seed = SentenceSeed(selected.nounId, selected.adjectiveId)
        val basePhrase = nounPhrase(selected.nounId, GramCase.NOM, number, selected.adjectiveId, owner)
        val baseSentence = caseSentence(seed, GramCase.NOM, owner, number)
        caseRows.forEach { row ->
            val phrase = nounPhrase(selected.nounId, row.id, number, selected.adjectiveId, owner)
            val sentence = caseSentence(seed, row.id, owner, number)
            composeRule.existsWithText("${row.question} · ${row.trigger}")
            composeRule.existsDescribedAs("Было: $basePhrase. Стало: $phrase")
            composeRule.existsDescribedAs("Было: $baseSentence. Стало: $sentence")
        }
        // Default comparison case is caseRows.first().id — every noun's own base/compared pair.
        comparisonNounIds.forEach { id ->
            val noun = nounById(id)
            val base = noun.forms.getValue(number).getValue(GramCase.NOM)
            val compared = noun.forms.getValue(number).getValue(caseRows.first().id)
            composeRule.existsDescribedAs("Было: $base. Стало: $compared")
        }
    }

    @Test fun verbsSectionShowsEverySubjectAcrossAllThreeTenses() {
        val state = AppUiState()
        composeRule.setContent { MaterialTheme { AndroidVerbsSection(state, dispatch = {}) } }
        val selected = state.matrixSelection
        val lemma = verbs.first { it.id == selected.verbId }.lemma
        referenceVerbTeaching.subjects.forEach { subject ->
            listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).forEach { tense ->
                composeRule.existsWithText(referenceVerbTeaching.tenseLabels.getValue(tense).compact)
                val form = verbForm(selected.verbId, tense, subject.person, subject.number, subject.gender(selected.feminineGroup))
                composeRule.existsDescribedAs("Было: $lemma. Стало: $form")
            }
        }
    }

    @Test fun pronounsSectionShowsEveryPronounContextAndPossessiveCase() {
        val state = AppUiState()
        composeRule.setContent { MaterialTheme { AndroidPronounsSection(dispatch = {}) } }
        val teaching = referencePronounTeaching
        teaching.pronounIds.forEach { id ->
            val forms = personalPronouns.getValue(id)
            teaching.contexts.forEach { context ->
                val value = if (context.id == GramCase.LOC) forms.getValue(GramCase.LOC) else context.value(id, forms)
                composeRule.existsDescribedAs("Было: $id. Стало: $value")
            }
        }
        possessives.forEach { possessive ->
            val base = teaching.demo.phrase(possessive.id, GramCase.NOM)
            teaching.demo.cases.forEach { row ->
                val phrase = teaching.demo.phrase(possessive.id, row.id)
                composeRule.existsDescribedAs("Было: $base. Стало: $phrase")
            }
        }
    }
}

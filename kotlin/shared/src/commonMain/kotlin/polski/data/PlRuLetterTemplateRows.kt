package polski.data

import polski.data.rows.plru.lettertemplate.LETTER_MISTAKE_ROW_ROWS
import polski.data.rows.plru.lettertemplate.LETTER_PHRASE_ROW_ROWS
import polski.data.rows.plru.lettertemplate.LETTER_SECTION_ROW_ROWS
import polski.data.rows.plru.lettertemplate.LETTER_TASK_LINK_ROW_ROWS
import polski.data.rows.plru.lettertemplate.LETTER_TEMPLATE_ROW_ROWS
import polski.data.rows.plru.lettertemplate.MODEL_LETTER_ROW_ROWS
import polski.exam.template.LetterMistake
import polski.exam.template.LetterPhrase
import polski.exam.template.LetterSection
import polski.exam.template.LetterTaskLink
import polski.exam.template.LetterTemplate
import polski.exam.template.LetterTemplateModel
import polski.exam.template.ModelLetter

/** pl-ru letter template model from generated rows; consumed by [polski.exam.template.LetterTemplateSourceData]. */
val plRuLetterTemplates: LetterTemplateModel by lazy { buildPlRuLetterTemplates() }

private fun buildPlRuLetterTemplates(): LetterTemplateModel {
    val phrasesBySection = LETTER_PHRASE_ROW_ROWS.groupBy { it.sectionRef }
    val sectionsByTemplate = LETTER_SECTION_ROW_ROWS
        .groupBy { it.templateRef }
        .mapValues { (_, sections) ->
            sections.sortedBy { it.order }.map { s ->
                LetterSection(
                    id = s.sectionId,
                    order = s.order,
                    sectionKey = s.sectionKey,
                    labelNative = s.labelNative,
                    noteNative = s.noteNative,
                    phrases = phrasesBySection[s.sectionId].orEmpty().map { p ->
                        LetterPhrase(
                            id = p.phraseId,
                            targetText = p.targetText,
                            nativeNote = p.nativeNote,
                            register = p.register,
                            isPreferred = p.isPreferred ?: false,
                            mistakeNote = p.mistakeNote,
                            verificationStatus = p.verificationStatus,
                        )
                    },
                )
            }
        }
    val mistakesByTemplate = LETTER_MISTAKE_ROW_ROWS.groupBy { it.templateRef }
    val letterByTemplate = MODEL_LETTER_ROW_ROWS.associateBy { it.templateRef }

    val templates = LETTER_TEMPLATE_ROW_ROWS.map { t ->
        LetterTemplate(
            id = t.templateId,
            letterType = t.letterType,
            register = t.register,
            titleNative = t.titleNative,
            sections = sectionsByTemplate[t.templateId].orEmpty(),
            mistakes = mistakesByTemplate[t.templateId].orEmpty().map { m ->
                LetterMistake(
                    id = m.mistakeId,
                    wrongForm = m.wrongForm,
                    correctForm = m.correctForm,
                    ruleNative = m.ruleNative,
                    verificationStatus = m.verificationStatus,
                )
            },
            modelLetter = letterByTemplate[t.templateId]?.let { l ->
                ModelLetter(
                    id = l.letterId,
                    titleNative = l.titleNative,
                    fullText = l.fullText,
                    annotationNative = l.annotationNative,
                    verificationStatus = l.verificationStatus,
                )
            },
        )
    }
    val links = LETTER_TASK_LINK_ROW_ROWS.map { r ->
        LetterTaskLink(linkId = r.linkId, taskRef = r.taskRef, templateRef = r.templateRef)
    }
    return LetterTemplateModel(templates, links)
}

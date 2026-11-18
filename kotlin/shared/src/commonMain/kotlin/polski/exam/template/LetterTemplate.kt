package polski.exam.template

import polski.data.rows.plru.lettertemplate.LetterRegister
import polski.data.rows.plru.lettertemplate.LetterTemplateRowLetterType
import polski.data.rows.plru.lettertemplate.LetterSectionRowSectionKey
import polski.data.rows.plru.lettertemplate.LetterVerificationStatus

// Generated enums are reused via typealiases — never redeclared (P7).
typealias LetterType = LetterTemplateRowLetterType
typealias SectionKey = LetterSectionRowSectionKey

data class LetterPhrase(
    val id: String,
    val targetText: String,
    val nativeNote: String?,
    val register: LetterRegister,
    val isPreferred: Boolean,
    val mistakeNote: String?,
    val verificationStatus: LetterVerificationStatus,
)

data class LetterSection(
    val id: String,
    val order: Int,
    val sectionKey: SectionKey,
    val labelNative: String,
    val noteNative: String?,
    val phrases: List<LetterPhrase>,
)

data class LetterMistake(
    val id: String,
    val wrongForm: String,
    val correctForm: String,
    val ruleNative: String,
    val verificationStatus: LetterVerificationStatus,
)

data class ModelLetter(
    val id: String,
    val titleNative: String,
    val fullText: String,
    val annotationNative: String?,
    val verificationStatus: LetterVerificationStatus,
)

data class LetterTemplate(
    val id: String,
    val letterType: LetterType,
    val register: LetterRegister,
    val titleNative: String,
    val sections: List<LetterSection>,
    val mistakes: List<LetterMistake>,
    val modelLetter: ModelLetter?,
)

data class LetterTaskLink(val linkId: String, val taskRef: String, val templateRef: String)

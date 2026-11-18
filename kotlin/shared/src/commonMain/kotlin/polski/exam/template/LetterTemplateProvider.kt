package polski.exam.template

interface LetterTemplateProvider {
    fun forPack(packId: String): LetterTemplateModel
}

/** Static provider backed by generated rows. Returns empty model for packs with no data. */
object StaticLetterTemplateProvider : LetterTemplateProvider {
    override fun forPack(packId: String): LetterTemplateModel = when (packId) {
        "pl-ru" -> plRuLetterTemplates
        else -> LetterTemplateModel(emptyList(), emptyList())
    }
}

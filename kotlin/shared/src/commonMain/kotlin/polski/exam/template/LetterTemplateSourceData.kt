package polski.exam.template

/** pl-ru letter template model, lazy-initialized once via the mapper. No IO. */
val plRuLetterTemplates: LetterTemplateModel by lazy { polski.data.plRuLetterTemplates }

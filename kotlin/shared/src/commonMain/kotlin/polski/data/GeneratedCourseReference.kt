// Generated from courses-biome/pl-ru/reference.core-data; do not edit.
// SHA-256: 3306d03cefeff36a9fc193c675be3624f5305ac741e6315067b395b217433dff
package polski.data

import polski.model.GramCase

/** Typed pl-ru caseReferenceRows from Biome literal source. */
internal val generatedPlRuCaseReferenceRows: List<CaseReferenceRow> = listOf(
    CaseReferenceRow(GramCase.fromId("nom"), "Mianownik", "Именительный", "kto? co?", "To jest…", null),
    CaseReferenceRow(GramCase.fromId("gen"), "Dopełniacz", "Родительный", "kogo? czego?", "nie widzę / nie mam / bez / do", "case.gen.neg"),
    CaseReferenceRow(GramCase.fromId("dat"), "Celownik", "Дательный", "komu? czemu?", "daję / pomagam", "case.dat"),
    CaseReferenceRow(GramCase.fromId("acc"), "Biernik", "Винительный", "kogo? co?", "widzę / mam / lubię", "case.acc.f"),
    CaseReferenceRow(GramCase.fromId("inst"), "Narzędnik", "Творительный", "kim? czym?", "z (с) / jestem", "case.inst"),
    CaseReferenceRow(GramCase.fromId("loc"), "Miejscownik", "Предложный", "o kim? o czym?", "o / w / na (место)", "case.loc"),
    CaseReferenceRow(GramCase.fromId("voc"), "Wołacz", "Обращение", "обращаемся к кому-то", "Żono! Kolego!", null),

)

/** Typed pl-ru referenceGenderNames from Biome literal source. */
internal val generatedPlRuReferenceGenderNames: Map<String, String> = mapOf("m-personal" to "мужской · человек", "m-animate" to "мужской · животное", "m-inanimate" to "мужской · предмет", "f" to "женский", "n" to "средний")

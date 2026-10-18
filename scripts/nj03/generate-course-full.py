#!/usr/bin/env python3
"""Generate GeneratedCourseFull.kt: all remaining course fields for pl-ru.

Covers: chainPresentation, stemAlternations, comparisonNounIds, matrixIntroduction,
webCaseCompositionHeader, maleAccIntro, contextHelp, aspectNoPresent,
vocabularyInstructions, vocabularyUnavailableLabel, referenceChainRows,
maleAccRows, tenseRows, aspectRows, systemCards, pipeline,
caseTeaching, verbTeaching, pronounTeaching, russianSupport.
"""
from __future__ import annotations
import hashlib, sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseFull.kt"


def ks(s: str) -> str:
    if not isinstance(s, str): s = str(s)
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def sl(items: list[str]) -> str:
    return f'listOf({", ".join(chr(34)+ks(i)+chr(34) for i in items)})'


def read(fname: str, tag: str) -> list:
    p = BIOME / fname
    return [parse(l[len(tag)+1:]) for l in p.read_text("utf-8").splitlines()
            if l.startswith(tag + " ")]


def main() -> None:
    ref = read("reference.core-data", "REFERENCE")[0]
    training = read("training.core-data", "TRAINING")[0]
    vocab = read("vocabulary.core-data", "VOCABULARY")[0]
    stem_alts = read("stemAlternations.core-data", "STEM_ALT")

    cp = training["chainPresentation"]
    steps_kt = ", ".join(f'ChainStep("{ks(s["id"])}", "{ks(s["label"])}")' for s in cp["steps"])
    compl = cp["completion"]
    compl_kt = (f'ChainCompletion("{ks(compl["title"])}", "{ks(compl["reactEyebrow"])}", '
                f'"{ks(compl["reactBody"])}", "{ks(compl["webBody"])}")')

    # stemAlternations
    stem_kt = ", ".join(f"StemAlternation('{a['a'][0]}', '{a['b'][0]}')" for a in stem_alts)

    # Simple reference strings
    matrix_intro = ks(ref["matrixIntroduction"])
    web_case_hdr = ks(ref["webCaseCompositionHeader"])
    male_acc_intro = ks(ref["maleAccIntro"])
    comparison_ids = sl(ref["comparisonNounIds"])
    ctx = ref["contextHelp"]
    ctx_kt = f'CourseContextHelp("{ks(ctx["react"])}", "{ks(ctx["compact"])}")'
    anp = ref["aspectNoPresent"]
    anp_kt = f'CourseAspectNoPresent("{ks(anp["compact"])}", "{ks(anp["ios"])}")'

    vi = vocab["instructions"]
    vi_kt = f'CourseVocabularyInstructions("{ks(vi["react"])}", "{ks(vi["web"])}", "{ks(vi["native"])}", "{ks(vi["ios"])}")'
    vunavail = ks(vocab["unavailableLabel"])

    # referenceChainRows
    chain_rows = ", ".join(f'ReferenceChainRow("{ks(r["label"])}", "{ks(r["from"])}", "{ks(r["to"])}", "{ks(r["change"])}")'
                           for r in ref["chainRows"])

    # tenseRows
    tense_rows = ", ".join(f'ReferenceTenseRow("{ks(r["label"])}", "{ks(r["from"])}", "{ks(r["to"])}")'
                           for r in ref["tenseRows"])

    # aspectRows
    def asp_row(r: dict) -> str:
        present = f'"{ks(r["present"])}"' if r.get("present") else "null"
        return (f'ReferenceAspectRow("{ks(r["label"])}", "{ks(r["from"])}", '
                f'{present}, "{ks(r["past"])}", "{ks(r["future"])}")')
    aspect_rows = ", ".join(asp_row(r) for r in ref["aspectRows"])

    # maleAccRows
    def male_acc_row(r: dict) -> str:
        examples = ", ".join(f'MaleAccExample("{ks(e["from"])}", "{ks(e["to"])}", "{ks(e["sentence"])}")'
                             for e in r.get("examples", []))
        return (f'MaleAccRow("{ks(r["id"])}", "{ks(r["label"])}", "{ks(r["title"])}", '
                f'listOf({examples}), "{ks(r["rule"])}")')
    male_acc_rows = ", ".join(male_acc_row(r) for r in ref["maleAccRows"])

    # systemCards
    def sys_card(c: dict) -> str:
        steps = sl(c.get("steps", []))
        return (f'ReferenceSystemCard("{ks(c["id"])}", "{ks(c["title"])}", '
                f'"{ks(c["explanation"])}", "{ks(c["example"])}", {steps})')
    sys_cards = ", ".join(sys_card(c) for c in ref["systemCards"])

    # pipeline
    pip = ref["pipeline"]
    pip_steps = ", ".join(f'ReferencePipelineStep("{ks(s["id"])}", "{ks(s["label"])}", "{ks(s["question"])}", "{ks(s["example"])}")'
                          for s in pip["steps"])
    pipeline_kt = (f'ReferencePipeline("{ks(pip["title"])}", listOf({pip_steps}), '
                   f'"{ks(pip["compactExample"])}")')

    sha = hashlib.sha256((BIOME / "reference.core-data").read_bytes()).hexdigest()

    content = f"""\
// Generated from courses-biome/pl-ru/; do not edit.
// reference SHA-256: {sha}
package polski.data

import polski.model.GramCase
import polski.model.Gender
import polski.model.NumberGram
import polski.model.Person
import polski.model.Tense
import polski.presentation.StemAlternation

// --- chainPresentation ---
internal val generatedPlRuChainPresentation = ChainPresentation(
    listOf({steps_kt}), {compl_kt})

// --- stemAlternations ---
internal val generatedPlRuStemAlternations: List<StemAlternation> =
    listOf({stem_kt})

// --- reference simple strings ---
internal val generatedPlRuMatrixIntroduction = "{matrix_intro}"
internal val generatedPlRuWebCaseCompositionHeader = "{web_case_hdr}"
internal val generatedPlRuMaleAccIntro = "{male_acc_intro}"
internal val generatedPlRuComparisonNounIds: List<String> = {comparison_ids}
internal val generatedPlRuContextHelp = {ctx_kt}
internal val generatedPlRuAspectNoPresent = {anp_kt}
internal val generatedPlRuVocabularyInstructions = {vi_kt}
internal val generatedPlRuVocabularyUnavailableLabel = "{vunavail}"

// --- reference rows ---
internal val generatedPlRuReferenceChainRows: List<ReferenceChainRow> = listOf({chain_rows})
internal val generatedPlRuReferenceTenseRows: List<ReferenceTenseRow> = listOf({tense_rows})
internal val generatedPlRuReferenceAspectRows: List<ReferenceAspectRow> = listOf({aspect_rows})
internal val generatedPlRuMaleAccRows: List<MaleAccRow> = listOf({male_acc_rows})
internal val generatedPlRuReferenceSystemCards: List<ReferenceSystemCard> = listOf({sys_cards})
internal val generatedPlRuReferencePipeline: ReferencePipeline = {pipeline_kt}
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()

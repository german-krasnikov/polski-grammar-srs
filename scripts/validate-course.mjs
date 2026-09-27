import { readFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import Ajv2020 from 'ajv/dist/2020.js';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));
// exerciseCopy is a required-key dictionary; its text values share one additionalProperties rule.
const ajv = new Ajv2020({ allErrors: true, strict: true, strictRequired: false });
const courseSchema = ajv.compile(readJson('courses/schema/course-pack-v1.schema.json'));
const frequencySchema = ajv.compile(readJson('courses/schema/frequency-top1000-v1.schema.json'));
const styleRecipeSchema = ajv.compile(readJson('courses/schema/style-recipe-v1.schema.json'));
const styleIds = ['rule-first', 'situation-first', 'native-contrast', 'minimal-theory'];

function assertSchema(validate, data, prefix) {
  if (validate(data)) return;
  const first = validate.errors[0];
  const suffix = first.keyword === 'required' ? `/${first.params.missingProperty}` :
    first.keyword === 'additionalProperties' ? `/${first.params.additionalProperty}` : '';
  throw new Error(`${prefix}${first.instancePath}${suffix}: ${first.message}`);
}

function uniqueBy(rows, key, path) {
  const seen = new Set();
  rows.forEach((row, index) => {
    if (seen.has(row[key])) throw new Error(`${path}/${index}/${key}: duplicate ${row[key]}`);
    seen.add(row[key]);
  });
}

function noHtml(value, path = '') {
  if (typeof value === 'string' && /<\/?[A-Za-z][^>]*>/.test(value)) {
    throw new Error(`${path || '/'}: HTML markup is not course data`);
  }
  if (Array.isArray(value)) value.forEach((child, index) => noHtml(child, `${path}/${index}`));
  else if (value && typeof value === 'object') {
    for (const [key, child] of Object.entries(value)) noHtml(child, `${path}/${key}`);
  }
}

/** Rejects an accidental empty array or empty table in a skill's optional styleContent.
 *  styleContent itself, and nativeParallel within it, are never required here: a skill with
 *  no authored nativeParallel is a normal, supported state (StylesBlueprint.md §2-§3, ST-03) —
 *  StyleComposer.resolveEffectiveStyle (CORE) falls the native-contrast style back to rule-first
 *  for that skill at runtime, so this generic validator must not make partial coverage a
 *  build-time error for pl-ru or for any future pack. */
function validateStyleContent(skills) {
  skills.forEach((skill, index) => {
    const path = `/skills/${index}/styleContent`;
    const content = skill.styleContent;
    if (!content) return;
    if (content.nativeParallel && content.nativeParallel.length === 0) {
      throw new Error(`${path}/nativeParallel: omit the field instead of an empty list`);
    }
    if (content.examples && content.examples.length === 0) {
      throw new Error(`${path}/examples: omit the field instead of an empty list`);
    }
    if (content.table && content.table.rows.length === 0) {
      throw new Error(`${path}/table/rows: omit the field instead of an empty list`);
    }
  });
}

/** Rejects a recipe set that could crash or leak the answer before reveal: unknown/duplicate
 *  ids, a fallback chain (fallback target with its own requires), or changes/contrast on front. */
function validateStyleRecipes() {
  const recipes = styleIds.map((id) => {
    const recipe = readJson(`courses/styles/${id}.json`);
    assertSchema(styleRecipeSchema, recipe, `styles/${id}.json`);
    noHtml(recipe, `/styles/${id}`);
    if (recipe.id !== id) throw new Error(`/styles/${id}.json/id: expected ${id}, filename must match`);
    return recipe;
  });
  uniqueBy(recipes, 'id', '/styles');
  const byId = new Map(recipes.map((recipe) => [recipe.id, recipe]));
  for (const recipe of recipes) {
    if (recipe.blocks.front.some((kind) => kind === 'changes' || kind === 'contrast')) {
      throw new Error(`/styles/${recipe.id}.json/blocks/front: changes/contrast would leak the answer before reveal`);
    }
    if (recipe.fallback) {
      const target = byId.get(recipe.fallback);
      if (!target) throw new Error(`/styles/${recipe.id}.json/fallback: unknown style ${recipe.fallback}`);
      if (target.requires?.length) {
        throw new Error(`/styles/${recipe.id}.json/fallback: target ${recipe.fallback} must not itself declare requires`);
      }
    }
  }
}

function validatePrerequisites(skills) {
  const byId = new Map(skills.map((skill, index) => [skill.id, { skill, index }]));
  const visiting = new Set();
  const visited = new Set();
  function visit(id) {
    if (visiting.has(id)) throw new Error(`/skills/${byId.get(id).index}/prerequisites: prerequisite cycle at ${id}`);
    if (visited.has(id)) return;
    visiting.add(id);
    const { skill, index } = byId.get(id);
    skill.prerequisites.forEach((prerequisite, position) => {
      if (!byId.has(prerequisite)) {
        throw new Error(`/skills/${index}/prerequisites/${position}: unknown skill ${prerequisite}`);
      }
      visit(prerequisite);
    });
    visiting.delete(id);
    visited.add(id);
  }
  skills.forEach((skill) => visit(skill.id));
}

const patternTokens = {
  seenAcc: ['acc'], seenGenNeg: ['gen'], caseDrill: ['start', 'target'],
  verbSentence: ['nom', 'verb'], verbFutureAccepted: ['nom', 'past'],
  questionSource: ['acc'], questionExpected: ['acc'],
  mixedMale: ['gen'], mixedFemale: ['gen'], chainPast: ['acc'],
  chainNeg: ['gen'], chainOwner: ['theirGen'], chainLoc: ['theirLoc'],
};

function validatePatterns(patterns) {
  for (const [key, expected] of Object.entries(patternTokens)) {
    const pattern = patterns[key];
    const found = [...pattern.matchAll(/\{([A-Za-z][A-Za-z0-9]*)\}/g)].map(match => match[1]);
    if (found.join('\0') !== expected.join('\0') || /[{}]/.test(pattern.replace(/\{[A-Za-z][A-Za-z0-9]*\}/g, ''))) {
      throw new Error(`/exercisePatterns/${key}: expected placeholders ${expected.join(', ')}`);
    }
  }
}

/** A "*To" exerciseCopy key feeds FormChange.to verbatim (ExerciseFactory.kt, generator.ts). It
 *  must be one literal matching `expected`; alternatives (e.g. gender variants) belong in
 *  `accepted` instead, or `wholePhraseStart` cannot find the change in the rendered sentence and
 *  the ending highlight silently disappears (EmphasisUXAudit-2026-09-27 E4). */
function validateNoChangeAlternatives(exerciseCopy) {
  for (const [key, value] of Object.entries(exerciseCopy)) {
    if (key.endsWith('To') && typeof value === 'string' && value.includes(' / ')) {
      throw new Error(`/exerciseCopy/${key}: FormChange.to must be a single literal, not alternatives joined by ' / '`);
    }
  }
}

/** Rejects malformed author data without mutating the pack or the learner's progress. */
export function validateCoursePack(course, frequency) {
  assertSchema(courseSchema, course, 'course');
  assertSchema(frequencySchema, frequency, 'frequency');
  noHtml(course);
  validateNoChangeAlternatives(course.exerciseCopy);
  validateStyleContent(course.skills);
  validateStyleRecipes();
  validatePatterns(course.exercisePatterns);
  for (const section of ['nouns', 'adjectives', 'verbs', 'possessives', 'skills']) {
    uniqueBy(course[section], 'id', `/${section}`);
  }
  const ownerKinds = new Map([
    ['my', 'declined'], ['your', 'declined'], ['his', 'invariant'],
    ['her', 'invariant'], ['our', 'declined'], ['yourPlural', 'declined'], ['their', 'invariant'],
  ]);
  course.possessives.forEach((owner, index) => {
    const expected = ownerKinds.get(owner.id);
    if (!expected) throw new Error(`/possessives/${index}/id: unknown owner ${owner.id}`);
    if (owner.forms.kind !== expected) throw new Error(`/possessives/${index}/forms/kind: expected ${expected}`);
  });
  for (const id of ownerKinds.keys()) {
    if (!course.possessives.some(owner => owner.id === id)) throw new Error(`/possessives: missing owner ${id}`);
  }
  // Each declared stem alternation (UC S2) must be a real pair, and pairs are unordered so a↔b
  // reversed still counts as a duplicate.
  const seenAlternations = new Set();
  (course.stemAlternations ?? []).forEach((pair, index) => {
    const path = `/stemAlternations/${index}`;
    if (pair.a === pair.b) throw new Error(`${path}: a and b must differ`);
    const key = [pair.a, pair.b].sort().join('');
    if (seenAlternations.has(key)) throw new Error(`${path}: duplicate alternation`);
    seenAlternations.add(key);
  });
  const auxiliaryId = course.morphology.futureAuxiliary.verbId;
  if (!course.verbs.some(verb => verb.id === auxiliaryId && verb.aspect === 'imperfective')) {
    throw new Error(`/morphology/futureAuxiliary/verbId: unknown or non-imperfective verb ${auxiliaryId}`);
  }
  uniqueBy(course.vocabulary.items, 'id', '/vocabulary/items');
  uniqueBy(course.vocabulary.items, 'lemma', '/vocabulary/items');
  const chainSteps = course.training.chainPresentation.steps;
  const expectedChainIds = ['acc', 'past', 'neg', 'owner', 'loc'];
  chainSteps.forEach((step, index) => {
    if (step.id !== expectedChainIds[index]) {
      throw new Error(`/training/chainPresentation/steps/${index}/id: expected ${expectedChainIds[index]}`);
    }
  });
  if (course.reference.chainRows.length !== chainSteps.length) {
    throw new Error('/reference/chainRows: must match training chain step count');
  }
  const skillIds = new Set(course.skills.map(skill => skill.id));
  const pluralFormula = course.skills.find(skill => skill.id === 'sentence.plural')?.formula;
  if (!pluralFormula?.startsWith(`${course.reference.webCaseCompositionHeader} → `)) {
    throw new Error('/reference/webCaseCompositionHeader: must be the leading composition in sentence.plural formula');
  }
  const chainSkillIds = [
    ['case.acc.f', 'case.acc.n', 'case.acc.m'],
    ['verb.past'], ['case.gen.neg'], ['agreement.my'], ['case.loc'],
  ];
  chainSkillIds.forEach((variants, index) => {
    for (const id of variants) {
      if (!skillIds.has(id)) {
        throw new Error(`/training/chainPresentation/steps/${index}/id: missing chain skill ${id}`);
      }
    }
  });
  validatePrerequisites(course.skills);
  const pronouns = course.reference.pronounTeaching;
  const orderedPronounIds = ['ja', 'ty', 'on', 'ona', 'ono', 'my', 'wy', 'oni', 'one'];
  pronouns.personal.pronounIds.forEach((id, index) => {
    if (id !== orderedPronounIds[index] || !course.personalPronouns[id]) {
      throw new Error(`/reference/pronounTeaching/personal/pronounIds/${index}: invalid pronoun ${id}`);
    }
  });
  const orderedContexts = ['gen', 'dat', 'acc', 'inst', 'loc'];
  pronouns.contexts.forEach((context, index) => {
    const path = `/reference/pronounTeaching/contexts/${index}`;
    if (context.id !== orderedContexts[index]) throw new Error(`${path}/id: expected ${orderedContexts[index]}`);
    if (context.caseId !== context.id) throw new Error(`${path}/caseId: must match id`);
    if (context.cue.full !== context.cue.compact) throw new Error(`${path}/cue/compact: must match full`);
    if (context.id === 'inst') {
      if (context.valuePrefix !== 'z ' || context.specialValues?.ja !== 'ze mną') {
        throw new Error(`${path}/specialValues/ja: expected instrumental pattern`);
      }
    } else if (context.id === 'loc') {
      if (context.valuePrefix !== 'o ' || context.specialValues) throw new Error(`${path}/valuePrefix: expected locative pattern`);
    } else if (context.valuePrefix || context.specialValues) {
      throw new Error(`${path}/valuePrefix: unexpected pattern`);
    }
  });
  const demo = pronouns.possessive.demo;
  const demoNoun = course.nouns.find(noun => noun.id === demo.nounId);
  if (!demoNoun || demoNoun.gender !== 'f') throw new Error('/reference/pronounTeaching/possessive/demo/nounId: expected feminine noun');
  if (!course.adjectives.some(adjective => adjective.id === demo.adjectiveId)) {
    throw new Error('/reference/pronounTeaching/possessive/demo/adjectiveId: unknown adjective');
  }
  demo.cases.forEach((row, index) => {
    if (row.id !== ['nom', 'acc', 'gen'][index]) throw new Error(`/reference/pronounTeaching/possessive/demo/cases/${index}/id: wrong order`);
  });
  demo.invariableOwnerIds.forEach((id, index) => {
    if (id !== ['his', 'her', 'their'][index] || !course.possessives.some(owner => owner.id === id)) {
      throw new Error(`/reference/pronounTeaching/possessive/demo/invariableOwnerIds/${index}: invalid owner`);
    }
  });
  const teaching = course.reference.verbTeaching;
  const expectedSubjects = [
    ['ja', 1, 'sg', 'selected', undefined], ['ty', 2, 'sg', 'selected', undefined],
    ['on', 3, 'sg', 'fixed', 'm-personal'], ['ona', 3, 'sg', 'fixed', 'f'],
    ['ono', 3, 'sg', 'fixed', 'n'], ['my', 1, 'pl', 'selected', undefined],
    ['wy', 2, 'pl', 'selected', undefined], ['oni', 3, 'pl', 'fixed', 'm-personal'],
    ['one', 3, 'pl', 'fixed', 'f'],
  ];
  teaching.subjects.forEach((subject, index) => {
    const expected = expectedSubjects[index];
    for (const [field, value] of [['id', expected[0]], ['person', expected[1]],
      ['number', expected[2]], ['genderMode', expected[3]], ['fixedGender', expected[4]]]) {
      if (subject[field] !== value) {
        throw new Error(`/reference/verbTeaching/subjects/${index}/${field}: expected ${value ?? 'absent'}`);
      }
    }
  });
  teaching.genderOptions.forEach((option, index) => {
    if (option.id !== ['m', 'f'][index]) {
      throw new Error(`/reference/verbTeaching/genderOptions/${index}/id: expected ${['m', 'f'][index]}`);
    }
  });
  uniqueBy(course.reference.caseRows, 'id', '/reference/caseRows');
  const expectedSupportIds = ['accusative', 'instrumental', 'locative', 'possessive'];
  course.reference.russianSupport.rows.forEach((row, index) => {
    if (row.id !== expectedSupportIds[index]) {
      throw new Error(`/reference/russianSupport/rows/${index}/id: expected ${expectedSupportIds[index]}`);
    }
    if (!row.mobileLine.startsWith(`${row.cue} → `)) {
      throw new Error(`/reference/russianSupport/rows/${index}/mobileLine: must begin with cue`);
    }
    if (!Array.isArray(row.comparisons) || !row.comparisons.length) {
      throw new Error(`/reference/russianSupport/rows/${index}/comparisons: at least one pair is required`);
    }
    row.comparisons.forEach((pair, pairIndex) => {
      const path = `/reference/russianSupport/rows/${index}/comparisons/${pairIndex}`;
      for (const [side, value] of [['beforeParts', pair.from], ['afterParts', pair.to]]) {
        const parts = pair[side];
        if (parts.map(part => part.text).join('') !== value) {
          throw new Error(`${path}/${side}: segments must reassemble exactly`);
        }
        let cursor = 0;
        parts.forEach((part, partIndex) => {
          const next = cursor + part.text.length;
          const before = value[cursor - 1] ?? '';
          const after = value[next] ?? '';
          const word = (char) => /[\p{L}\p{N}]/u.test(char);
          if (part.isEnding && (!part.isChanged || part.text.length > 3 || !/^\p{L}+$/u.test(part.text) ||
              !word(before) || word(after))) {
            throw new Error(`${path}/${side}/${partIndex}/isEnding: invalid ending span`);
          }
          if (part.isChanged && !part.isEnding && (word(before) || word(after))) {
            throw new Error(`${path}/${side}/${partIndex}/isChanged: replacement must mark a whole word`);
          }
          cursor = next;
        });
      }
      if (pair.from !== pair.to && (!pair.beforeParts.some(part => part.isChanged) ||
          !pair.afterParts.some(part => part.isChanged))) {
        throw new Error(`${path}: both changed forms need marked spans`);
      }
      if (pair.beforeParts.filter(part => !part.isChanged).map(part => part.text).join('') !==
          pair.afterParts.filter(part => !part.isChanged).map(part => part.text).join('') ||
          (pair.from === pair.to && (pair.beforeParts.some(part => part.isChanged) ||
            pair.afterParts.some(part => part.isChanged)))) {
        throw new Error(`${path}: unchanged fragments must agree on both sides`);
      }
    });
  });
  const expectedPipelineIds = ['intent', 'case', 'agreement'];
  course.reference.pipeline.steps.forEach((step, index) => {
    if (step.id !== expectedPipelineIds[index]) {
      throw new Error(`/reference/pipeline/steps/${index}/id: expected ${expectedPipelineIds[index]}`);
    }
  });
  const expectedSystemCardIds = ['noun', 'agreement', 'verb', 'modifiers'];
  course.reference.systemCards.forEach((card, index) => {
    if (card.id !== expectedSystemCardIds[index]) {
      throw new Error(`/reference/systemCards/${index}/id: expected ${expectedSystemCardIds[index]}`);
    }
    if (card.steps.join(' → ') !== card.example) {
      throw new Error(`/reference/systemCards/${index}/steps: must join with ' → ' into example`);
    }
  });
  uniqueBy(course.reference.maleAccRows, 'id', '/reference/maleAccRows');
  const expectedAccTypes = ['person', 'animal', 'object'];
  course.reference.maleAccRows.forEach((row, index) => {
    if (row.id !== expectedAccTypes[index]) throw new Error(`/reference/maleAccRows/${index}/id: expected ${expectedAccTypes[index]}`);
    row.examples.forEach((example, exampleIndex) => {
      if (!example.sentence.includes(example.to)) {
        throw new Error(`/reference/maleAccRows/${index}/examples/${exampleIndex}/sentence: must contain target phrase`);
      }
    });
  });
  const expectedCases = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
  course.reference.caseRows.forEach((row, index) => {
    if (row.id !== expectedCases[index]) throw new Error(`/reference/caseRows/${index}/id: expected ${expectedCases[index]}`);
    if (row.skill && !course.skills.some(skill => skill.id === row.skill)) {
      throw new Error(`/reference/caseRows/${index}/skill: unknown skill ${row.skill}`);
    }
  });
  course.reference.chainRows.forEach((row, index) => {
    if (index > 0 && row.from !== course.reference.chainRows[index - 1].to) {
      throw new Error(`/reference/chainRows/${index}/from: must equal previous to`);
    }
  });
  const tenseRows = course.reference.tenseRows;
  tenseRows.forEach((row, index) => {
    const expectedFrom = index === 2 ? tenseRows[1].to : tenseRows[0].to;
    if (row.from !== expectedFrom) {
      throw new Error(`/reference/tenseRows/${index}/from: must equal ${index === 2 ? 'past' : 'present'} baseline`);
    }
  });
  const nounIds = new Set(course.nouns.map((noun) => noun.id));
  const comparisonIds = new Set();
  course.reference.comparisonNounIds.forEach((id, index) => {
    if (!nounIds.has(id)) throw new Error(`/reference/comparisonNounIds/${index}: unknown noun ${id}`);
    if (comparisonIds.has(id)) throw new Error(`/reference/comparisonNounIds/${index}: duplicate noun ${id}`);
    comparisonIds.add(id);
  });
  const adjectiveIds = new Set(course.adjectives.map((adjective) => adjective.id));
  const seenSeeds = new Set();
  course.sentenceSeeds.forEach((seed, index) => {
    if (!nounIds.has(seed.nounId)) throw new Error(`/sentenceSeeds/${index}/nounId: unknown noun ${seed.nounId}`);
    if (!adjectiveIds.has(seed.adjectiveId)) {
      throw new Error(`/sentenceSeeds/${index}/adjectiveId: unknown adjective ${seed.adjectiveId}`);
    }
    const key = `${seed.nounId}\0${seed.adjectiveId}`;
    if (seenSeeds.has(key)) throw new Error(`/sentenceSeeds/${index}: duplicate seed`);
    seenSeeds.add(key);
  });

  const verbs = new Map(course.verbs.map((verb) => [verb.id, verb]));
  const aspectLemmas = new Map(course.verbs.map((verb) => [verb.lemma, verb]));
  course.reference.aspectRows.forEach((row, index) => {
    const verb = aspectLemmas.get(row.from);
    if (!verb) throw new Error(`/reference/aspectRows/${index}/from: unknown verb ${row.from}`);
    if ((row.present === null) !== (verb.aspect === 'perfective')) {
      throw new Error(`/reference/aspectRows/${index}/present: must match verb aspect`);
    }
  });
  course.verbs.forEach((verb, index) => {
    if (verb.perfectivePair && verbs.get(verb.perfectivePair)?.aspect !== 'perfective') {
      throw new Error(`/verbs/${index}/perfectivePair: unknown perfective verb ${verb.perfectivePair}`);
    }
  });
  if (course.targetLanguage === course.nativeLanguage) {
    throw new Error('/nativeLanguage: must differ from targetLanguage');
  }

  uniqueBy(frequency.items, 'lemma', '/frequency/items');
  frequency.items.forEach((entry, index) => {
    if (entry.rank !== index + 1) throw new Error(`/frequency/items/${index}/rank: expected ${index + 1}`);
  });
  course.vocabulary.items.forEach((item, index) => {
    if (item.frequencyRank !== null && frequency.items[item.frequencyRank - 1].lemma !== item.lemma) {
      throw new Error(`/vocabulary/items/${index}/frequencyRank: ranked lemma does not match ${item.lemma}`);
    }
  });
  return true;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const coursePath = process.argv[2] ?? fileURLToPath(new URL('../courses/pl-ru/course.json', import.meta.url));
    const frequencyPath = process.argv[3] ?? fileURLToPath(new URL('../courses/pl-ru/frequency-top1000.json', import.meta.url));
    validateCoursePack(JSON.parse(readFileSync(coursePath, 'utf8')), JSON.parse(readFileSync(frequencyPath, 'utf8')));
    process.stdout.write(`PASS course pack: ${coursePath}\n`);
  } catch (error) {
    process.stderr.write(`FAIL course pack: ${error.message}\n`);
    process.exitCode = 1;
  }
}

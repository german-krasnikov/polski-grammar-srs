// R1.11 (Plans/Biome/R1Design.md): the recipe expressions (R1.11a) and the constructions (R1.11b) against dsl/course.data-contract: what `biome data` rejects,
// with the exact position of each problem.
// A kind that carries a field of another kind, or lacks its own, is beyond the contract (no union): PlRuRecipeRows.kt refuses it, and the last case of each group says so.
// They need a biome-dsl checkout (see course-data.test.ts); without one they are skipped, which is NOT RUN, not a pass.
import { describe, expect, it } from 'vitest';
import { hasCli, row, rowsOf } from './support/course-data';

const bundle = '[{feature: "Number" value: "sg"} {feature: "Case" value: "nom"}]';
const CONSTRUCTION = '"core.argument.case-role"';
const phrase = `{construction: ${CONSTRUCTION} bundle: ${bundle}}`;
const value = (fields: Record<string, string>, change: Record<string, string | undefined> = {}) => row('VALUE', fields, change);
const COPY = { kind: 'copy', key: '"accPrompt"' };

describe.skipIf(!hasCli)('text values against the contract', () => {
  const { check, problem } = rowsOf('TextValueRow', 'CopyEntryRow', 'ConstructionRow', 'PossessiveRow');

  it('every kind is accepted, also capitalized, and an empty literal is data', () => {
    const rows = [
      value(COPY), value({ kind: 'literal', value: '""' }), value({ kind: 'phrase', capitalized: 'true', spec: phrase }),
      value({ kind: 'pronoun', genderToKey: '[{gender: "f" pronounId: "ona"} {gender: "default" pronounId: "on"}]', caseId: '"acc"' }),
    ];
    expect(check(rows.join('\n') + '\n')).toEqual({ status: 0, problems: [] });
  });
  it('a kind outside the enum is DATA_ENUM at the value', () => problem([value(COPY, { kind: 'copyy' })], 0, 'copyy', 'DATA_ENUM'));
  it('a copy key that is no copy entry is DATA_REF at the value', () => problem([value(COPY, { key: '"nope"' })], 0, '"nope"', 'DATA_REF'));
  it('a capitalized flag that is a text is DATA_TYPE at the value', () => problem([value(COPY, { capitalized: '"yes"' })], 0, '"yes"', 'DATA_TYPE'));
  it('a kind left out is DATA_REQUIRED at the { of the row', () => problem([value(COPY, { kind: undefined })], 0, '{', 'DATA_REQUIRED'));
  it('a field the contract does not know is DATA_UNKNOWN_FIELD at its key', () => problem([value(COPY, { extra: '1' })], 0, 'extra', 'DATA_UNKNOWN_FIELD'));
  it('a feature of a bundle without its value is DATA_REQUIRED at the { of the pair', () =>
    problem([value({ kind: 'phrase', spec: `{construction: ${CONSTRUCTION} bundle: [{feature: "Number"}]}` })], 0, '{feature: "Number"}', 'DATA_REQUIRED'));
  it('a pronoun without its id is DATA_REQUIRED at the { of the pair', () =>
    problem([value({ kind: 'pronoun', genderToKey: '[{gender: "f"}]', caseId: '"acc"' })], 0, '{gender: "f"}', 'DATA_REQUIRED'));
  it('a construction that is no construction is DATA_REF, an owner that is no possessive is DATA_REF', () => {
    problem([value({ kind: 'phrase', spec: '{construction: "core.nope" bundle: []}' })], 0, '"core.nope"', 'DATA_REF');
    problem([value({ kind: 'phrase', spec: `{construction: ${CONSTRUCTION} bundle: [] ownerLexeme: "nobody"}` })], 0, '"nobody"', 'DATA_REF');
  });
  it('a field of another kind is beyond the contract: it passes here and the mapper refuses it', () =>
    expect(check(value(COPY, { caseId: '"acc"' }) + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('text specs, reasons and changes against the contract', () => {
  const spec = rowsOf('TextSpecRow', 'CopyEntryRow', 'PatternEntryRow', 'ConstructionRow', 'PossessiveRow');
  const reason = rowsOf('ReasonSpecRow', 'CopyEntryRow');
  const change = rowsOf('ChangeSpecRow', 'CopyEntryRow', 'ConstructionRow', 'PossessiveRow');
  const text = (change: Record<string, string | undefined>) => row('SPEC', { kind: 'direct', value: '{kind: copy key: "accPrompt"}' }, change);

  it('every kind of a spec is accepted', () => {
    const rows = [
      text({}), text({ kind: 'pattern', value: undefined, key: '"seenAcc"', values: `[{name: "acc" value: {kind: phrase spec: ${phrase}}}]` }),
      text({ kind: 'concat', value: undefined, parts: '[{kind: copy key: "accPrompt"} {kind: literal value: "."}]' }),
      text({ kind: 'prefixed', value: undefined, spec: phrase, prefixCase: '"nom"', punct: '"."' }),
    ];
    expect(spec.check(rows.join('\n') + '\n')).toEqual({ status: 0, problems: [] });
  });
  it('a spec kind outside the enum is DATA_ENUM at the value', () => spec.problem([text({ kind: 'copy' })], 0, 'copy', 'DATA_ENUM'));
  it('a pattern key that is no pattern entry is DATA_REF at the value', () =>
    spec.problem([text({ kind: 'pattern', value: undefined, key: '"seenNope"', values: '[]' })], 0, '"seenNope"', 'DATA_REF'));
  it('a copy key inside a part that is no copy entry is DATA_REF at the value', () =>
    spec.problem([text({ kind: 'concat', value: undefined, parts: '[{kind: copy key: "nope"}]' })], 0, '"nope"', 'DATA_REF'));
  it('a pattern value without its name is DATA_REQUIRED at the { of the pair', () =>
    spec.problem([text({ kind: 'pattern', value: undefined, key: '"seenAcc"', values: '[{value: {kind: copy key: "accPrompt"}}]' })], 0, '{value: {kind: copy', 'DATA_REQUIRED'));

  const reasonRow = (fields: Record<string, string>) => row('REASON', fields);
  it('a fixed reason and a classified one with its rules are accepted', () => {
    const rows = [reasonRow({ kind: 'fixed', key: '"accPrompt"' }), reasonRow({ kind: 'classified', rules: '[{whenClass: "default" key: "accPrompt"}]' })];
    expect(reason.check(rows.join('\n') + '\n')).toEqual({ status: 0, problems: [] });
  });
  it('a reason kind outside the enum is DATA_ENUM at the value', () => reason.problem([reasonRow({ kind: 'fix', key: '"accPrompt"' })], 0, 'fix', 'DATA_ENUM'));
  it('a rule without its key is DATA_REQUIRED at the { of the rule', () =>
    reason.problem([reasonRow({ kind: 'classified', rules: '[{whenClass: "default"}]' })], 0, '{whenClass: "default"}', 'DATA_REQUIRED'));
  it('a rule whose key is no copy entry is DATA_REF at the value', () =>
    reason.problem([reasonRow({ kind: 'classified', rules: '[{whenClass: "default" key: "nope"}]' })], 0, '"nope"', 'DATA_REF'));

  it('a change needs both texts and a reason', () => {
    const fields = { from: '{kind: copy key: "accPrompt"}', to: '{kind: literal value: "x"}', reason: '{kind: fixed key: "accPrompt"}' };
    expect(change.check(row('CHANGE', fields) + '\n')).toEqual({ status: 0, problems: [] });
    change.problem([row('CHANGE', fields, { reason: undefined })], 0, '{', 'DATA_REQUIRED');
  });
});

describe.skipIf(!hasCli)('constructions against the contract', () => {
  const { check, problem } = rowsOf('ConstructionRow');
  const slot = (change: Record<string, string | undefined> = {}) => `{${Object.entries({ name: '"noun"', category: '"noun"', requiredFeatures: '["Number" "Case"]', ...change }).filter(([, v]) => v !== undefined).map(([k, v]) => `${k}: ${v}`).join(' ')}}`;
  const construction = (id: string, change: Record<string, string | undefined> = {}) => row('CONSTRUCTION', { id: `"${id}"`, slots: `[${slot()}]` }, change);

  it('slots, a government, conditions and ordered alternatives are accepted', () => {
    const full = construction('c', {
      slots: `[${slot({ optional: 'true', agreementSource: '"noun"', requiredFeaturesWhen: '[{condition: {feature: "Tense" value: "past"} features: ["Tense" "Gender"]}]' })}]`,
      govFeature: '"Case"', govDefault: '"acc"', govWhen: '[{condition: {feature: "Polarity" value: "neg"} result: "gen"}]',
      orderWhen: `[{condition: {feature: "Mood" value: "q"} slots: [${slot()}]}]`,
    });
    expect(check(full + '\n')).toEqual({ status: 0, problems: [] });
  });
  it('a second construction with the same id is DATA_DUPLICATE at its id', () => problem([construction('c'), construction('c')], 1, '"c"', 'DATA_DUPLICATE'));
  it('a slot without its features is DATA_REQUIRED at the { of the slot', () =>
    problem([construction('c', { slots: `[{name: "noun" category: "noun"}]` })], 0, '{name: "noun" category: "noun"}', 'DATA_REQUIRED'));
  it('a condition without its value is DATA_REQUIRED at the { of the condition', () =>
    problem([construction('c', { govWhen: '[{condition: {feature: "Polarity"} result: "gen"}]' })], 0, '{feature: "Polarity"}', 'DATA_REQUIRED'));
  it('a government result left out is DATA_REQUIRED at the { of the entry', () =>
    problem([construction('c', { govWhen: '[{condition: {feature: "Polarity" value: "neg"}}]' })], 0, '{condition:', 'DATA_REQUIRED'));
  it('an optional flag that is a text is DATA_TYPE at the value', () => problem([construction('c', { slots: `[${slot({ optional: '"yes"' })}]` })], 0, '"yes"', 'DATA_TYPE'));
  it('a slot field the contract does not know is DATA_UNKNOWN_FIELD at its key', () => problem([construction('c', { slots: `[${slot({ order: '1' })}]` })], 0, 'order', 'DATA_UNKNOWN_FIELD'));
  it('no row at all is DATA_COUNT at 1:1', () => expect(check('').problems).toEqual(['constructions.core-data:1:1: DATA_COUNT']));
  it('a government without its feature is beyond the contract: it passes here and the mapper refuses it', () =>
    expect(check(construction('c', { govDefault: '"acc"' }) + '\n')).toEqual({ status: 0, problems: [] }));
});

"""Regenerate locked exercise/evaluation cases from the React oracle fixtures."""
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[7]
OUT = Path(__file__).with_name('TrainingParityTest.kt')
EXERCISES = ROOT / 'tests/fixtures/kotlin-parity/exercises.json'
EVALUATION = ROOT / 'tests/fixtures/kotlin-parity/evaluation.json'
ex = json.loads(EXERCISES.read_text())
ev = json.loads(EVALUATION.read_text())
assert ex['schemaVersion'] == ev['schemaVersion'] == 1
assert ex['sourceRevision'] == ev['sourceRevision']
assert len(ex['cases']) == 112 and len(ev['cases']) == 9
for source in ('src/training/generator.ts', 'src/training/evaluator.ts', 'src/training/queue.ts', 'src/training/skills.ts'):
    locked = subprocess.check_output(['git', 'show', f'{ex["sourceRevision"]}:{source}'], cwd=ROOT)
    assert locked == (ROOT / source).read_bytes(), f'React oracle drifted: {source}'

def s(value):
    return json.dumps(value, ensure_ascii=False).replace('$', "\\$")

def seed(value):
    return 'SentenceSeed(%s, %s)' % (s(value['nounId']), s(value['adjectiveId']))

def exercise(value):
    accepted = value.get('accepted', [])
    changes = ', '.join('FormChange(%s, %s, %s)' % (s(x['from']), s(x['to']), s(x['reason'])) for x in value['changes'])
    return 'Exercise(%s, %s, %s, %s, %s, listOf(%s), %s, listOf(%s), %s, %s, PossessiveId.fromId(%s), NumberGram.fromId(%s), listOf(%s))' % (
        s('generated'), s(value['primarySkill']), s(value['source']), s(value['prompt']), s(value['expected']),
        ', '.join(map(s, accepted)), s(value['explanation']), ', '.join(map(s, value['tags'])),
        s(value['nounId']), s(value['adjectiveId']), s(value['possessive']), s(value['number']), changes)

lines = ['package polski.training', '', 'import kotlin.test.*', 'import polski.model.*', 'import polski.data.skills', '',
         '/** Generated from tests/fixtures/kotlin-parity at React revision %s. */' % ex['sourceRevision'],
         'class TrainingParityTest {', '    private class Draws(private val values: List<Double>) : RandomSource {',
         '        private var index = 0', '        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]', '    }',
         '    private fun factory(draws: List<Double>) = ExerciseFactory(Draws(draws), ExerciseIdFactory { "generated" })',
         '    @Test fun metadataAndChainLinks() {', '        assertEquals(16, skills.size)', '        assertEquals(12, sentenceSeeds.size)',
         '        for (seed in sentenceSeeds) {', '            val chain = factory(listOf(0.1)).generateChain(seed)',
         '            assertEquals(5, chain.size)', '            for (i in 0 until 4) assertEquals(chain[i].expected, chain[i + 1].source)',
         '        }', '    }', '    @Test fun allExerciseCases() {']
for c in ex['cases']:
    inp = c['input']; output = c['expected'].get('exercise', c['expected']); draws = c['context']['randomDraws']
    lines.append('        run { // ' + c['id'])
    lines.append('            val factory = factory(listOf(%s))' % ', '.join(str(float(d)) for d in draws))
    if 'step' in inp:
        lines.append('            val actual = factory.generateChain(%s)[%d]' % (seed(inp['seed']), inp['step'] - 1))
    else:
        pref = 'null' if inp['preferred'] is None else seed(inp['preferred'])
        lines.append('            val actual = factory.generateForSkill(%s, %s)' % (s(inp['skillId']), pref))
    lines.append('            assertEquals(%s, actual, %s)' % (exercise(output), s(c['id'])))
    lines.append('        }')
lines += ['    }', '    @Test fun allEvaluationCases() {']
for c in ev['cases']:
    result = c['expected']['result']
    lines.append('        run { // ' + c['id'])
    lines.append('            assertEquals(Evaluation(%s, %s, %s, %d), evaluate(%s, %s), %s)' % (
        str(result['correct']).lower(), s(result['normalized']), s(result['expected']), result['distance'],
        s(c['input']['answer']), exercise(c['input']['exercise']), s(c['id'])))
    lines.append('        }')
lines += ['    }', '    @Test fun queueSelection() {',
          '        val cards = listOf(DueSkillCard("first", 20), DueSkillCard("second", 10), DueSkillCard("third", 10))',
          '        assertEquals("first", nextSkillId(cards, "first"))',
          '        assertEquals("second", nextSkillId(cards))',
          '        assertEquals("second", nextSkillId(cards, "unknown"))',
          '        assertFailsWith<IllegalStateException> { nextSkillId(emptyList()) }',
          '    }', '    @Test fun portsAndUtf16Distance() {',
          '        assertFailsWith<IllegalArgumentException> { ExerciseFactory(RandomSource { 1.0 }, ExerciseIdFactory { \"id\" }).generateForSkill(\"mixed\") }',
          '        assertFailsWith<IllegalArgumentException> { ExerciseFactory(RandomSource { 0.0 }, ExerciseIdFactory { \"\" }).generateChain() }',
          '        val ex = factory(listOf(0.0)).generateForSkill(\"mixed\").copy(expected = \"a\", accepted = emptyList())',
          '        assertEquals(2, evaluate(\"😀\", ex).distance)',
          '        assertEquals(1, evaluate(\"b\", ex.copy(expected = \"aaaa\", accepted = listOf(\"bb\"))).distance)',
          '        assertFailsWith<IllegalStateException> { factory(listOf(0.0)).generateForSkill(\"unknown\") }',
          '        val preferred = factory(listOf(0.0)).generateForSkill(\"case.gen.neg\", SentenceSeed(\"wife\", \"small\"))',
          '        assertEquals(\"small\", preferred.adjectiveId)',
          '        assertEquals(\"Nie widzę mojej małej żony.\", preferred.expected)',
          '    }', '}']
generated = '\n'.join(lines) + '\n'
if '--check' in sys.argv:
    assert OUT.read_text() == generated, 'Generated test differs from locked fixtures'
else:
    OUT.write_text(generated)
print(f'{OUT}: {len(ex["cases"])} exercise and {len(ev["cases"])} evaluation cases, source {ex["sourceRevision"]}')

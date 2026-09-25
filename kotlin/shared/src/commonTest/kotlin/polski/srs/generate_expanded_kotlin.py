"""Emit common Kotlin differential assertions from generate_expanded.mjs oracle."""
import json
from pathlib import Path
p=Path(__file__).with_name('expanded_oracle.json')
fixtures=json.loads(p.read_text())
out=Path(__file__).with_name('SchedulerExpandedTest.kt')
def q(s): return json.dumps(s)
def card(c):
    return f'SrsCard(Instant.parse({q(c["due"])}), {float(c["stability"])} , {float(c["difficulty"])} , {c["elapsed_days"]}, {c["scheduled_days"]}, {c["reps"]}, {c["lapses"]}, {c["learning_steps"]}, CardState.entries[{c["state"]}], ' + (f'Instant.parse({q(c["last_review"])})' if c.get('last_review') else 'null') + ')'
lines=['package polski.srs','','import kotlin.test.Test','import kotlin.test.assertEquals','import kotlin.test.assertTrue','import kotlin.math.abs','import kotlin.time.Instant','','/** Generated from installed ts-fsrs 5.4.2; absolute numeric tolerance 1e-7. */','class SchedulerExpandedTest {','    private fun check(expected: SrsCard, actual: SrsCard, id: String) {','        assertEquals(expected.due, actual.due, id + " due")','        assertTrue(abs(expected.stability - actual.stability) <= 1e-7, id + " stability: " + actual.stability)','        assertTrue(abs(expected.difficulty - actual.difficulty) <= 1e-7, id + " difficulty: " + actual.difficulty)','        assertEquals(expected.elapsedDays, actual.elapsedDays, id + " elapsed")','        assertEquals(expected.scheduledDays, actual.scheduledDays, id + " scheduled")','        assertEquals(expected.reps, actual.reps, id + " reps")','        assertEquals(expected.lapses, actual.lapses, id + " lapses")','        assertEquals(expected.learningSteps, actual.learningSteps, id + " steps")','        assertEquals(expected.state, actual.state, id + " state")','        assertEquals(expected.lastReview, actual.lastReview, id + " last review")','    }','']
for x in fixtures['cases']:
 name=x['id'].replace('-','_')
 lines += [f'    @Test fun {name}() {{',f'        val at = Instant.parse({q(x["at"])})',f'        val source = StoredCard("oracle", {card(x["input"])})',f'        val scheduler = FsrsScheduler({str(x["fuzz"]).lower()})','        val before = source.copy(card = source.card.copy())','        val preview = scheduler.preview(source, at)']
 for rating in ['again','hard','good','easy']:
  lines += [f'        assertEquals(Instant.parse({q(x["expected"]["preview"][rating])}), preview.{rating}, {q(x["id"] + " preview " + rating)})']
 lines += [f'        val reviewed = scheduler.review(source, Rating.entries[{x["rating"]-1}], at)', f'        check({card(x["expected"]["review"])}, reviewed.card, {q(x["id"])})','        assertEquals(before, source)','        assertEquals(preview[Rating.entries['+str(x['rating']-1)+']], reviewed.card.due)','    }','']
lines += ['}']
out.write_text('\n'.join(lines)+'\n')
print(len(fixtures['cases']))
